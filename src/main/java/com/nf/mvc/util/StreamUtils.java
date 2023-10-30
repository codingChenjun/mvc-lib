/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nf.mvc.util;

import com.nf.mvc.support.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public abstract class StreamUtils {
  public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";
  public static final int BUFFER_SIZE = 4096;

  private static final byte[] EMPTY_CONTENT = new byte[0];

  public static byte[] copyToByteArray(InputStream in) throws IOException {
    if (in == null) {
      return new byte[0];
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
    copy(in, out);
    return out.toByteArray();
  }

  public static String copyToString(InputStream in, Charset charset) throws IOException {
    if (in == null) {
      return "";
    }

    StringBuilder out = new StringBuilder(BUFFER_SIZE);
    InputStreamReader reader = new InputStreamReader(in, charset);
    char[] buffer = new char[BUFFER_SIZE];
    int charsRead;
    while ((charsRead = reader.read(buffer)) != -1) {
      out.append(buffer, 0, charsRead);
    }
    return out.toString();
  }

  public static String copyToString(ByteArrayOutputStream baos, Charset charset) {
    Assert.notNull(baos, "No ByteArrayOutputStream specified");
    Assert.notNull(charset, "No Charset specified");
    try {
      // Can be replaced with toString(Charset) call in Java 10+
      return baos.toString(charset.name());
    } catch (UnsupportedEncodingException ex) {
      // Should never happen
      throw new IllegalArgumentException("Invalid charset name: " + charset, ex);
    }
  }

  public static void copy(byte[] in, OutputStream out) throws IOException {
    Assert.notNull(in, "No input byte array specified");
    Assert.notNull(out, "No OutputStream specified");

    out.write(in);
    out.flush();
  }

  public static void copy(String in, Charset charset, OutputStream out) throws IOException {
    Assert.notNull(in, "No input String specified");
    Assert.notNull(charset, "No Charset specified");
    Assert.notNull(out, "No OutputStream specified");

    Writer writer = new OutputStreamWriter(out, charset);
    writer.write(in);
    writer.flush();
  }

  public static int copy(InputStream in, OutputStream out) throws IOException {
    Assert.notNull(in, "No InputStream specified");
    Assert.notNull(out, "No OutputStream specified");

    int byteCount = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
      byteCount += bytesRead;
    }
    out.flush();
    return byteCount;
  }

  public static long copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
    Assert.notNull(in, "No InputStream specified");
    Assert.notNull(out, "No OutputStream specified");

    long skipped = in.skip(start);
    if (skipped < start) {
      throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required");
    }

    long bytesToCopy = end - start + 1;
    byte[] buffer = new byte[(int) Math.min(StreamUtils.BUFFER_SIZE, bytesToCopy)];
    while (bytesToCopy > 0) {
      int bytesRead = in.read(buffer);
      if (bytesRead == -1) {
        break;
      } else if (bytesRead <= bytesToCopy) {
        out.write(buffer, 0, bytesRead);
        bytesToCopy -= bytesRead;
      } else {
        out.write(buffer, 0, (int) bytesToCopy);
        bytesToCopy = 0;
      }
    }
    return (end - start + 1 - bytesToCopy);
  }

  public static int drain(InputStream in) throws IOException {
    Assert.notNull(in, "No InputStream specified");
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    int byteCount = 0;
    while ((bytesRead = in.read(buffer)) != -1) {
      byteCount += bytesRead;
    }
    return byteCount;
  }

  public static InputStream emptyInput() {
    return new ByteArrayInputStream(EMPTY_CONTENT);
  }

  public static InputStream getInputStreamFromRealPath(String realPath) {
    InputStream inputStream;
    try {
      Path path = Paths.get(realPath);
      inputStream = Files.newInputStream(path);
    } catch (IOException e) {
      throw new IllegalArgumentException("路径可能不对:" + realPath + " 无法生成输入流", e);
    }
    return inputStream;
  }

}
