package fr.twitteranalyzer.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils {

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(DATETIME_FORMAT);

	public static final long HOUR = 3600 * 1000;

	private DateUtils() {
		// Do nothing
	}

	public static String formatDate(Date date) {
		String formattedDate = dateFormatter.format(date);
		return formattedDate;
	}

	public static String formatDateTime(Date date) {
		String formattedDate = dateTimeFormatter.format(date);
		return formattedDate;
	}

	public static Date parseDate(String date) throws ParseException {
		Date parsedDate = dateFormatter.parse(date);
		return parsedDate;
	}

	public static Date parseDateTime(String date) throws ParseException {
		Date parsedDate = dateTimeFormatter.parse(date);
		return parsedDate;
	}

	public static String firstSecondDate(Date date) {
		String strDate = dateFormatter.format(date);
		strDate += " 00:00:00";
		return strDate;
	}

	public static String lastSecondDate(Date date) {
		String strDate = dateFormatter.format(date);
		strDate += " 23:59:59";
		return strDate;
	}
}
