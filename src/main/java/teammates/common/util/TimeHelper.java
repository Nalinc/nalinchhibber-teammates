package teammates.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** A helper class to hold time-related functions (e.g., converting dates to strings etc.).
 * Time zone is assumed as UTC unless specifically mentioned.
 */
public class TimeHelper {
	
	/**
	 * Convert a date string and time string into a Date object. Returns null on error.
	 * 
	 * @param date
	 *            The date in format dd/MM/yyyy
	 * @param time
	 *            The time in format HHMM
	 */
	public static Date combineDateTime(String inputDate, String inputTime) {
		if (inputDate == null || inputTime == null) {
			return null;
		}

		int inputTimeInt = 0;
		if (inputTime != null) {
			inputTimeInt = Integer.parseInt(inputTime) * 100;
		}
		return convertToDate(inputDate, inputTimeInt);
	}

	/**
	 * Returns the date object with specified offset in number of days from now
	 */
	public static Date getDateOffsetToCurrentTime(int offsetDays) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(cal.getTime());
		cal.add(Calendar.DATE, +offsetDays);
		return cal.getTime();
	}

	/**
	 * Returns the date object with specified offset in number of ms from now.
	 */
	public static Date getMsOffsetToCurrentTime(int offsetMilliseconds) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(cal.getTime());
		cal.add(Calendar.MILLISECOND, +offsetMilliseconds);
		return cal.getTime();
	}

	public static Date getMsOffsetToCurrentTimeInUserTimeZone(int offset, double timeZone) {
		Date d = getMsOffsetToCurrentTime(offset);
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);
		return convertToUserTimeZone(c, timeZone).getTime();
	}

	public static Calendar convertToUserTimeZone(Calendar time, double timeZone) {
		time.add(Calendar.MILLISECOND, (int) (60 * 60 * 1000 * timeZone));
		return time; // for chaining
	}

	/**
	 * Formats a date in the corresponding option value in 'Time' dropdowns The
	 * hour just after midnight is converted to option 24 (i.e., 2359 as shown
	 * to the user) 23.59 is also converted to 24. (i.e., 23.59-00.59 ---> 24)
	 */
	public static String convertToOptionValueInTimeDropDown(Date date) { 
		//TODO: see if we can eliminate this method (i.e., merge with convertToDisplayValueInTimeDropDown)
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(date);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		hour = (hour == 0 ? 24 : hour);
		hour = ((hour == 23) && (minutes == 59)) ? 24 : hour;
		return hour + "";
	}
	
	/**
	 * @return one of these : 0100H, 0200H, ..., 0900H, 1000H, ... 2300H, 2359H.
	 * Note the last one is different from the others.
	 */
	public static String convertToDisplayValueInTimeDropDown(Date date) {
		String optionValue = convertToOptionValueInTimeDropDown(date);
		if (optionValue.equals("24")) {
			return "2359H";
		}else if (optionValue.length() == 1) {
			return "0" + optionValue + "00H";
		} else if (optionValue.length() == 2) {
			return optionValue + "00H";
		} else {
			throw new RuntimeException("Unrecognized time option: "+optionValue);
		}
	}

	/**
	 * Formats a date in the format dd/MM/yyyy
	 */
	public static String formatDate(Date date) {
		if (date == null)
			return "";
		return new SimpleDateFormat("dd/MM/yyyy").format(date);
	}

	/**
	 * Formats a date in the format dd MMM yyyy, hh:mm. Example: 05 May 2012,
	 * 22:04<br />
	 * This is used in JSP pages to display time information to users
	 */
	public static String formatTime(Date date) {
		if (date == null)
			return "";
		return new SimpleDateFormat("dd MMM yyyy, HH:mm").format(date);
	}

	public static String calendarToString(Calendar c) {
		if (c == null)
			return "";
		return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss SSS").format(c.getTime());
	}

	/**
	 * @param dateInStringFormat should be in the format {@link Constants.DEFAULT_DATE_TIME_FORMAT}
	 */
	public static Date convertToDate(String dateInStringFormat) throws ParseException {
		DateFormat df = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
		return df.parse(dateInStringFormat);
	}

	public static Calendar dateToCalendar(Date date) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (date == null)
			return c;
		c.setTime(date);
		return c;
	}
	
	/**
	 * Returns the date object representing the next full hour from now.
	 * Example: If now is 1055, this will return 1100
	 */
	public static Date getNextHour() {
		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.add(Calendar.HOUR_OF_DAY, 1);
		cal.set(Calendar.MINUTE, 0);
		return cal.getTime();
	}

	/**
	 * Returns whether the given date is being used as a special representation,
	 * signifying it's face value should not be used without proper processing.
	 * A null date is not a special time.
	 */
	public static boolean isSpecialTime(Date date) {
		
		if (date == null) {
			return false;
		}
		
		if (date.equals(Constants.TIME_REPRESENTS_FOLLOW_OPENING) ||
			date.equals(Constants.TIME_REPRESENTS_FOLLOW_VISIBLE) ||
			date.equals(Constants.TIME_REPRESENTS_LATER) ||
			date.equals(Constants.TIME_REPRESENTS_NEVER)) {
			return true;
		} else {
			return false;
		}
		
	}

	private static Date convertToDate(String date, int time) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	
		Date newDate = new Date();
	
		// Perform date manipulation
		try {
			newDate = sdf.parse(date);
			calendar.setTime(newDate);
	
			if (time == 2400) {
				calendar.set(Calendar.HOUR, 23);
				calendar.set(Calendar.MINUTE, 59);
			} else {
				calendar.set(Calendar.HOUR, time / 100);
				calendar.set(Calendar.MINUTE, time % 100);
			}
	
			return calendar.getTime();
		} catch (Exception e) {
			return null;
		}
	
	}

}
