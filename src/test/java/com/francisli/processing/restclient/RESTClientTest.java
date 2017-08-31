package com.francisli.processing.restclient;

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
public class RESTClientTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RESTClientTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( RESTClientTest.class );
    }

    /**
     * Executes a GET request. TODO: make it into an actual test.
     */
    public void testGET()
    {
        PApplet stub = new PApplet() {
            public void responseReceived(HttpRequest request, HttpResponse response) { }
        };

        RESTClient client = new RESTClient(stub, "www.googleapis.com");
        client.useSSL = true;
        HttpRequest request = client.GET("/books/v1/volumes?q=isbn:0747532699");
        for (int i = 0; i < 10; i++) {
            HttpResponse response = client.get(request);
            if (response != null) {
                assertEquals(200, response.statusCode);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RESTClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
