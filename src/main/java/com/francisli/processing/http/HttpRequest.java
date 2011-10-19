package com.francisli.processing.http;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import processing.core.PApplet;

/**
 *
 * @author francisli
 */
public class HttpRequest extends Thread {
    HttpClient client;
    HttpHost host;
    org.apache.http.HttpRequest request;
    
    HttpRequest(HttpClient client, HttpHost host, org.apache.http.HttpRequest request) {
        this.client = client;
        this.host = host;
        this.request = request;
    }
    
    @Override
    public void run() {
        try {
            if (client.logging) {
                PApplet.println("HttpClient: Connecting to " + host.getHostName() + " on port " + host.getPort());
            }
            org.apache.http.HttpResponse httpResponse = client.httpClient.execute(host, request);
            client.put(this, new HttpResponse(httpResponse));
        } catch (Exception e) {
            System.err.println("HttpClient: An error occurred- ");
            e.printStackTrace();;
        }        
    }
}
