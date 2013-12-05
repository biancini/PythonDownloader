package fr.twitteranalyzer.exceptions;

public class UtilsException extends Exception {

	private static final long serialVersionUID = -8719772315551498029L;

	public UtilsException() {
		super();
	}

	public UtilsException(String message) {
		super(message);
	}

	public UtilsException(Exception e) {
		super(e);
	}

}
