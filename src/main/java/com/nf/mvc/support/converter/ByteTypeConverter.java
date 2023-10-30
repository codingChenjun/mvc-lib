package com.nf.mvc.support.converter;

import com.nf.mvc.support.WebTypeConverter;

public class ByteTypeConverter implements WebTypeConverter<Byte> {
  @Override
  public Byte convert(String paramValue) throws Exception {
    return Byte.valueOf(paramValue);
  }
}
