package com.mojang.android.net;

import android.text.TextUtils;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HTTPRequest {
    public static boolean debugNet = false;
    public String contentType;
    public String cookieData;
    public String requestBody;
    public String url;

    public void setURL(String url) {
        if (debugNet) {
            System.out.println("URL: " + url);
        }
        this.url = url;
    }

    public void setRequestBody(String body) {
        if (debugNet) {
            System.out.println("Body: " + body);
        }
        this.requestBody = body;
    }

    public void setCookieData(String cookie) {
        if (debugNet) {
            System.out.println("Cookie: " + cookie);
        }
        this.cookieData = cookie;
    }

    public void setContentType(String contentType) {
        if (debugNet) {
            System.out.println("Content type: " + contentType);
        }
        this.contentType = contentType;
    }

    public HTTPResponse send(String mode) {
        if (debugNet) {
            System.out.println("Send: " + mode);
        }
        InputStream is = null;
        int status = 0;
        HTTPResponse hTTPResponse;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(this.url).openConnection();
            //conn.setRequestProperty("User-Agent", KamcordConstants.GAME_NAME);
            conn.setRequestProperty("Cookie", this.cookieData);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", this.contentType);
            conn.setRequestMethod(mode);
            conn.connect();
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(this.requestBody);
            writer.close();
            try {
                status = conn.getResponseCode();
                is = conn.getInputStream();
            } catch (Exception e) {
                is = conn.getErrorStream();
            }
            if (is == null) {
                throw new Exception("Null input stream");
            }
            ByteArrayOutputStream realos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (true) {
                int count = is.read(buffer);
                if (count == -1) {
                    break;
                }
                realos.write(buffer, 0, count);
            }
            hTTPResponse = new HTTPResponse(1, status, new String(realos.toByteArray(), "UTF-8"), toApacheHeaders(conn));
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e2) {
                }
            }
            return hTTPResponse;
        } catch (Exception e3) {
            e3.printStackTrace();
            hTTPResponse = new HTTPResponse(0, 0, null, new Header[0]);
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e4) {
                }
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e5) {
                }
            }
        }
		return null;
    }

    public void abort() {
        if (debugNet) {
            System.out.println("Abort");
        }
    }

    private static Header[] toApacheHeaders(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        Header[] headersOut = new Header[(headers.containsKey(null) ? headers.size() - 1 : headers.size())];
        int headerIndex = 0;
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null) {
                int headerIndex2 = headerIndex + 1;
                headersOut[headerIndex] = new BasicHeader((String) entry.getKey(), TextUtils.join(",", (Iterable) entry.getValue()));
                headerIndex = headerIndex2;
            }
        }
        return headersOut;
    }
}
