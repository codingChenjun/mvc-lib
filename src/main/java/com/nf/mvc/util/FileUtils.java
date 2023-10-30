package com.nf.mvc.util;

import com.nf.mvc.support.Assert;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;

public abstract class FileUtils {

  public static final int BUFFER_SIZE = StreamUtils.BUFFER_SIZE;
  private static final char EXTENSION_SEPARATOR = '.';
  private static final String FOLDER_SEPARATOR = "/";

  private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

  private static final String TOP_PATH = "..";

  private static final String CURRENT_PATH = ".";

  //---------------------------------------------------------------------
  // Copy methods for java.io.File
  //---------------------------------------------------------------------

  public static int copy(File in, File out) throws IOException {
    Assert.notNull(in, "No input File specified");
    Assert.notNull(out, "No output File specified");
    return copy(Files.newInputStream(in.toPath()), Files.newOutputStream(out.toPath()));
  }


  public static void copy(byte[] in, File out) throws IOException {
    Assert.notNull(in, "No input byte array specified");
    Assert.notNull(out, "No output File specified");
    copy(new ByteArrayInputStream(in), Files.newOutputStream(out.toPath()));
  }

  public static byte[] copyToByteArray(File in) throws IOException {
    Assert.notNull(in, "No input File specified");
    return copyToByteArray(Files.newInputStream(in.toPath()));
  }


  //---------------------------------------------------------------------
  // Copy methods for java.io.InputStream / java.io.OutputStream
  //---------------------------------------------------------------------

  public static int copy(InputStream in, OutputStream out) throws IOException {
    Assert.notNull(in, "No InputStream specified");
    Assert.notNull(out, "No OutputStream specified");

    try {
      return StreamUtils.copy(in, out);
    } finally {
      close(in);
      close(out);
    }
  }

  public static void copy(byte[] in, OutputStream out) throws IOException {
    Assert.notNull(in, "No input byte array specified");
    Assert.notNull(out, "No OutputStream specified");

    try {
      out.write(in);
    } finally {
      close(out);
    }
  }

  public static byte[] copyToByteArray(InputStream in) throws IOException {
    if (in == null) {
      return new byte[0];
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
    copy(in, out);
    return out.toByteArray();
  }


  //---------------------------------------------------------------------
  // Copy methods for java.io.Reader / java.io.Writer
  //---------------------------------------------------------------------

  public static int copy(Reader in, Writer out) throws IOException {
    Assert.notNull(in, "No Reader specified");
    Assert.notNull(out, "No Writer specified");

    try {
      int charCount = 0;
      char[] buffer = new char[BUFFER_SIZE];
      int charsRead;
      while ((charsRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, charsRead);
        charCount += charsRead;
      }
      out.flush();
      return charCount;
    } finally {
      close(in);
      close(out);
    }
  }


  public static void copy(String in, Writer out) throws IOException {
    Assert.notNull(in, "No input String specified");
    Assert.notNull(out, "No Writer specified");

    try {
      out.write(in);
    } finally {
      close(out);
    }
  }

  public static String copyToString(Reader in) throws IOException {
    if (in == null) {
      return "";
    }

    StringWriter out = new StringWriter(BUFFER_SIZE);
    copy(in, out);
    return out.toString();
  }

  private static void close(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException ex) {
      // ignore
    }
  }

  public static String getMediaType(String filename) {
    // guessContentTypeFromName是从文件名猜测其内容类型，如果为null就猜测失败
    String mediaType = URLConnection.guessContentTypeFromName(filename);
    if (mediaType == null) {
      mediaType = StreamUtils.APPLICATION_OCTET_STREAM_VALUE;
    }
    return mediaType;
  }

  public static String getFilename(String path) {
    if (path == null) {
      return null;
    }

    int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
    return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
  }

  /**
   * Extract the filename extension from the given Java resource path,
   * e.g. "path/file.txt" -> "txt".
   *
   * @param path the file path (maybe {@code null})
   * @return the extracted filename extension, or {@code null} if none
   */
  public static String getFilenameExtension(String path) {
    if (path == null) {
      return null;
    }

    int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
    if (extIndex == -1) {
      return null;
    }

    int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
    if (folderIndex > extIndex) {
      return null;
    }

    return path.substring(extIndex + 1);
  }
}