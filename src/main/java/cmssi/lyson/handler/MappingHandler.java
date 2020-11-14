/*
 * MIT License
 *
 * Copyright (c) 2019 - 2020  Christophe Munilla
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
package cmssi.lyson.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.LysonParser;
import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonParsingException;

/**
 * {@link LysonParserHandler} implementation dedicated to a JSON chars sequence mapping
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.5
 */
public class MappingHandler<T> implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(MappingHandler.class.getName());

	//keep intermediate data structure while parsing
	private Deque<Object> stack = new LinkedList<>();
	private MappingWrapper<T> wrapper;
	
	
	/**
	 * Constructor
	 * 
	 * @param mappedType the java type to map the parsed JSON chars sequence to
	 */
	public MappingHandler(Class<T> mappedType){
		this.wrapper = new MappingWrapper<T>(mappedType);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param mapped the instance to which assign the fields of with the parsed 
	 * JSON chars sequence
	 */
	public MappingHandler(T mapped){
		this.wrapper = new MappingWrapper<T>(mapped);
	}
	
	/**
	 * Constructor
	 * 
	 * @param handleIdentity defines whether JSON Object item key might be reused to assign
	 * as identity to newly created complex data structure (Map, List) - For the specific 
	 * case of List, the identity field will be the first List entry 
	 */
	//If no mapped type or instance is provided the JSON chars sequence will be 
	//converted into Map or a List
	public MappingHandler(boolean handleIdentity){
		this.wrapper = new MappingWrapper<T>(handleIdentity);
		this.stack.add(this.wrapper.mapped());
	}
	
	/**
	 * Constructor
	 */
	//If no mapped type or instance is provided the JSON chars sequence will be 
	//converted into Map or a List
	public MappingHandler(){
		this(false);
	}

	/**
	 * Returns true if the Stack of intermediate data structures kept
	 * while parsing is not empty - Otherwise returns false
	 * 
	 * @return true if waiting for an intermediate data structure closing
	 */
	public boolean waitClosing() {
		return !stack.isEmpty();
	}

	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) 
			return false;
		if(LOG.isLoggable(Level.FINEST)) 
			LOG.log(Level.FINEST,event.toString());
		
		Object val = null;
		MappingConfiguration<T> config = this.wrapper.getMappingConfiguration();
		String key =  config.getPrefix().getSuffix(event.getPath());		
				
		AccessibleObject ao = config.getMapping(key);
		
		String identity = null;
		KeyValueEventWrapper kvwrapper = event.adapt(KeyValueEventWrapper.class);
		if(kvwrapper!=null) {
			if(ao == null) {
				key = config.getPrefix().getSuffix(kvwrapper.getKey());
				ao = config.getMapping(key);
			}
			identity = kvwrapper.getKey();
		}
		boolean opening = false;
		boolean closing = false;
		switch(event.getType()) {
			case ParsingEvent.JSON_ARRAY_OPENING:
			case ParsingEvent.JSON_OBJECT_OPENING:	
				Class<?> defaultType = event.getType()==ParsingEvent.JSON_ARRAY_OPENING?ArrayList.class: HashMap.class;
				if(config.getPrefix().isPrefix(event.getPath())) {
					this.wrapper.newMappedInstance();
					if((!wrapper.getMappingConfiguration().getMappedType().isAssignableFrom(List.class) && 
					    !wrapper.getMappingConfiguration().getMappedType().isAssignableFrom(Map.class)))
						assignIdentityValue(this,identity);
					else if(wrapper.getMappingConfiguration().handleIdentity())
						assignIdentityValue(getMapped(),identity);
				} else 
					val = handleJsonOpening(ao, defaultType, identity);
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
				//if the intermediate MappingHandler is not waiting for a closing
				//event of an sub-intermediate data structure it is the one closed 
				//by the current event
				if(closing && !((MappingHandler)obj).waitClosing()) 
					stack.pop();
				else 
				    ((MappingHandler)obj).handle(event);				
				return true;
			}
			if(closing) {
				stack.pop();
				return true;
			}
			if(obj instanceof List) 				
				((List)obj).add(val);
			 else if(obj instanceof Map)
				((Map)obj).put(key , val);			
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
	@SuppressWarnings("unchecked")
	public <K> K getMapped() {
		return (K) this.wrapper.get();
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
			if(wrapper.getMappingConfiguration().handleIdentity())
				assignIdentityValue(defaultValue,identity);
		} catch(ReflectiveOperationException e) {
			if(LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE, e.getMessage(), e);
		}		
		if(ao == null) {
			val = defaultValue;
			stacked = defaultValue;
		} else {
			Class<?> type = null;
			if(ao instanceof Field) {
				type = ((Field)ao).getType();	
			} else if(ao instanceof Method) {
				type = ((Method)ao).getParameterTypes()[0];	
			}
			if(type==null) {
				return stacked;
			}
			if(type.isAssignableFrom(defaultValueType)) {
				val = defaultValue;
				stacked = defaultValue;
			} else {
				try {
					MappingHandler sub = new MappingHandler(type);
					assignIdentityValue(sub,identity);
					val = sub.getMapped();
					stacked = sub;
				} catch(NullPointerException e) {
					if(LOG.isLoggable(Level.SEVERE))
						LOG.log(Level.SEVERE,e.getMessage(),e);
				}
			}
			assignValue(ao,val);
		}
		return stacked;
	}
	
	//Assign identity
	private void assignIdentityValue(Object obj, String identity) {
		if(obj==null || identity == null)
			return;
		if(obj instanceof MappingHandler) {
			AccessibleObject ao = ((MappingHandler<?>)obj).wrapper.getMappingConfiguration().getMapping(
					MappingConfiguration.IDENTITY_MAPPING);
			if(ao == null) 
				return;			
			try {
				ao.setAccessible(true);
				if(ao instanceof Field) {
					Object typed = cast(((Field)ao).getType(),identity);
					((Field)ao).set(((MappingHandler<?>)obj).wrapper.mapped() , typed);	
				} else if(ao instanceof Method) {
					Object typed = cast(((Method)ao).getParameterTypes()[0],identity);
					((Method)ao).invoke(((MappingHandler<?>)obj).wrapper.mapped() , typed);	
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				if(LOG.isLoggable(Level.SEVERE))
					LOG.log(Level.SEVERE,e.getMessage(),e);
			}
		} else if (obj instanceof List)
			((List)obj).add(identity);
		else if (obj instanceof Map)
			((Map)obj).put("identity" , identity);
	}
	
	//Define the val value Object of the appropriate field of 
	//the mapped object using the ao AccessibleObject argument  
	//by setting it if it's a Field or invoking it if it's a
	//Method
	private void assignValue(AccessibleObject ao, Object val) {
		if(ao == null) 
			return;			
		try {
			ao.setAccessible(true);
			if(ao instanceof Field) {
				Object obj = cast(((Field)ao).getType(),val);
				((Field)ao).set(this.wrapper.mapped() , obj);	
			} else if(ao instanceof Method) {
				Object obj = cast(((Method)ao).getParameterTypes()[0],val);
				((Method)ao).invoke(this.wrapper.mapped() , obj);	
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			if(LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE,e.getMessage(),e);
		}
	}
	
	//Cast helper method
	private Object cast(Class<?> clazz, Object val) {
		if(val == null) {
			return null;
		}
		if(clazz.isAssignableFrom(val.getClass())) {
			return val;
		}	
		String str = String.valueOf(val);
		if(clazz == String.class)
			return str;
		if(clazz == Class.class) {
			try {
				return Class.forName(str);
			} catch(ClassNotFoundException e) {
				if(LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER,e.getMessage(),e);
				return null;
			}
		}
		Number n = null;
		try {
			n = LysonParser.numberFromString(str);
		} catch(NumberFormatException e) {
			if(LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER,e.getMessage(),e);
		}
		if(Number.class.isAssignableFrom(clazz)) {
			return castToNumber(clazz, n);
		}
		switch(clazz.getSimpleName()) {
			case "boolean" :
				return castToBoolean(val);
			case "Boolean" :
				return Boolean.valueOf(castToBoolean(val));
			case "char":
				return castToChar(val);
			case "Character":
				return Character.valueOf(castToChar(val));
			case "byte":
				if(n == null) {
					return (byte)0;
				}
				return n.byteValue();
			case "short":	
				if(n == null) {
					return (short)0;
				}
				return n.shortValue();
			case "int":	
				if(n == null) {
					return 0;
				}
				return n.intValue();
			case "long":	
				if(n == null) {
					return 0l;
				}
				return n.longValue();
			case "float":
				if(n == null) {
					return 0f;
				}				
				return n.floatValue();
			case "double":
				if(n == null) {
					return 0d;
				}			
				return n.doubleValue();	
		}
		return null;
	}
	
	private boolean castToBoolean(Object val) {
		if(val.getClass() == String.class) 
			return Boolean.parseBoolean((String)val);
		if(val instanceof Number) 
			return ((Number)val).intValue() > 0;
		return false;
	}

	private char castToChar(Object val) {
		char c = 0;
		if(val.getClass() == String.class && ((String)val).length()==1)
			return ((String)val).charAt(0);
		if(val instanceof Number) {
			int i =((Number)val).intValue();
			if(i >= Character.MIN_VALUE && i <= Character.MAX_VALUE) {
				return (char)i;
			}		
		}
		if(val.getClass() == Boolean.class){
			if(((Boolean)val).booleanValue())
				return '1';
			else
				return '0';
		}	
		return c;
	}

	private Object castToNumber(Class<?> clazz, Number number) {
		if(number!=null) {
			switch(clazz.getSimpleName()) {
				case "Byte" :
					return Byte.valueOf(number.byteValue());
				case "Short" :
					return Short.valueOf(number.shortValue());
				case "Integer":
					return Integer.valueOf(number.intValue());
				case "Long":
					return Long.valueOf(number.longValue());
				case "BigInteger":
					return new BigInteger(number.toString());
				case "Float":
					return Float.valueOf(number.floatValue());
				case "Double":	
					return Double.valueOf(number.doubleValue());
				case "BigDecimal":
					return new BigDecimal(number.toString());
			}
		}
		return null;
	}
}
