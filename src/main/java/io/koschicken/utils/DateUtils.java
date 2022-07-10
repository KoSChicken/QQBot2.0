package io.koschicken.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static void main(String[] args) {
        System.out.println(isBetween(23, 8));
    }

    /**
     * 当前时间是否处于指定时间范围
     * 例如 isBetween(23, 8)，当前时间是0点会返回true；当前时间是9-22点则会返回false;
     *
     * @param begin 开始时间
     * @param end   结束时间
     */
    public static boolean isBetween(int begin, int end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= begin || hour <= end;
    }
}
