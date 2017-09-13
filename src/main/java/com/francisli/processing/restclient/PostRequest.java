/**
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */
package com.francisli.processing.restclient;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

/**
 * <p>An HttpRequest object represents a single HTTP request to a server.  Each
 * request is dispatched in its own background thread and is passed along with
 * its response in the responseReceived() callback.</p>
 *
 * @author Francis Li
 * @usage Application
 * @param request HttpRequest: any variable of type HttpRequest
 */
public class PostRequest extends HttpRequest {
    public PostRequest(RESTClient client, String path) {
      this(client, path, null, null);
    }

    public PostRequest(RESTClient client, String path, Map params) {
      this(client, path, params, null);
    }

    public PostRequest(RESTClient client, String path, Map params, Map files) {
      super();
      this.client = client;
      this.host = client.getHost();
      //// clean up path a little bit- remove whitespace, add slash prefix
      path = path.trim();
      if (!path.startsWith("/")) {
          path = "/" + path;
      }
      //// finally, invoke request
      HttpEntityEnclosingRequestBase post = newRequest(this.host.toURI() + path);
      MultipartEntityBuilder builder = null;
      //// if files passed, set up a multipart request
      if (files != null) {
          builder = MultipartEntityBuilder.create();
          for (Object key: files.keySet()) {
              Object value = files.get(key);
              if (value instanceof byte[]) {
                  builder.addPart((String)key, new ByteArrayBody((byte[])value, "bytes.dat"));
              } else if (value instanceof String) {
                  File file = new File((String) value);
                  if (!file.exists()) {
                      file = client.parent.sketchFile((String) value);
                  }
                  builder.addPart((String)key, new FileBody(file));
              }
          }
      }
      if (params != null) {
          if (builder == null) {
              ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
              for (Object key: params.keySet()) {
                  Object value = params.get(key);
                  pairs.add(new BasicNameValuePair(key.toString(), value.toString()));
              }
              post.setEntity(new UrlEncodedFormEntity(pairs, Consts.UTF_8));
          } else {
              for (Object key: params.keySet()) {
                  Object value = params.get(key);
                  builder.addTextBody((String) key, (String) value);
              }
          }
      }
      if (builder != null) {
        post.setEntity(builder.build());
      }
      if (client.useOAuth) {
          OAuthConsumer consumer = new CommonsHttpOAuthConsumer(client.oauthConsumerKey, client.oauthConsumerSecret);
          consumer.setTokenWithSecret(client.oauthAccessToken, client.oauthAccessTokenSecret);
          try {
              consumer.sign(post);
          } catch (Exception e) {
              System.err.println("HttpClient: Unable to sign POST request for OAuth");
          }
      }
      start();
    }

    HttpEntityEnclosingRequestBase newRequest(String uri) {
      return new HttpPost(uri);
    }
}
