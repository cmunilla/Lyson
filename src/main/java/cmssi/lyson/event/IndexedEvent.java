package cmssi.lyson.event;

/**
 * JSON ParsingEvent dedicated to an indexed one
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface IndexedEvent {
	
	/**
	 * Returns the Integer index of this IndexedEvent
	 * 
	 * @return this IndexedEvent index
	 */
	int getIndex();

	/**
	 * Defines the Integer index of this IndexedEvent
	 * 
	 * @param index the Integer index to be set
	 */
	IndexedEvent withIndex(int index);
}
