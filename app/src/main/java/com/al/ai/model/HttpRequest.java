package com.al.ai.model;

import java.util.Map;

public record HttpRequest(String url, String data, Map<String, String> headers, int timeOut) {
}
