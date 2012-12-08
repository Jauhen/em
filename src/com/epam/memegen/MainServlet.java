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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(MainServlet.class.getName());
  private final Util util = new Util();
  private final MemeDao memeDao = new MemeDao();
  private final UserService userService = UserServiceFactory.getUserService();

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


    // Check authentication.
    // If not logged in, send him to login url.
    // If logged in, but email doesn't end on @epam.com, send to google.com/a/epam.com.
    // We shouldn't send to logout url, because it logs user out of internal services.
    boolean userAuthenticated = util.isAuthenticated();
    replaced = replaced.replace("###IS_AUTHENTICATED###", "" + userAuthenticated);

    if (!userAuthenticated) {
      String msg = "";
      if (userService.isUserLoggedIn()) {
        msg = ", but your email is <b>" + userService.getCurrentUser().getEmail() + "</b>";
      }
      replaced = replaced.replace("###WRONG_EMAIL_MSG###", msg);

      String returnUrl = req.getRequestURL().toString();
      String loginUrl = userService.createLoginURL(returnUrl);

      if (userService.isUserLoggedIn()) {
        loginUrl = "http://google.com/a/epam.com/";
      }

      replaced = replaced.replace("###LOGIN_URL###", loginUrl);
    }

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
