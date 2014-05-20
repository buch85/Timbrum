package it.buch85.timbrum.request;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * Created by mbacer on 23/04/14.
 */
public class AbstractRequest {

    protected HttpClient httpclient;
    protected String              url;
    protected HttpContext context;

    public AbstractRequest(HttpClient httpclient, HttpContext context) {
        this.httpclient = httpclient;
		this.context = context;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
