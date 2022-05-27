package com.MeehanMetaSpace;

import java.net.URLEncoder;

/**
 * <p>Title: FacsXpert Server</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Credentials {
  public final String login, password;
  public final Boolean notifyUpdates;
  public Credentials(
      final String login,
      final String password,
      final Boolean notifyUpdates) {
    this.login = login;
    this.password = password;
    this.notifyUpdates = notifyUpdates;
  }

  public Credentials(
      final String login,
      final String password,
      final String notifyUpdates) {
    this.login = login;
    this.password = password;
    this.notifyUpdates =
        notifyUpdates == null ?
        Boolean.FALSE :
        Boolean.valueOf(notifyUpdates);
  }

  public final static String
      HTTP_PARAMETER_NOTIFY_UPDATES = "notifyUpdates",
      HTTP_PARAMETER_PASSWORD = "password",
      HTTP_PARAMETER_LOGIN = "login"; // for parameter names
  public String toUrlString() {
    return HTTP_PARAMETER_PASSWORD +
        "=" +
        (password == null ? "null" : URLEncoder.encode(password)) +
        "&" +
        HTTP_PARAMETER_LOGIN +
        "=" +
        (login == null ? "null" : URLEncoder.encode(login)) +
        "&" +
        HTTP_PARAMETER_NOTIFY_UPDATES +
        "=" +
        (notifyUpdates == null ? "null" : notifyUpdates.toString())
        ;
  }
}
