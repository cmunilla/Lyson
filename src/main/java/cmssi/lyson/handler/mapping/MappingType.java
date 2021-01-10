/*
 * MIT License
 *
 * Copyright (c) 2019 - 2021  Christophe Munilla
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.LysonParser;

/**
 * Java Type Wrapper
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.5
 */
public class MappingType {

	
	private static final Logger LOG = Logger.getLogger(MappingType.class.getName());
	
	/**
	 * Casting helper method
	 * 
	 * @param clazz the targeted Java Type to cast the specified Object to
	 * @param val the Object to be casted into the specified target Java Type
	 * 
	 * @return the casted value of the specified Object
	 */
	public static Object cast(Class<?> clazz, Object val) {
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
	
	private static boolean castToBoolean(Object val) {
		if(val.getClass() == String.class) 
			return Boolean.parseBoolean((String)val);
		if(val instanceof Number) 
			return ((Number)val).intValue() > 0;
		return false;
	}

	private static char castToChar(Object val) {
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

	private static Object castToNumber(Class<?> clazz, Number number) {
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
	
	/*
	 * (non-javadoc)
	 * Returns true if the java Type passed as parameter is one of the primitive types 
	 * or one of their corresponding object wrapper classes. Otherwise returns false
	*/
	protected static boolean isPrimitive(Class<?> clazz) {
		if(clazz == null)
			return false;
		if(clazz.isPrimitive())
			return true;
		return false;
	}
	
	/*
	 * (non-javadoc)
	 * Returns true if the java Type passed as parameter is one of the primitive types 
	 * or one of their corresponding object wrapper classes. Otherwise returns false
	*/
	protected static boolean isPrimitiveOrBoxing(Class<?> clazz) {
		if(isPrimitive(clazz))
			return true;
		if(clazz == null)
			return false;
		switch(clazz.getSimpleName()) {
			case "Boolean" :
			case "Character":
			case "Byte":
			case "Short":	
			case "Integer":	
			case "Long":	
			case "Float":
			case "Double":
			case "BigInteger":
			case "BigDecimal":
				return true;
			default:
				break;
		}
		return clazz==String.class;
	}
	
	private Class<?> mappedType = null;
	private Class<?> componentType = null;
	private Class<?> rawType = null;	
	private boolean isPojoCollection = false;
	private boolean isSimpleCollection = false;
	
	/**
	 * Constructor 
	 * 
	 * @param type the Java Type to be wrapped by the MappingType 
	 * to be instantiated 
	 */
	public MappingType(Type type){
		this.defineWrappedType(type);
	}
	
	/**
	 * Returns true if the handled Java Type is one among Array, List, Set, Map, Dictionary whose 
	 * generic type is neither a primitive, nor boxing, nor a String  
	 *  
	 * @return true if the mapped type is Collection; false otherwise
	 */
	public boolean isPojoCollection() {
		return this.isPojoCollection;
	}
	
	/**
	 * @return true if the mapped type is a List or a Map
	 */
	public boolean isSimpleCollection() {
		return this.isSimpleCollection;
	}
	
	
	/**
	 * Returns the defined mapped Type of the mapping process
	 * configured by this MappingConfiguration
	 * 
	 * @return this MappingConfiguration's mapped Type
	 */
	public Class<?> getMappedType() {
		return this.mappedType;
	}
	
	/**
	 * Returns the wrapper java Type if defined - Otherwise returns null
	 * 
	 * @return the wrapper java Type or null
	 */
	public Class<?> getRawType(){
		return this.rawType;
	}

	/**
	 * Returns embedded mapped java Type if defined - Otherwise returns null
	 * 
	 * @return the embedded mapped java Type or null
	 */
	public Class<?> getComponentType(){
		return this.componentType;
	}

	/*
	 * (non-javadoc)
	 * Checks whether the java Type passed as parameter is a parameterized one, in manner
	 * of identifying both raw and component types involved 
	 */
	private <K,T> void defineWrappedType(Type mappedType) {		
		if(mappedType == null)
			return;
		if(mappedType instanceof ParameterizedType) {
			Class<?> clazz = (Class) ((ParameterizedType)mappedType).getRawType();
			this.mappedType = clazz;
			
			ParameterizedType pt = (ParameterizedType)mappedType;
			this.rawType = (Class<?>) pt.getRawType();
			
			Type[] types = pt.getActualTypeArguments();			
			if(List.class.isAssignableFrom(clazz)) 
				this.componentType = (Class<?>) types[0];
			if(Set.class.isAssignableFrom(clazz)) 
				this.componentType = (Class<?>) types[0];
			if(Queue.class.isAssignableFrom(clazz)) 
				this.componentType = (Class<?>) types[0];
			if(Vector.class.isAssignableFrom(clazz)) 
				this.componentType = (Class<?>) types[0];
			if(Map.class.isAssignableFrom(clazz) || Dictionary.class.isAssignableFrom(clazz)) 
				this.componentType = (Class<?>) types[1];
		} else {
			this.mappedType = (Class)mappedType;
			if(this.mappedType.isArray()) {
				this.rawType = this.mappedType;
				this.componentType = this.mappedType.getComponentType();			
			} else if(Collection.class.isAssignableFrom(this.mappedType) 
				|| Map.class.isAssignableFrom(this.mappedType)
				|| Dictionary.class.isAssignableFrom(this.mappedType)
				|| Vector.class.isAssignableFrom(this.mappedType)
				|| Queue.class.isAssignableFrom(this.mappedType))
				this.rawType = this.mappedType;
		}
		boolean prim = this.componentType==null?true:isPrimitiveOrBoxing(this.componentType);
		this.isPojoCollection = prim?false:(this.componentType!=null && this.componentType!=Object.class);
		this.isSimpleCollection  = this.componentType!=null && isPrimitiveOrBoxing(this.componentType);
		
	}
}
