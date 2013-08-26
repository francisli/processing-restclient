/**
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, version 3.</p>
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.</p>
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 */
package com.francisli.processing.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.stringtree.json.JSONWriter;
import processing.core.*;

/** 
 * <p>The HttpClient class provides the interface for performing different types
 * of HTTP requests against a particular server. Instantiate a new HttpClient
 * object for each server you wish to communicate with.</p>
 * 
 * <p>Requests are performed in the background and responses are returned in a
 * callback function with the following signature:</p>
 * 
 * <pre>
 * void responseReceived(HttpRequest request, HttpResponse response) {
 * 
 * }
 * </pre>
 * 
 * <p>Because requests are performed in the background, it is important that
 * you have a draw() function defined (even if it is empty, as shown in the 
 * example above) and the animation loop is running.  Without a draw() function,
 * Processing will terminate the sketch after setup(), which will shutdown
 * all connections.  If the animation loop is not running, you will never get
 * a responseReceived() callback.  The library will invoke your responseReceived()
 * callback at the beginning of a frame, before your draw() function is called.
 * 
 * @example
 * import com.francisli.processing.http.*;
 * 
 * HttpClient client;
 * 
 * void setup() {
 *   client = new HttpClient(this, "api.twitter.com");
 *   client.GET("/1/statuses/public_timeline.json");
 * }
 * 
 * void responseReceived(HttpRequest request, HttpResponse response) {
 *   println(response.getContentAsString());
 * }
 * 
 * void draw() {
 *   
 * }
 * 
 * @author Francis Li
 * @usage Application
 * @param client HttpClient: any variable of type HttpClient
 */
public class HttpClient {
   
    PApplet parent;
    Method callbackMethod;
        
    DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
    HashMap<HttpRequest, HttpResponse> requestMap = new HashMap<HttpRequest, HttpResponse>();
    
    HttpHost host, secureHost;
    
    /** boolean: set true to use SSL encryption */
    public boolean useSSL;
    /** boolean: set false to turn off logging information in the console */
    public boolean logging;
    
    /** boolean: set true to sign requests using OAuth */
    public boolean useOAuth;
    /** String: the OAuth consumer key assigned to you for your app */
    public String oauthConsumerKey;
    /** String: the OAuth consumer secret assigned to you for your app */
    public String oauthConsumerSecret;    
    /** String: the OAuth access token for a user of your app */
    public String oauthAccessToken;
    /** String: the OAuth access token secret for a user of your app*/
    public String oauthAccessTokenSecret;
    
    public HttpClient(PApplet parent, String hostname) {
        this(parent, hostname, 80, 443);
    }
        
    public HttpClient(PApplet parent, String hostname, int port) {
        this(parent, hostname, port, 443);
    }
    
    /** Returns a new HttpClient instance that connects to the specified 
     * host and ports.
     * 
     * @param parent PApplet: typically use "this"
     * @param hostname String: the domain name or IP address of the host
     * @param port int: the port to use for unsecured connections (typically 80)
     * @param securePort int: The port to use for secure connections (typically 443)
     */
    public HttpClient(PApplet parent, String hostname, int port, int securePort) {
        this.parent = parent;
        parent.registerDispose(this);
        parent.registerPre(this);
        try {
            callbackMethod = parent.getClass().getMethod("responseReceived", new Class[] { HttpRequest.class, HttpResponse.class });
        } catch (Exception e) {
            System.err.println("HttpClient: No responseReceived callback method found in your sketch!");
        }        
        host = new HttpHost(hostname, port, "http");
        secureHost = new HttpHost(hostname, securePort, "https");
        logging = true;
    }    

    /**
     * @exclude
     */
    public void dispose() {
        httpClient.getConnectionManager().shutdown();
    }
    
    /**
     * @exclude
     */
    public void pre() throws Throwable {
        HashMap<HttpRequest, HttpResponse> requestMapClone;
        synchronized(this) {
            requestMapClone = (HashMap<HttpRequest, HttpResponse>)requestMap.clone();
        }
        for (HttpRequest request: requestMapClone.keySet()) {
            HttpResponse response = requestMapClone.get(request);
            try {
                callbackMethod.invoke(parent, new Object[] { request, response });
            } catch (IllegalAccessException ex) {

            } catch (IllegalArgumentException ex) {

            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
            synchronized(this) {
                requestMap.remove(request);
            }
        }
    }

    private void checkOAuth(String method, HttpRequestBase base) {
    	   if (useOAuth) {
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
            consumer.setTokenWithSecret(oauthAccessToken, oauthAccessTokenSecret);
            try {
                consumer.sign(base);
            } catch (Exception e) {
                System.err.println("HttpClient: Unable to sign "+method+" request for OAuth");
            }
        }
    }
        
    /** 
     * Performs a GET request to fetch content from the specified path.
     * 
     * @param path String: an absolute path to file or script on the server
     * @return HttpRequest
     */
    public HttpRequest GET(String path) {
        return GET(path, null);
    }
    
    /** 
     * Performs a GET request to fetch content from the specified path with
     * the specified parameters. The parameters are assembled into a query
     * string and appended to the path.
     * 
     * @param params String: a collection of parameters to pass as a query string with the path
     */
    public HttpRequest GET(String path, Map params) {
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
            String queryString = URLEncodedUtils.format(pairs, HTTP.UTF_8);
            if (path.contains("?")) {
                path = path + "&" + queryString;
            } else {
                path = path + "?" + queryString;
            }
        }
        //// finally, invoke request
        HttpGet get = new HttpGet(getHost().toURI() + path);
        checkOAuth("GET", get);
        HttpRequest request = new HttpRequest(this, getHost(), get);
        request.start();
        return request;
    }
    
    /**
     * Performs a POST request, sending the specified parameters as data 
     * in the same way a web browser submits a form.
     * 
     * @param path String: an absolute path to a file or script on the server
     * @param params HashMap: a collection of parameters to send to the server
     * @return HttpRequest
     */
    public HttpRequest POST(String path, Map params) {
        return POST(path, params, null);
    }

    public HttpRequest POST_JSON(String path, Map params) {
    	   //// clean up path a little bit- remove whitespace, add slash prefix
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        //// finally, invoke request
        HttpPost post = new HttpPost(getHost().toURI() + path);
        JSONWriter writer = new JSONWriter();
        post.setEntity(new StringEntity(writer.write(params), 
                             ContentType.create("application/json")));
        checkOAuth("POST JSON", post);
        HttpRequest request = new HttpRequest(this, getHost(), post);
        request.start();
        return request;
    }
    
    /**
     * @param files HashMap: a collection of files to send to the server
     */
    public HttpRequest POST(String path, Map params, Map files) {
        //// clean up path a little bit- remove whitespace, add slash prefix
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        //// finally, invoke request
        HttpPost post = new HttpPost(getHost().toURI() + path);
        MultipartEntity multipart = null;
        //// if files passed, set up a multipart request
        if (files != null) {
            multipart = new MultipartEntity();
            post.setEntity(multipart);
            for (Object key: files.keySet()) {
                Object value = files.get(key);
                if (value instanceof byte[]) {
                    multipart.addPart((String)key, new ByteArrayBody((byte[])value, "bytes.dat"));
                } else if (value instanceof String) {
                    File file = new File((String) value);
                    if (!file.exists()) {
                        file = parent.sketchFile((String) value);
                    }
                    multipart.addPart((String)key, new FileBody(file));
                }
            }
        }
        //// if params passed, format into a query string and append
        if (params != null) {
            if (multipart == null) {
                ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                for (Object key: params.keySet()) {
                    Object value = params.get(key);
                    pairs.add(new BasicNameValuePair(key.toString(), value.toString()));
                }
                String queryString = URLEncodedUtils.format(pairs, HTTP.UTF_8);
                if (path.contains("?")) {
                    path = path + "&" + queryString;
                } else {
                    path = path + "?" + queryString;
                }
                try {
                    post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("HttpClient: Unable to set POST data from parameters");
                }
            } else {
                for (Object key: params.keySet()) {
                    Object value = params.get(key);
                    try {
                        multipart.addPart((String)key, new StringBody((String) value));
                    } catch (UnsupportedEncodingException ex) {
                        System.err.println("HttpClient: Unable to add " + key + ", " + value);
                    }
                }
            }
        }
        checkOAuth("POST", post);
        HttpRequest request = new HttpRequest(this, getHost(), post);
        request.start();
        return request;
    }
    
    HttpHost getHost() {
        return useSSL ? secureHost : host;
    }
    
    void put(HttpRequest request, HttpResponse response) {
        synchronized(this) {
            requestMap.put(request, response);
        }
    }
    
    HttpResponse get(HttpRequest request) {
        synchronized(this) {
            return requestMap.get(request);
        }
    }
}
