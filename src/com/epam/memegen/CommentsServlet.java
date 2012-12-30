package com.epam.memegen;

import com.epam.memegen.model.Comment;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.epam.memegen.Util.isNullOrEmpty;

/**
 * @author amormysh@gmail.com (Andrey Mormysh)
 */
public class CommentsServlet extends HttpServlet {

  private UserService userService = UserServiceFactory.getUserService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    String memeId = req.getParameter("id");
    if (memeId != null) {
      List<Comment> comments = getComments(Long.parseLong(memeId));
      resp.getWriter().write(new Gson().toJson(comments));
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    JsonElement jsonElement = new JsonParser().parse(req.getReader());
    Comment comment = new Gson().fromJson(jsonElement, Comment.class);
    if (isNullOrEmpty(comment.getText())) {
      return;
    }
    String user = userService.getCurrentUser().getEmail();
    comment.setAuthor(user);
    Date date = new Date();
    comment.setTimestamp(date.getTime());

    Entity entity = new Entity(Comment.KIND, KeyFactory.createKey(MemeDao.KIND, comment.getMemeId()));
    entity.setProperty(Comment.MEME_ID, comment.getMemeId());
    entity.setProperty(Comment.TEXT, comment.getText());
    entity.setProperty(Comment.DATE, date);
    entity.setProperty(Comment.AUTHOR, comment.getAuthor());
    // TODO(lazerka): For backwards compatibility, for versions 20 and below. Remove when not needed anymore.
    entity.setProperty("user", comment.getAuthor());
    entity.setProperty("timestamp", date.getTime());

    datastore.put(entity);
    resp.getWriter().write(new Gson().toJson(comment));
  }

  private List<Comment> getComments(long memeId) {
    Query query = new Query(Comment.KIND, KeyFactory.createKey(MemeDao.KIND, memeId))
        .addSort(Comment.DATE);
    List<Comment> comments = Lists.newArrayList();
    PreparedQuery preparedQuery = datastore.prepare(query);
    for (Entity entity : preparedQuery.asIterable()) {
      Comment comment = fromEntity(entity);
      comments.add(comment);
    }

    return comments;
  }

  private Comment fromEntity(Entity entity) {
    long memeId = (Long) entity.getProperty(Comment.MEME_ID);
    String text = (String) entity.getProperty(Comment.TEXT);
    Date date = (Date) entity.getProperty(Comment.DATE);
    String author = (String) entity.getProperty(Comment.AUTHOR);
    return new Comment(memeId, text, date.getTime(), author);
  }
}
