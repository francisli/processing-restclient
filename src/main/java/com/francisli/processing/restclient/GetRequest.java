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

import java.util.ArrayList;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
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
public class GetRequest extends HttpRequest {
    public GetRequest(RESTClient client, String path) {
      this(client, path, null);
    }

    public GetRequest(RESTClient client, String path, Map params) {
      super();
      this.client = client;
      this.host = client.getHost();
      //// clean up path a little bit- remove whitespace, add slash prefix
      path = path.trim();
      if (!path.startsWith("/")) {
          path = "/" + path;
      }
      //// if params passed, format into a query string and append
      if (params != null) {
          ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
          for (Object key: params.keySet()) {
              Object value = params.get(key);
              pairs.add(new BasicNameValuePair(key.toString(), value.toString()));
          }
          String queryString = URLEncodedUtils.format(pairs, Consts.UTF_8);
          if (path.contains("?")) {
              path = path + "&" + queryString;
          } else {
              path = path + "?" + queryString;
          }
      }
      //// finally, invoke request
      this.request = newRequest(host.toURI() + path);
      if (client.useOAuth) {
          OAuthConsumer consumer = new CommonsHttpOAuthConsumer(client.oauthConsumerKey, client.oauthConsumerSecret);
          consumer.setTokenWithSecret(client.oauthAccessToken, client.oauthAccessTokenSecret);
          try {
              consumer.sign(request);
          } catch (Exception e) {
              System.err.println("HttpClient: Unable to sign GET request for OAuth");
          }
      }
    }

    org.apache.http.HttpRequest newRequest(String uri) {
      return new HttpGet(uri);
    }
}
