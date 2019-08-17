package cmssi.lyson.event;

/**
 * A ParsingEvent wrapper service
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface ParsingEventWrapper {

	/**
	 * Returns the wrapped ParsingEvent
	 * 
	 * @return the wrapped ParsingEvent
	 */
	ParsingEvent getEvent();
}
