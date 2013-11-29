package fr.twitteranalyzer.utils;

public final class LoggerUtils {

	private LoggerUtils() {
		// Do nothing
	}

	public static void writeLog(String message, boolean error) {
		if (error) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}
}
