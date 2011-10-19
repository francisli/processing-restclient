package com.francisli.processing.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
                throw new RuntimeException(e);
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
            String queryString = URLEncodedUtils.format(pairs, "UTF-8");
            if (path.contains("?")) {
                path = path + "&" + queryString;
            } else {
                path = path + "?" + queryString;
            }
        }
        //// finally, invoke request
        HttpGet get = new HttpGet(path);
        HttpRequest request = new HttpRequest(this, getHost(), get);        
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
