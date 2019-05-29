package com.jtf.quartzstarter.utils;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DateUtil {

    public static String format(String pattern, Date date){
       if(Objects.isNull(date)){
           return null;
       }
       if(StringUtils.hasText(pattern)){
           return new SimpleDateFormat(pattern).format(date);
       }
       return new SimpleDateFormat().format(date);
    }

    public static Date parse(String pattern, String dateStr){
        try{
            if(!StringUtils.hasText(dateStr)){
                return null;
            }
            if(StringUtils.hasText(pattern)){
                return new SimpleDateFormat(pattern).parse(dateStr);
            }
            return new SimpleDateFormat().parse(dateStr);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
