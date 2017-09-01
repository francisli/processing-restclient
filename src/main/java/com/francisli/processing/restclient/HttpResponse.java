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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import processing.data.JSONObject;
import processing.data.XML;

/** 
 * An HttpResponse object contains both status information and the content
 * of the response to an HTTP request.
 *
 * @author Francis Li <mail@francisli.com>
 * @usage Application
 */
public class HttpResponse {
    org.apache.http.HttpResponse response;
    HttpEntity entity;

    /** int: the HTTP status code of this response */
    public int statusCode;
    /** String: a short descriptive message for the current status */
    public String statusMessage;    
    /** String: the MIME type of the content, including optional character set */
    public String contentType;
    /** int: the length of the content data */
    public long contentLength;



    HttpResponse(org.apache.http.HttpResponse response) throws IOException {
        this.response = response;
        
        StatusLine status = response.getStatusLine();
        this.statusCode = status.getStatusCode();
        this.statusMessage = status.getReasonPhrase();
        
        entity = response.getEntity();
        Header contentType = entity.getContentType();
        if (contentType != null) {
            this.contentType = contentType.getValue();
        }
        contentLength = entity.getContentLength();
    }

    public String getHeader(String name) {
        String value = null;
        Header header = response.getFirstHeader(name);
        if (header != null) {
            value = header.getValue();
        }
        return value;
    }

    public String[] getHeaders(String name) {
        Header[] headers = response.getHeaders(name);
        String[] values = new String[headers.length];
        for (int i = 0, length = headers.length; i < length; i++) {
            values[i] = headers[i].getValue();
        }
        return values;
    }

    HashMap<String, String> links;
    Pattern linkPattern = Pattern.compile("<([^>]+)>; rel=\"([^\"]+)\"");

    void parseLinkHeader() {
        if (links == null) {
            links = new HashMap<String, String>();
            String link = getHeader("Link");
            if (link != null) {
                Matcher m = linkPattern.matcher(link);
                while (m.find()) {
                    links.put(m.group(2), m.group(1));
                }
            }
        }
    }

    public String getNextHeaderLink() {
        parseLinkHeader();
        return links.get("next");
    }

    public String getPrevHeaderLink() {
        parseLinkHeader();
        return links.get("prev");
    }

    public String getFirstHeaderLink() {
        parseLinkHeader();
        return links.get("first");
    }

    public String getLastHeaderLink() {
        parseLinkHeader();
        return links.get("last");
    }

    /** Returns the underlying InputStream for the data returned in this response
     *
     * @return InputStream
     */
    public InputStream getContent() {
        try {
            return entity.getContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 
     * Converts the content into a String.
     * 
     * @return String
     */
    public String getContentAsString() {
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * Parses the data as a JSON document and returns it as a JSONObject.
     * 
     * @return JSONObject
     */
    public JSONObject getContentAsJSONObject() {
        return JSONObject.parse(getContentAsString());
    }
    
    /** 
     * Parses the data as an XML document and returns an XML object.
     * 
     * @return XML
     */
    public XML getContentAsXML() {
        try {
            return XML.parse(getContentAsString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
