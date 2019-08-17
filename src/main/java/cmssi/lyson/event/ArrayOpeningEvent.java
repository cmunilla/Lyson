package cmssi.lyson.event;

/**
 * JSON ParsingEvent dedicated to array opening one
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface ArrayOpeningEvent  {

	/**
	 * Returns the current Integer index of the JSON array
	 * whose opening was delimited by this ArrayOpeningEvent
	 * 
	 * @return the associated JSON array current Integer index
	 */
	int getInnerIndex();

	/**
	 * Defines the current Integer index of the JSON array
	 * whose opening was delimited by this ArrayOpeningEvent
	 * 
	 * @param innerIndex the associated JSON array current 
	 * Integer index
	 * 
	 * @return this ArrayOpeningEvent
	 */
	ArrayOpeningEvent withInnerIndex(int innerIndex) ;
	
}
