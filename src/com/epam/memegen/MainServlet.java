package com.epam.memegen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(MainServlet.class.getName());
  private final Util util = new Util();
  private final MemeDao memeDao = new MemeDao();

  private String welcomeFileContent;

  @Override
  public void init() throws ServletException {
    try {
      readFile();
    } catch (FileNotFoundException e) {
      throw new ServletException(e);
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (SystemProperty.environment.value() == Environment.Value.Development) {
      readFile();
    }
    String uploadUrl = util.createUploadUrl();
    String allMemesJson = memeDao.getAllAsJson(req);
    String replaced = welcomeFileContent.replace("###UPLOAD_URL###", uploadUrl);
    replaced = replaced.replace("###MEMES_JSON###", allMemesJson);
    resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(replaced);
  }

  private void readFile() throws FileNotFoundException, IOException {
    FileInputStream fr = new FileInputStream("index.html");
    welcomeFileContent = IOUtils.toString(fr, Charset.forName("UTF-8"));
  }
}
