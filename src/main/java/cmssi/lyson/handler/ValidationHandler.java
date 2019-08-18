package cmssi.lyson.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.exception.LysonParsingException;

/**
 * {@link LysonParserHandler} implementation dedicated to 
 * a JSON chars sequence validation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class ValidationHandler implements LysonParserHandler {

	private static final Logger LOG = Logger.getLogger(ValidationHandler.class.getName());
	
	private LysonParsingException parsingException;
	private int count;
	
	@Override
	public boolean handle(ParsingEvent event) {
		if(event == null) {
			return false;
		}
		LOG.log(Level.INFO,event.toString());
		count++;
	    return true;
	}
	
	@Override
	public void handle(LysonParsingException parsingException) {
		this.parsingException = parsingException;
	}
	
	public boolean valid() {
		return (this.parsingException != null) && count > 0;
	}
	
	public LysonParsingException cause() {
		return this.parsingException;		
	}
}
