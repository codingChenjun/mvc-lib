package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeConverter implements WebTypeConverter<Date> {

  public static final String DATE_PATTERN = "yyyy-MM-dd";
  public static final String TIME_PATTERN = "HH:mm:ss";
  public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  
  @Override
  public Date convert(String paramValue) throws Exception {
    try {
      return new SimpleDateFormat(DATETIME_PATTERN).parse(paramValue);
    } catch (ParseException e1) {
      throw new IllegalArgumentException("Date convert error.", e1);
    }
  }
}
