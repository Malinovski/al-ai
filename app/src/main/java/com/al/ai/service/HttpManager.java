package com.al.ai.service;

import com.al.ai.model.HttpRequest;
import com.al.ai.model.HttpResult;

public interface HttpManager {
    HttpResult sendGetRequest(HttpRequest request);

    HttpResult sendPostRequest(HttpRequest request);
}
