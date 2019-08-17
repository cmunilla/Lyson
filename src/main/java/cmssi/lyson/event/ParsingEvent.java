package cmssi.lyson.event;

/**
 * JSON parsing event
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface ParsingEvent {
	
	/**
	 * JSON Object opening event type constant
	 */
	public static final int JSON_OBJECT_OPENING = 1;
	/**
	 * JSON Object closing event type constant
	 */
	public static final int JSON_OBJECT_CLOSING = 2;
	/**
	 * JSON Object item event type constant
	 */
	public static final int JSON_OBJECT_ITEM = 4;
	/**
	 * JSON Array opening event type constant
	 */
	public static final int JSON_ARRAY_OPENING = 8;
	/**
	 * JSON Array closing event type constant
	 */
	public static final int JSON_ARRAY_CLOSING = 16;
	/**
	 * JSON Array item event type constant
	 */
	public static final int JSON_ARRAY_ITEM = 32;

	/**
	 * Returns the event type constant of this ParsingEvent
	 * 
	 * @return this ParsingEvent type
	 */
	int getType();
	
	/**
	 * Returns the String path of this ParsingEvent
	 * 
	 * @return this ParsingEvent path
	 */
	String getPath();
	
	/**
	 * Defines the String path of this ParsingEvent
	 * 
	 * @param path the String path of this ParsingEvent
	 * @return this ParsingEvent
	 */
	ParsingEvent withPath(String path);
}
