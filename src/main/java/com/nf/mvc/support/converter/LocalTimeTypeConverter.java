package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTypeConverter implements WebTypeConverter<LocalTime> {

  @Override
  public LocalTime convert(String paramValue) throws Exception {
    return LocalTime.parse(paramValue, DateTimeFormatter.ofPattern(DateTypeConverter.TIME_PATTERN));
  }
}
