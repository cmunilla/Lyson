package cmssi.lyson.exception;

/**
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class LysonParsingException extends LysonException {

	/**
	 * Generated long ID
	 */
	private static final long serialVersionUID = 1958218358893726694L;
	
	/**
	 * Constructor 
	 * 
	 * @param message the error message
	 * @param line the line number where the error occurred
	 * @param column the column number where the error occurred
	 */
	public LysonParsingException(String message, int line, int column) {
		super(new StringBuilder().append(message).append(String.format(": at line %s,  column %s ", line, column)
		    		).toString());
	}
}
