package com.nf.mvc.file;

import com.nf.mvc.util.FileUtils;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StandardMultipartFile implements MultipartFile {
  private final Part part;

  private final String filename;

  public StandardMultipartFile(Part part, String filename) {
    this.part = part;
    this.filename = filename;
  }

  @Override
  public String getName() {
    return this.part.getName();
  }

  @Override
  public String getOriginalFilename() {
    return this.filename;
  }

  @Override
  public String getContentType() {
    return this.part.getContentType();
  }

  @Override
  public boolean isEmpty() {
    return (this.part.getSize() == 0);
  }

  @Override
  public long getSize() {
    return this.part.getSize();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return FileUtils.copyToByteArray(this.part.getInputStream());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return this.part.getInputStream();
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    this.part.write(dest.getPath());
    if (dest.isAbsolute() && !dest.exists()) {
      // Servlet 3.0 Part.write is not guaranteed to support absolute file paths:
      // may translate the given path to a relative location within a temp dir
      // (e.g. on Jetty whereas Tomcat and Undertow detect absolute paths).
      // At least we offloaded the file from memory storage; it'll get deleted
      // from the temp dir eventually in any case. And for our user's purposes,
      // we can manually copy it to the requested location as a fallback.
      FileUtils.copy(this.part.getInputStream(), Files.newOutputStream(dest.toPath()));
    }
  }

  @Override
  public void transferTo(Path dest) throws IOException, IllegalStateException {
    FileUtils.copy(this.part.getInputStream(), Files.newOutputStream(dest));
  }
}
