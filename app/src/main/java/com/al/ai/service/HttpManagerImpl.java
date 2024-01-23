package com.al.ai.service;

import static com.al.ai.util.Constants.HTTPS;
import static com.al.ai.util.Constants.TLS;

import android.content.Context;

import com.al.ai.R;
import com.al.ai.model.HttpMethod;
import com.al.ai.model.HttpRequest;
import com.al.ai.model.HttpResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class HttpManagerImpl implements HttpManager {
    private final Context context;

    public HttpManagerImpl(Context context) {
        this.context = context;
    }

    @Override
    public HttpResult sendGetRequest(HttpRequest request) {
        return sendRequest(request, HttpMethod.GET);
    }

    @Override
    public HttpResult sendPostRequest(HttpRequest request) {
        return sendRequest(request, HttpMethod.POST);
    }

    private HttpResult sendRequest(HttpRequest request, HttpMethod method) {
        URL url;
        HttpURLConnection conn;
        try {
            url = new URL(request.url());
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return new HttpResult(0, context.getString(R.string.error) + e);
        }

        if (request.url().startsWith(HTTPS)) {
            try {
                SSLContext sc;
                sc = SSLContext.getInstance(TLS);
                sc.init(null, null, new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                return new HttpResult(0, context.getString(R.string.error) + e);
            }
        }
        try {
            conn.setReadTimeout(request.timeOut());
            conn.setConnectTimeout(request.timeOut());
            conn.setDoOutput(true);
            request.headers().forEach(conn::setRequestProperty);
            conn.setRequestMethod(method.name());
            if (request.data() != null) {
                conn.setFixedLengthStreamingMode(request.data().getBytes().length);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(request.data());
                writer.flush();
                writer.close();
                os.close();
            }
        } catch (Exception e) {
            return new HttpResult(0, context.getString(R.string.error) + e);
        }
        InputStream inputStream;
        try {
            inputStream = conn.getInputStream();
        } catch (IOException exception) {
            inputStream = conn.getErrorStream();
        }

        int responseCode;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            responseCode = 0;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder result = new StringBuilder();
        try {
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            return new HttpResult(responseCode, context.getString(R.string.error) + e);
        }

        return new HttpResult(responseCode, result.toString());
    }

}
