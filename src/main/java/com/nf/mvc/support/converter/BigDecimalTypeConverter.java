package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

import java.math.BigDecimal;


public class BigDecimalTypeConverter implements WebTypeConverter<BigDecimal> {

  @Override
  public BigDecimal convert(String paramValue) throws Exception {
    return new BigDecimal(paramValue);
  }
}
