package com.patho.main.util.helper;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class TimeUtil {

    public static final int getCurrentYear() {
        return getYearAsInt(Calendar.getInstance());
    }

    public static final int getYearAsInt(Calendar cal) {
        return cal.get(Calendar.YEAR);
    }

    public static final long getDateInUnixTimestamp(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day, hour, minute, second);
        return cal.getTimeInMillis();
    }

    public static final boolean isDateOnSameDay(long date, long timeOfDay) {
        return isDateOnSameDay(new Date(date), new Date(timeOfDay));
    }

    public static final boolean isDateOnSameDay(Date date, Date timeOfDay) {
        Calendar dateC = Calendar.getInstance();
        dateC.setTime(date);
        Calendar timeOfDayC = Calendar.getInstance();
        timeOfDayC.setTime(timeOfDay);

        return (dateC.get(Calendar.YEAR) == timeOfDayC.get(Calendar.YEAR))
                && (dateC.get(Calendar.DAY_OF_YEAR) == timeOfDayC.get(Calendar.DAY_OF_YEAR));
    }

    public static final Calendar setMonthEnding(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        setDayEnding(cal);
        return cal;
    }

    public static final Calendar setMonthBeginning(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        setDayBeginning(cal);
        return cal;
    }

    public static final Calendar setWeekEnding(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        setDayEnding(cal);
        return cal;
    }

    public static final Calendar setWeekBeginning(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        setDayBeginning(cal);
        return cal;
    }

    public static final Calendar setDayEnding(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    public static final long setDayBeginning(long date) {
        return setDayBeginning(new Date(date)).getTime();
    }

    public static final Date setDayBeginning(Date date) {
        Calendar dateC = Calendar.getInstance();
        dateC.setTime(date);
        return setDayBeginning(dateC).getTime();
    }

    public static final Calendar setDayBeginning(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static final String formatDate(long date, String formatString) {
        return formatDate(new Date(date), formatString);
    }

    /**
     * Formats a given date using the given formatString. Returns a string.
     *
     * @param date
     * @param formatString
     * @return
     */
    public static final String formatDate(Date date, String formatString) {
        String dateString = "";

        try {
            SimpleDateFormat sdfr = new SimpleDateFormat(formatString);
            dateString = sdfr.format(date);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return dateString;
    }

    public static long toUnixTime(Instant i) {
        BigDecimal nanos = BigDecimal.valueOf(i.getNano(), 9);
        BigDecimal seconds = BigDecimal.valueOf(i.getEpochSecond());
        BigDecimal total = seconds.add(nanos);
        return total.longValue();
    }

    public static long toUnixTimeMillis(Instant i) {
        BigDecimal nanos = BigDecimal.valueOf(i.getNano(), 9);
        BigDecimal seconds = BigDecimal.valueOf(i.getEpochSecond());
        BigDecimal total = seconds.add(nanos);
        return total.longValue() * 1000;
    }
}
