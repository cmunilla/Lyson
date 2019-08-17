package cmssi.lyson.exception;


public class LysonException extends RuntimeException {

	/**
	 */
	private static final long serialVersionUID = -594551979575684100L;

	public LysonException(String message) {
		super(message);
	}

	public LysonException(Exception e) {
		super(e);
	}

}
