package com.al.ai.service;

import android.view.View;

public interface OpenAPIService {
    void sendRequestAsync(View view);
    boolean sendRequestAsync(int actionId);
}
