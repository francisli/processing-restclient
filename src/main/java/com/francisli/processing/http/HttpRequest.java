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
 * <p>An HttpRequest object represents a single HTTP request to a server.  Each
 * request is dispatched in its own background thread and is passed along with
 * its response in the responseReceived() callback.</p>
 * 
 * @author Francis Li
 * @usage Application
 * @param request HttpRequest: any variable of type HttpRequest
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
    
    /**
     * @exclude
     */
    @Override
    public void run() {
        try {
            if (client.logging) {
                PApplet.println("HttpClient: Connecting to " + host.getHostName() + " on port " + host.getPort());
                PApplet.println("HttpClient: " + request.getRequestLine().toString());
            }
            org.apache.http.HttpResponse httpResponse = client.httpClient.execute(host, request);
            client.put(this, new HttpResponse(httpResponse));
        } catch (Exception e) {
            System.err.println("HttpClient: An error occurred- ");
            e.printStackTrace();;
        }        
    }
}
