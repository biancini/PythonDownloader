package fr.twitteranalyzer.utils;

import org.apache.log4j.Logger;

public final class LoggerUtils {
	private static Logger logger = Logger.getLogger("comfr.twitteranalyzer");

	private LoggerUtils() {
		// Do nothing
	}

	public static void info(String message) {
		info(message, null);
	}

	public static void info(String message, Throwable t) {
		if (t != null) {
			logger.info(message, t);
		} else {
			logger.info(message);
		}
	}

	public static void debug(String message) {
		debug(message, null);
	}

	public static void debug(String message, Throwable t) {
		if (t != null) {
			logger.debug(message, t);
		} else {
			logger.debug(message);
		}
	}

	public static void warn(String message) {
		warn(message, null);
	}

	public static void warn(String message, Throwable t) {
		if (t != null) {
			logger.warn(message, t);
		} else {
			logger.warn(message);
		}
	}

	public static void error(String message) {
		error(message, null);
	}

	public static void error(String message, Throwable t) {
		if (t != null) {
			logger.error(message, t);
		} else {
			logger.error(message);
		}
	}
}
