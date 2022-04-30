/*
 * MIT License
 *
 * Copyright (c) 2019 - 2022  Christophe Munilla
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cmssi.lyson.handler.mapping;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonParsingException;
import cmssi.lyson.handler.LysonParserHandler;

/**
 * {@link LysonParserHandler} implementation dedicated to a JSON chars sequence mapping
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class MappingHandler implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(MappingHandler.class.getName());

	public static final String IDENTITY_MAPPING_KEY = "identity"; 
	//keep intermediate data structure while parsing
	private Deque<Object> stack = new LinkedList<>();
	private MappingBuffer buffer;
	
	/**
	 * Constructor
	 * 
	 * @param mappedType the Java Type to map the parsed JSON chars sequence to
	 */
	public MappingHandler(Class<?> mappedType){
		this(new MappingType(mappedType));
	}
	
	/**
	 * Constructor
	 * 
	 * @param mappedType the {@link MappingType} wrapping the Java Type to map the 
	 * parsed JSON chars sequence to
	 */
	public MappingHandler(MappingType mappedType){
		this.buffer = new MappingBuffer(mappedType);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param mapped the instance of which assigning the fields of with the parsed 
	 * JSON chars sequence
	 */
	public MappingHandler(Object mapped){
		this.buffer = new MappingBuffer(mapped);
	}
	
	/**
	 * Constructor
	 * 
	 * @param handleIdentity defines whether JSON Object item key might be reused to assign
	 * as identity to newly created complex data structure (Map)
	 */
	public MappingHandler(boolean handleIdentity){
		this.buffer = new MappingBuffer(handleIdentity);
		this.stack.add(this.buffer.getCurrent());
	}
	
	/**
	 * Constructor
	 */
	public MappingHandler(){
		this(false);
	}

	/**
	 * Returns true if the Stack of intermediate data structures is not 
	 * empty - Otherwise returns false
	 * 
	 * @return true if waiting for an intermediate data structure closing
	 */
	public boolean waitClosing() {
		return !stack.isEmpty();
	}
	
	/**
	 * Returns the {@link MappingBuffer} attached to this MappingHandler
	 *  
	 * @return this MappingHandler's {@link MappingBuffer}
	 */
	protected MappingBuffer getMappingBuffer() {
		return this.buffer;
	}
	

	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) 
			return false;
		if(LOG.isLoggable(Level.FINEST)) 
			LOG.log(Level.FINEST,event.toString());
		
		Object val = null;
		MappingConfiguration config = this.buffer.getMappingConfiguration();
		boolean pojoCollection = config.getMappingBuilder().isPojoCollection();
		
		String key =  config.getPrefix().getSuffix(event.getPath());		
		//Is the mapping defined for the current event's path ? ...
		AccessibleObject ao = config.getMapping(key);
		
		String identity = null;
		
		//otherwise, check if the key of the KeyValueEventWrapper event is a known mapping key
		KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
		if(kvwrapper!=null) {
			if(ao == null) {
				key = config.getPrefix().getSuffix(kvwrapper.getKey());
				ao = config.getMapping(key);
			}
			//define the identity key
			identity = kvwrapper.getKey();
		}
		boolean opening = false;
		boolean closing = false;
		
		Class<?> defaultValueType = event.getType()==ParsingEvent.JSON_ARRAY_OPENING?ArrayList.class:HashMap.class;
		
		switch(event.getType()) {
			case ParsingEvent.JSON_ARRAY_OPENING:
			case ParsingEvent.JSON_OBJECT_OPENING:				
				if(config.getPrefix().isPrefix(event.getPath()) || (pojoCollection && this.stack.isEmpty())) {
					this.buffer.newMappedInstance();
					assignIdentityValue(this,identity);
					if(pojoCollection) {
						MappingHandler sub = new MappingHandler(this.buffer.getCurrent());
						this.stack.push(sub);
						return true;
					}
				} else
					val = handleJsonOpening(ao, defaultValueType, identity);
				opening = true;
				break;
			case ParsingEvent.JSON_ARRAY_ITEM:
				ValuableEventWrapper vwrapper = event.adapt(ValuableEventWrapper.class);
				if(vwrapper!=null) 
					val = vwrapper.getValue();				
				if(this.stack.isEmpty())
					assignValue(ao,val);
				break;
			case ParsingEvent.JSON_OBJECT_ITEM:
				KeyValueEventWrapper kvw = event.adapt(KeyValueEventWrapper.class);
				if(kvw!=null) {
					key = kvw.getKey();
					val = kvw.getValue();
				}	
				if(this.stack.isEmpty())
					assignValue(ao,val);
				break;
			case ParsingEvent.JSON_OBJECT_CLOSING:
			case ParsingEvent.JSON_ARRAY_CLOSING:
				closing = true;
				break;
			default:
				break;
		}		
		if(!stack.isEmpty()) {			
			Object obj = stack.peek();
			if(obj instanceof MappingHandler) {

				MappingConfiguration config_ = ((MappingHandler)obj).getMappingBuffer().getMappingConfiguration();
				boolean pojoCollection_ = config_.getMappingBuilder().isPojoCollection();
				boolean simpleCollection_ = config_.getMappingBuilder().isSimpleCollection();
				
				//if the intermediate MappingHandler is not waiting for a closing
				//event of an sub-intermediate data structure it is the one closed 
				//by the current event
				if(closing && !((MappingHandler)obj).waitClosing()) {
					if((pojoCollection_ || simpleCollection_)) {
						Object container = ((MappingHandler)obj).getMappingBuffer().collect();
						if(config_.getMappingBuilder().isArrayType()) {
							if(ao == null) {
								String[] keyElements = key.split("/");
								//Search for the key in here (this MappingHandler instead of the one on the top of the stack) 
								//because we search for the reference of the embedded array in the currently mapped type
								ao = config.getMapping(keyElements[keyElements.length-1]);
							}
							//As we use a List as intermediate to collect data we need to assign the reference 
							//of the newly created array
							assignValue(ao,container);
						}
					}
					stack.pop();
				} else if (!opening && simpleCollection_) 
					assignValue(((MappingHandler)obj).getMappingBuffer().getCurrent(),key,val);
				else 
				    ((MappingHandler)obj).handle(event);				
				return true;
			}
			if(closing) {
				stack.pop();
				return true;
			}
			assignValue(obj,key,val);			
		}
		if((ao!=null || !stack.isEmpty()) && opening) 
			stack.push(val);
	    return true;
	}

	@Override
	public void handle(LysonParsingException parsingException) {
		if(LOG.isLoggable(Level.SEVERE)) 
			LOG.log(Level.SEVERE,parsingException.getMessage(),parsingException);
	}
	
	/**
	 * Returns the &lt;K&gt; typed mapped instance
	 * 
	 * @param <K> the mapping object result type
	 *  
	 * @return the &lt;K&gt; typed mapped instance
	 */
	public <K> K getMapped() {
		return (K) this.buffer.collect();
	}
	
	//According to the type of the JSON data structure opening event, and the type 
	//of the Field of the mapped Object targeted by the ao AccessibleObject argument
	//the assigned value is a Map, a List or the mapped Object of a sub MappingHandler
	private Object handleJsonOpening(AccessibleObject ao, Class<?> defaultValueType, String identity) {
		Object val = null;
		Object stacked = null;
		Object defaultValue = null;	
		try {
			defaultValue = defaultValueType.getConstructor().newInstance();			
			if(buffer.getMappingConfiguration().handleIdentity())
				assignIdentityValue(defaultValue,identity);
		} catch(ReflectiveOperationException e) {
			if(LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE, e.getMessage(), e);
		}			
		if(ao == null) {
			val = defaultValue;
			stacked = defaultValue;
		} else {
			Type type = null;
			if(ao instanceof Field) 
				type = ((Field)ao).getGenericType();	
			else if(ao instanceof Method)
				type = ((Method)ao).getParameterTypes()[0];
			if(type==null) 
				return stacked;	
			MappingType mappingType = new MappingType(type);
			if(!mappingType.isPojoCollection() 
				&& !mappingType.isSimpleCollection() 
				&& mappingType.getMappedType().isAssignableFrom(defaultValueType)) {
				val = defaultValue;
				stacked = defaultValue;
			} else {
				MappingHandler sub = new MappingHandler(mappingType);
				if(!mappingType.isPojoCollection() && sub.getMappingBuffer().handleIdentity())
					assignIdentityValue(sub, identity);
				val = sub.getMappingBuffer().getCurrent();
				stacked = sub;
			}
			assignValue(ao,val);
		}
		return stacked;
	}
	
	//Assigns the specified identity to the Object passed as parameter - By setting the appropriate 
	//Field if it is an POJO or by associating it to the "identity" key if it is a Map or a Dictionary
	private void assignIdentityValue(Object obj, String identity) {
		if(obj==null || identity == null)
			return;
		if(obj instanceof MappingHandler) {
			Object current = ((MappingHandler)obj).getMappingBuffer().getCurrent();
			MappingConfiguration config = ((MappingHandler)obj).getMappingBuffer().getMappingConfiguration();
			if(config.getMappingBuilder().isSimpleCollection()) {
				if (current instanceof Dictionary)
					((Dictionary)current).put(IDENTITY_MAPPING_KEY, identity);
				else if (current instanceof Map)
					((Map)current).put(IDENTITY_MAPPING_KEY, identity);
			} else {			
				AccessibleObject ao = config.getMapping(MappingConfiguration.IDENTITY_MAPPING);
				if(ao == null) 
					return;			
				try {
					ao.setAccessible(true);
					if(ao instanceof Field) {
						Object typed = MappingType.cast(((Field)ao).getType(), identity);
						((Field)ao).set(current, typed);	
					} else if(ao instanceof Method) {
						Object typed = MappingType.cast(((Method)ao).getParameterTypes()[0], identity);
						((Method)ao).invoke(current, typed);	
					}
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					if(LOG.isLoggable(Level.SEVERE))
						LOG.log(Level.SEVERE,e.getMessage(),e);
				}			
			}
		} 
		if (obj instanceof Dictionary)
			((Dictionary)obj).put(IDENTITY_MAPPING_KEY, identity);
		else if (obj instanceof Map)
			((Map)obj).put(IDENTITY_MAPPING_KEY, identity);
	}
	
	//Assigns the Field value of the current mapped type object by the way of the specified 
	//AccessibleObject by setting it if it's a Field or invoking it if it's a Method
	private void assignValue(AccessibleObject ao, Object val) {
		if(ao == null) 
			return;
		try {
			ao.setAccessible(true);
			if(ao instanceof Field) {
				Object obj = MappingType.cast(((Field)ao).getType(),val);
				((Field)ao).set(this.buffer.getCurrent() , obj);	
			} else if(ao instanceof Method) {
				Object obj = MappingType.cast(((Method)ao).getParameterTypes()[0],val);
				((Method)ao).invoke(this.buffer.getCurrent() , obj);	
			}
		} catch (Exception e) {
			if(LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE,e.getMessage(),e);
		}
	}
	
	//Adds the specified value Object to the container Object passed as parameter
	//and using the defined String key if it is a Map or a Dictionary
	private void assignValue(Object container, String key, Object val) {
		if(val == null)
			return;
		try {
			if(Collection.class.isAssignableFrom(container.getClass())) 				
				((Collection)container).add(val);
			else if(Vector.class.isAssignableFrom(container.getClass())) 				
				((Vector)container).add(val);
			else if(Queue.class.isAssignableFrom(container.getClass())) 				
				((Queue)container).add(val);
			else if(container instanceof Dictionary && key!=null)
				((Dictionary)container).put(key , val);	
			else if(container instanceof Map && key!=null)
				((Map)container).put(key , val);	
		} catch(IllegalArgumentException | ClassCastException e) {
			if(LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE,e.getMessage(),e);
		}
	}
}
