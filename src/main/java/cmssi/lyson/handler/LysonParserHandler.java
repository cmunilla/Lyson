package cmssi.lyson.handler;

import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.exception.LysonParsingException;

/**
 * The natural recipient of the {@link ParsingEvent}s and
 * {@link LysonParsingException}s triggered by a 
 * {@link cmssi.lyson.LysonParser}
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public interface LysonParserHandler {
	
	/**
	 * Handles the {@link ParsingEvent} passed 
	 * as parameter
	 * 
	 * @param event the {@link ParsingEvent} to be handled
	 * 
	 * @return a boolean value defining whether to continue 
	 * or not the parsing
	 */
	boolean handle(ParsingEvent event);
	
	/**
	 * Handles the {@link LysonParsingException} passed 
	 * as parameter
	 * 
	 * @param exception the {@link LysonParsingException} to 
	 * be handled
	 */
	void handle(LysonParsingException exception);
}
