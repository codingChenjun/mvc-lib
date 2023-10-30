package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

public class DoubleTypeConverter implements WebTypeConverter<Double> {

  @Override
  public Double convert(String paramValue) throws Exception {
    return Double.valueOf(paramValue);
  }
}
