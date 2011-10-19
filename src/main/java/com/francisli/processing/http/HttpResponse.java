package com.francisli.processing.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.stringtree.json.JSONReader;
import processing.xml.XMLElement;

/**
 *
 * @author francisli
 */
public class HttpResponse {
    org.apache.http.HttpResponse response;
    
    public int statusCode;
    public String statusMessage;    
    public String contentType;
    public byte[] content;
    public int contentLength;
    
    String contentCharSet;
        
    HttpResponse(org.apache.http.HttpResponse response) throws IOException {
        this.response = response;
        
        StatusLine status = response.getStatusLine();
        this.statusCode = status.getStatusCode();
        this.statusMessage = status.getReasonPhrase();
        
        HttpEntity entity = response.getEntity();
        contentType = entity.getContentType().getValue();
        content = EntityUtils.toByteArray(entity);
        contentLength = content.length;
        contentCharSet = EntityUtils.getContentCharSet(entity);
    }
    
    public String getContentAsString() {
        try {
            return new String(content, contentCharSet);
        } catch (UnsupportedEncodingException ex) {
            return new String(content);
        }
    }
    
    public Object getContentAsJSON() {
        JSONReader json = new JSONReader();
        return json.read(getContentAsString());
    }
    
    public XMLElement getContentAsXMLElement() {
        return XMLElement.parse(getContentAsString());
    }
}
