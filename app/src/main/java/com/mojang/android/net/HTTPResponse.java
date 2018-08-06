package com.mojang.android.net;

import org.apache.http.Header;

public class HTTPResponse {
    public static final int STATUS_FAIL = 0;
    public static final int STATUS_SUCCESS = 1;
    private String body;
    private Header[] headers;
    private int responseCode;
    private int status;

    public HTTPResponse(int status, int responseCode, String body, Header[] headers) {
        this.status = status;
        this.responseCode = responseCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatus() {
        if (HTTPRequest.debugNet) {
            System.out.println("get status");
        }
        return this.status;
    }

    public String getBody() {
        if (HTTPRequest.debugNet) {
            System.out.println("get response " + this.body);
        }
        return this.body;
    }

    public int getResponseCode() {
        if (HTTPRequest.debugNet) {
            System.out.println("get response code");
        }
        return this.responseCode;
    }

    public Header[] getHeaders() {
        if (HTTPRequest.debugNet) {
            System.out.println("get headers");
        }
        return this.headers;
    }
}
