package com.francisli.processing.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import processing.core.*;

/**
 *
 * @author francisli
 */
public class HttpClient {
   
    PApplet parent;
    Method callbackMethod;
        
    DefaultHttpClient httpClient = new DefaultHttpClient();
    HashMap<HttpRequest, HttpResponse> requestMap = new HashMap<HttpRequest, HttpResponse>();
    
    HttpHost host, secureHost;
    
    public boolean useSSL;
    public boolean logging;
    
    public HttpClient(PApplet parent, String hostname, int port, int securePort) {
        this.parent = parent;
        parent.registerDispose(this);
        parent.registerDraw(this);
        try {
            callbackMethod = parent.getClass().getMethod("responseReceived", new Class[] { HttpRequest.class, HttpResponse.class });
        } catch (Exception e) {
            System.err.println("HttpClient: No responseReceived callback method found in your sketch!");
        }        
        host = new HttpHost(hostname, port, "http");
        secureHost = new HttpHost(hostname, securePort, "https");
        logging = true;
    }
    
    public HttpClient(PApplet parent, String hostname, int port) {
        this(parent, hostname, port, 443);
    }
    
    public HttpClient(PApplet parent, String hostname) {
        this(parent, hostname, 80, 443);
    }
    
    public void dispose() {
        httpClient.getConnectionManager().shutdown();
    }
    
    public void draw() {
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
                throw new RuntimeException(e);
            }
        }
    }
        
    public HttpRequest GET(String file) {
        file = file.trim();
        if (!file.startsWith("/")) {
            file = "/" + file;
        }
        HttpGet get = new HttpGet(file);
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
