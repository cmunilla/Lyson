package cmssi.lyson.exception;

/**
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class LysonException extends RuntimeException {

	/**
	 * Generated long ID
	 */
	private static final long serialVersionUID = -594551979575684100L;

	/**
	 * Constructor
	 * 
	 * @param message the error message
	 */
	public LysonException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param e the cause Exception
	 */
	public LysonException(Exception e) {
		super(e);
	}

}
