package com.francisli.processing.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpHost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import processing.core.*;

/** 
 * The HttpClient class provides the interface for performing different types
 * of HTTP requests against a particular server. Instantiate a new HttpClient
 * object for each server you wish to communicate with.
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
 * <p>This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, version 3.</p>
 * 
 * <p>This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.</p>
 * 
 * <p>You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA</p>
 * 
 * @author Francis Li <mail@francisli.com>
 */
public class HttpClient {
   
    PApplet parent;
    Method callbackMethod;
        
    DefaultHttpClient httpClient = new DefaultHttpClient();
    HashMap<HttpRequest, HttpResponse> requestMap = new HashMap<HttpRequest, HttpResponse>();
    
    HttpHost host, secureHost;
    
    /** Set to true if you want the next request to use SSL encryption. */
    public boolean useSSL;
    /** Set to false if you want to turn off logging information in the console. */
    public boolean logging;
    
    /** Set to true if you want the next request to be signed using OAuth. */
    public boolean useOAuth;
    /** For OAuth signing, the consumer key assigned to you by the service provider for your app. */
    public String oauthConsumerKey;
    /** For OAuth signing, the consumer secret assigned to you by the service provider for your app. */
    public String oauthConsumerSecret;    
    /** For OAuth signing, the access token for the user authorized to use your app. */
    public String oauthAccessToken;
    /** For OAuth signing, the access token secret for the user authorized to use your app. */
    public String oauthAccessTokenSecret;
    
    /** Returns a new HttpClient instance that connects to the specified 
     * host and ports.
     * 
     * @param parent PApplet of the sketch (typically use "this")
     * @param hostname The domain name or IP address of the host
     * @param port The port to connect to for unsecured connections (typically 80)
     * @param securePort The port to connect to for secure connections (typically 443)
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
    
    /** Returns a new HttpClient instance that connects to the specified 
     * host and port.  The default secure SSL port is assumed.
     * 
     * @param parent PApplet of the sketch (typically use "this")
     * @param hostname The domain name or IP address of the host
     * @param port The port to connect to for unsecured connections (typically 80)
     */
    public HttpClient(PApplet parent, String hostname, int port) {
        this(parent, hostname, port, 443);
    }
    
    /** Returns a new HttpClient instance that connects to the specified 
     * host on default ports.
     * 
     * @param parent PApplet of the sketch (typically use "this")
     * @param hostname The domain name or IP address of the host
     */
    public HttpClient(PApplet parent, String hostname) {
        this(parent, hostname, 80, 443);
    }
    
    public void dispose() {
        httpClient.getConnectionManager().shutdown();
    }
    
    public void pre() {
        HashMap<HttpRequest, HttpResponse> requestMapClone;
        synchronized(this) {
            requestMapClone = (HashMap<HttpRequest, HttpResponse>)requestMap.clone();
        }
        for (HttpRequest request: requestMapClone.keySet()) {
            HttpResponse response = requestMapClone.get(request);
            try {
                callbackMethod.invoke(parent, new Object[] { request, response });
                synchronized(this) {
                    requestMap.remove(request);
                }
            } catch (Exception e) {
                callbackMethod = null;
                System.err.println("HttpClient: An error occurred in your responseRecieved() callback function");
            }
        }
    }
        
    /** 
     * Performs a GET request to fetch content from the specified path.
     * 
     * @param path An absolute path to file or script on the server
     * @return HttpRequest object representing this request
     */
    public HttpRequest GET(String path) {
        return GET(path, null);
    }
    
    /** 
     * Performs a GET request to fetch content from the specified path with
     * the specified parameters. The parameters are assembled into a query
     * string and appended to the path.
     * 
     * @param path An absolute path to file or script on the server
     * @param params A collection of parameters to pass as a query string with the path
     * @return HttpRequest object representing this request
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
        if (useOAuth) {
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
            consumer.setTokenWithSecret(oauthAccessToken, oauthAccessTokenSecret);
            try {
                consumer.sign(get);
            } catch (Exception e) {
                System.err.println("HttpClient: Unable to sign GET request for OAuth");
            }
        }
        HttpRequest request = new HttpRequest(this, getHost(), get);
        request.start();
        return request;
    }
    
    /**
     * Performs a POST request, sending the specified parameters as data 
     * in the same way a web browser submits a form.
     * 
     * @param path An absolute path to a file or script on the server
     * @param params A collection of parameters to send to the server
     * @return HttpRequest object representing this request
     */
    public HttpRequest POST(String path, Map params) {
        return POST(path, params, null);
    }
    
    /**
     * Performs a POST request, sending the specified parameters and files
     * as data in the same way a web browser submits a form.
     * 
     * @param path An absolute path to a file or script on the server
     * @param params A collection of parameters to send to the server
     * @param files A collection of files to send to the server
     * @return 
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
                    multipart.addPart((String)key, new FileBody(new File((String) value)));
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
        if (useOAuth) {
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
            consumer.setTokenWithSecret(oauthAccessToken, oauthAccessTokenSecret);
            try {
                consumer.sign(post);
            } catch (Exception e) {
                System.err.println("HttpClient: Unable to sign POST request for OAuth");
            }
        }
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
