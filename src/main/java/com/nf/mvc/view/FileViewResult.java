package com.nf.mvc.view;


import com.nf.mvc.util.FileUtils;
import com.nf.mvc.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.nf.mvc.util.FileUtils.getMediaType;

/**
 * 此类是一个响应文件数据给前端的ViewResult，通常用在文件下载以及显示图片等场景，
 * <h3>基本功能</h3>
 * <p>本类支持两类用法:
 * <ul>
 *     <li>实例化时参数指定文件的物理路径,构造函数是:{@link FileViewResult#FileViewResult(String)}与{@link FileViewResult#FileViewResult(String, Map)}</li>
 *     <li>实例化时指定要下载文件的流与文件名,构造函数是:{@link FileViewResult#FileViewResult(InputStream, String)}与{@link FileViewResult#FileViewResult(InputStream, String, Map)}</li>
 * </ul>
 * </p>
 * <h3>典型用法</h3>
 * <p>通常会写一个控制器，通过响应此类型的方式来响应一个文件数据给请求端，比如:
 * <pre class="code">
 *    &#064RequestMapping("/download")
 *     public ViewResult download(String filename) {
 *         String realPath = "E:/image/"+filename;
 *         return file(realPath);
 *     }
 * </pre>
 * </p>
 * 或者用它来显示图片，比如下面的代码:
 * <pre class="code">
 *     <img src="http://localhost:8080/file/download?filename=a.jpg"/>
 * </pre>
 * </p>
 *
 * @see StreamViewResult
 * @see com.nf.mvc.ViewResult
 * @see com.nf.mvc.handler.HandlerHelper
 */
public class FileViewResult extends StreamViewResult {

  private final String filename;

  public FileViewResult(String realPath) {
    this(realPath, new HashMap<>());
  }

  /**
   * 通过文件物理路径实例化视图结果
   *
   * @param realPath:物理路径,比如D:/downloads/1.jpg
   * @param headers：响应头信息
   */
  public FileViewResult(String realPath, Map<String, String> headers) {
    super(StreamUtils.getInputStreamFromRealPath(realPath), headers);
    // this.realPath = realPath;
    this.filename = FileUtils.getFilename(realPath);
  }

  public FileViewResult(InputStream inputStream, String filename) {
    this(inputStream, filename, new HashMap<>());
  }

  /**
   * 通过文件流方式实例化视图结果
   *
   * @param inputStream:要下载文件的输入流
   * @param filename:下载文件的名字，要有扩展名，比如a.jpg
   * @param headers:响应头信息
   */
  public FileViewResult(InputStream inputStream, String filename, Map<String, String> headers) {
    super(inputStream, headers);
    this.filename = filename;
  }

  @SuppressWarnings("RedundantThrows")
  @Override
  protected void writeContentType(HttpServletResponse resp) throws Exception {
    resp.setContentType(getMediaType(this.filename));
  }

  @Override
  protected void writeHeaders(HttpServletResponse resp) throws Exception {
    // attachment表示以附件的形式下载，对文件名编码以防止中文文件名在保存对话框中是乱码的
    resp.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
    super.writeHeaders(resp);
  }

}

