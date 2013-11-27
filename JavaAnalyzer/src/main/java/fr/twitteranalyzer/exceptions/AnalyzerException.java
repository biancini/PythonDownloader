package fr.twitteranalyzer.exceptions;

public class AnalyzerException extends Exception {

	private static final long serialVersionUID = 5612479851676891761L;

	public AnalyzerException() {
		super();
	}

	public AnalyzerException(String message) {
		super(message);
	}

	public AnalyzerException(Exception e) {
		super(e);
	}

}
