package com.example.instagram.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    /**
     * @return formatted timestamp to display on the timeline
     * @param date the date of the timestamp
     */
    public static String formatTimestamp(Date date) {
        // Parse the date into a SimpleDateFormat object
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        long nowMs, thenMs;
        thenMs = date.getTime();
        nowMs = System.currentTimeMillis();

        // Calculate difference in milliseconds
        long diff = nowMs - thenMs;

        // Calculate difference in seconds
        long diffSeconds = diff / (1000);
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        // Formate timestamp
        if (diffSeconds < 10) {
            return "now";
        } else if (diffSeconds < 60) {
            return diffSeconds + "s";
        } else if (diffMinutes < 60) {
            return diffMinutes + "m";
        } else if (diffHours < 24) {
            return diffHours + "h";
        } else if (diffDays < 7) {
            return diffDays + "d";
        } else if (date.getYear() == new Date().getYear()){
            SimpleDateFormat todate = new SimpleDateFormat("MMM dd",
                    Locale.ENGLISH);
            return todate.format(date);
        } else {
            SimpleDateFormat todate = new SimpleDateFormat("dd MMM yy",
                    Locale.ENGLISH);
            return todate.format(date);
        }
    }
}
