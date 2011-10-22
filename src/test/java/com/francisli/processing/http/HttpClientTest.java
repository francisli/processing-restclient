package com.francisli.processing.http;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import processing.core.PApplet;

/**
 * Unit test for simple App.
 */
public class HttpClientTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HttpClientTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HttpClientTest.class );
    }

    /**
     * Executes a GET request. TODO: make it into an actual test.
     */
    public void asdftestGET()
    {
        PApplet stub = new PApplet();
        stub.init();
        
        HttpClient client = new HttpClient(stub, "api.twitter.com", 80);
        HttpRequest request = client.GET("/1/statuses/public_timeline.xml");
        for (int i = 0; i < 10; i++) {
            HttpResponse response = client.get(request);
            if (response != null) {
                System.out.println(response.statusCode);
                System.out.println(response.statusMessage);
                System.out.println(response.contentType);
                System.out.println(response.contentLength);
                System.out.println(response.contentCharSet);
                //System.out.println(response.getContentAsString());
                //System.out.println(response.getContentAsXMLElement().toString());
                assertTrue(true);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HttpClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(false);
    }

    /**
     * Executes a GET request. TODO: make it into an actual test.
     */
    public void asdftestGETSigned()
    {
        PApplet stub = new PApplet();
        stub.init();
        
        HttpClient client = new HttpClient(stub, "api.twitter.com", 80);
        client.useOAuth = true;
        client.oauthConsumerKey = "<insert key here>";
        client.oauthConsumerSecret = "<insert secret here>";
        client.oauthAccessToken = "<insert token here>";
        client.oauthAccessTokenSecret = "<insert token secret here>";
        HttpRequest request = client.GET("/1/statuses/home_timeline.xml");
        for (int i = 0; i < 10; i++) {
            HttpResponse response = client.get(request);
            if (response != null) {
                System.out.println(response.statusCode);
                System.out.println(response.statusMessage);
                System.out.println(response.contentType);
                System.out.println(response.contentLength);
                System.out.println(response.contentCharSet);
                //System.out.println(response.getContentAsString());
                //System.out.println(response.getContentAsXMLElement().toString());
                assertTrue(true);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HttpClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(false);
    }
    
    public void asdftestGETWithParams()
    {
        PApplet stub = new PApplet();
        stub.init();
        
        HttpClient client = new HttpClient(stub, "search.twitter.com", 80);
        HashMap params = new HashMap();
        params.put("q", "processing");
        HttpRequest request = client.GET("/search.json", params);
        for (int i = 0; i < 10; i++) {
            HttpResponse response = client.get(request);
            if (response != null) {
                System.out.println(response.statusCode);
                System.out.println(response.statusMessage);
                System.out.println(response.contentType);
                System.out.println(response.contentLength);
                System.out.println(response.contentCharSet);
                //System.out.println(response.getContentAsString());
                System.out.println(response.getContentAsJSONObject());
                assertTrue(true);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HttpClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(false);
    }
    
    public void testPOSTSigned()
    {
        PApplet stub = new PApplet();
        stub.init();
        
        HttpClient client = new HttpClient(stub, "upload.twitter.com");
        client.useSSL = true;
        client.useOAuth = true;
        client.oauthConsumerKey = "<insert key here>";
        client.oauthConsumerSecret = "<insert secret here>";
        client.oauthAccessToken = "<insert token here>";
        client.oauthAccessTokenSecret = "<insert token secret here>";
        HashMap params = new HashMap();
        params.put("status", "test library with image");
        HashMap files = new HashMap();
        files.put("media[]", "/Users/francisli/pic.png");
        HttpRequest request = client.POST("/1/statuses/update_with_media.json", params, files);
        for (int i = 0; i < 10; i++) {
            HttpResponse response = client.get(request);
            if (response != null) {
                System.out.println(response.statusCode);
                System.out.println(response.statusMessage);
                System.out.println(response.contentType);
                System.out.println(response.contentLength);
                System.out.println(response.contentCharSet);
                System.out.println(response.getContentAsString());
                //System.out.println(response.getContentAsXMLElement().toString());
                assertTrue(true);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HttpClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(false);
    }    
}
