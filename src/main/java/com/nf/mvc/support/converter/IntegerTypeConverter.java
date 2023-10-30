package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

public class IntegerTypeConverter implements WebTypeConverter<Integer> {
  @Override
  public Integer convert(String paramValue) throws Exception {
    return Integer.valueOf(paramValue);
  }
}
