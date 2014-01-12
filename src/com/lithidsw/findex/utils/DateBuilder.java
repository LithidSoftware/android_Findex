package com.lithidsw.findex.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateBuilder {

    public static String getFullDate(Context c, long date) {
        Date dateFromSms = new Date(date);
        Locale locale = Locale.getDefault();

        SimpleDateFormat day = new SimpleDateFormat("EEE", locale);
        String strDay = day.format(dateFromSms);

        SimpleDateFormat month = new SimpleDateFormat("MMM", locale);
        String strMonth = month.format(dateFromSms);

        SimpleDateFormat dayNum = new SimpleDateFormat("dd", locale);
        String strDayNum = dayNum.format(dateFromSms);

        SimpleDateFormat hour = new SimpleDateFormat("HH", locale);
        String strH = hour.format(dateFromSms);
        String strHour = String.valueOf(getHour(c, strH));

        SimpleDateFormat min = new SimpleDateFormat("mm", locale);
        String strMin = min.format(dateFromSms);
        return strDay + ", " + strMonth + strDayNum + " " + strHour + ":"
                + strMin + getAmPm(c, strH);
    }

    private static int getHour(Context c, String sdf) {
        int hour = Integer.parseInt(sdf);
        if (!DateFormat.is24HourFormat(c)) {
            if (hour > 12) {
                return hour - 12;
            } else if (hour == 0) {
                return 12;
            } else {
                return hour;
            }
        } else {
            return hour;
        }
    }

    private static String getAmPm(Context c, String sdf) {
        int hour = Integer.parseInt(sdf);
        if (!DateFormat.is24HourFormat(c)) {
            if (hour >= 12) {
                return "PM";
            } else {
                return "AM";
            }
        } else {
            return "";
        }
    }
}
