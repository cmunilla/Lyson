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
package cmssi.lyson.handler.mapping;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A MappingBuilder is in charge of creating new mapped Object(s) instance(s)
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.5
 */
public class MappingBuilder {
	
	private static final Logger LOG = Logger.getLogger(MappingBuilder.class.getName());
	
	private MappingType mappingType;
	
	/**
	 * Constructor 
	 * 
	 * @param mappedType the type of the mapped value Object(s) 
	 * wrapped by the MappingBuffer to be instantiated 
	 */
	public MappingBuilder(MappingType mappedType) {
		this.mappingType = mappedType;
	}
	
	/**
	 * @param <T>
	 * @param <K>
	 * 
	 * @return the container Object holding the mapped Objects 
	 */
	public <T,K> T buildMappedObjectsContainer(){
		Class<T> containerType = (Class<T>)(this.mappingType.isPojoCollection()||this.mappingType.isSimpleCollection()
				?this.mappingType.getRawType():null);
		return this.<T,K>build(containerType);
	}

	/**
	 * @param <K>
	 * 
	 * @return a new instance of the mapped type
	 */
	public <K> K buildMappedObject() {
		Class<K> type = (Class<K>)(this.mappingType.isPojoCollection()?
			this.mappingType.getComponentType():this.mappingType.getMappedType()); 
		K object = null;
		if(type == null)
			object = (K) new ArrayList<Object>();
		else 
			object = this.<K,Object>build(type);
		
		return object;
	}	
	
	/**
	 * @param <T>
	 * @param <K>
	 * 
	 * @return the mapped Object 
	 */
	private <T,K> T build(Class<?> clazz){
		Object obj = null;
		if(clazz == null)
			return (T) obj;
		if(clazz.isArray())
			obj = (T) new ArrayList<K>();	
		else if(!clazz.isInterface() && ((clazz.getModifiers() 
				& Modifier.ABSTRACT) !=  Modifier.ABSTRACT)) {
			try {
				obj = clazz.getConstructor().newInstance();			
			} catch (Exception e) {
				if(LOG.isLoggable(Level.SEVERE)) {
					LOG.log(Level.SEVERE, e.getMessage(),e);
				}
			}
		} else if(Queue.class.isAssignableFrom(clazz)) 
			obj = (T) new LinkedList<K>();
		else if(Vector.class.isAssignableFrom(clazz)) 
			obj = (T) new Stack<K>();
		else if(List.class.isAssignableFrom(clazz)) 
			obj = (T) new ArrayList<K>();
		else if(Set.class.isAssignableFrom(clazz))
			obj = (T) new HashSet<K>();				
		else if(Map.class.isAssignableFrom(clazz))
			obj = (T) new HashMap<String,K>();			
		else if(Dictionary.class.isAssignableFrom(clazz))
			obj = (T) new Hashtable<String,K>();
		else 
			throw new IllegalArgumentException();
		return (T) obj;
	}
	
	/**
	 * @return true if the mapped type is an array
	 */
	public boolean isArrayType() {
		return this.mappingType.getMappedType().isArray();
	}
	
	/**
	 * @return true if the type is the one of a container 
	 */
	public boolean isPojoCollection() {
		return this.mappingType.isPojoCollection();
	}
	
	/**
	 * @return true if the mapped type is a List or a Map
	 */
	public boolean isSimpleCollection() {
		return this.mappingType.isSimpleCollection();
	}
	
	/**
	 * @return the mapped type name
	 */
	public String getMappedTypeName() {
		return this.mappingType.getMappedType()==null?null:this.mappingType.getMappedType().getName();
	}
	
	/**
	 * @param coll
	 * 
	 * @return the specified Collection converted into an array 
	 */
	public Object asArray(Collection coll) {
		if(!this.isArrayType())
			return null;
		Class<?> componentType = this.mappingType.getComponentType();
		Object array = Array.newInstance(componentType, coll.size());
		int n=0;
		for(Iterator it = coll.iterator();it.hasNext();)
			Array.set(array, n++, MappingType.cast(componentType, it.next()));
		return array;
	}
	
}
