package com.kubolab.gnss.casmLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /**
     * 时间戳转化为字符串
     * @param time  1541569323155
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return 2018-11-07 13:42:03
     */
    public static String getDate2String(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }

    /**
     *字符串转化为时间戳
     * @param dateString 2018-11-07 13:42:03,
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return 1541569323000
     */
    public static long getString2Date(String dateString, String pattern) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
