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
        PApplet stub = new PApplet();

        RESTClient client = new RESTClient(stub, "api.twitter.com", 80);
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
                Logger.getLogger(RESTClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(false);
    }
}
