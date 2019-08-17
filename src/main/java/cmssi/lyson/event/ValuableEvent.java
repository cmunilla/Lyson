package cmssi.lyson.event;

/**
 * JSON ParsingEvent dedicated to one holding an 
 * Object value
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface ValuableEvent {
	
	/**
	 * Returns the value Object held by this ValuableEvent
	 * 
	 * @return the held Object value
	 */
	Object getValue();
	
	/**
	 * Defines the value Object held by this ValuableEvent
	 * 
	 * @param value the held Object value
	 * 
	 * @return this ValuableEvent
	 */
	ValuableEvent withValue(Object value);
}
