package cmssi.lyson.event;

/**
 * JSON ParsingEvent dedicated to one holding a String 
 * key mapped to an Object value
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface KeyValueEvent extends ValuableEvent {
	
	/**
	 * Defines the String key mapped to the value Object 
	 * held by this KeyValueEvent
	 * 
	 * @param key the String key to be mapped to the 
	 * held Object value
	 * 
	 * @return this KeyValueEvent
	 */
	KeyValueEvent withKey(String key);
	
	/**
	 * Returns the String key mapped to the value Object 
	 * held by this KeyValueEvent
	 * 
	 * @return this KeyValueEvent String key
	 */
	String getKey();
}
