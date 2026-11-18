package com.example.el_mozote.statistics;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public enum TimePeriodFilter implements Serializable {
    ALL("Histórico completo"),
    TODAY("Hoy"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mes"),
    THIS_YEAR("Este año");

    private final String displayName;

    TimePeriodFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if a date string in format 'yyyy-MM-dd' matches the current filter period.
     */
    public boolean matchesDateString(String dateStr) {
        if (this == ALL) return true;
        if (dateStr == null || dateStr.trim().isEmpty()) return false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr.trim());
            if (date == null) return false;
            return matchesDate(date);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an epoch timestamp matches the current filter period.
     */
    public boolean matchesTimestamp(long timestamp) {
        if (this == ALL) return true;
        if (timestamp <= 0) return false;
        return matchesDate(new Date(timestamp));
    }

    private boolean matchesDate(Date targetDate) {
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetDate);

        Calendar nowCal = Calendar.getInstance();

        switch (this) {
            case TODAY:
                return targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                        && targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);

            case THIS_WEEK:
                return targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                        && targetCal.get(Calendar.WEEK_OF_YEAR) == nowCal.get(Calendar.WEEK_OF_YEAR);

            case THIS_MONTH:
                return targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                        && targetCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH);

            case THIS_YEAR:
                return targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR);

            case ALL:
            default:
                return true;
        }
    }

    /**
     * Approximate number of days in this period for utilization % calculation.
     */
    public int getReferenceDaysInPeriod() {
        Calendar cal = Calendar.getInstance();
        switch (this) {
            case TODAY:
                return 1;
            case THIS_WEEK:
                return 7;
            case THIS_MONTH:
                return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            case THIS_YEAR:
                return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
            case ALL:
            default:
                return 30; // standard 30-day baseline for rolling calculations
        }
    }
}
