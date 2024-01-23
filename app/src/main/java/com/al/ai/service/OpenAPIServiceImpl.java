package com.al.ai.service;

import static com.al.ai.util.Constants.APPLICATION_JSON;
import static com.al.ai.util.Constants.AUTHORIZATION;
import static com.al.ai.util.Constants.BEARER;
import static com.al.ai.util.Constants.CLOSE;
import static com.al.ai.util.Constants.CONNECTION;
import static com.al.ai.util.Constants.CONTENT;
import static com.al.ai.util.Constants.CONTENT_TYPE;
import static com.al.ai.util.Constants.MESSAGES;
import static com.al.ai.util.Constants.MODEL;
import static com.al.ai.util.Constants.MSG_OPENAI_KEY;
import static com.al.ai.util.Constants.MSG_OPENAI_MODEL;
import static com.al.ai.util.Constants.MSG_OPENAI_TIMEOUT;
import static com.al.ai.util.Constants.MSG_OPENAI_URL;
import static com.al.ai.util.Constants.ROLE;
import static com.al.ai.util.Constants.SPACE;
import static com.al.ai.util.Constants.USER;
import static com.al.ai.util.Utils.parseResult;
import static java.util.Collections.singletonList;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.al.ai.R;
import com.al.ai.model.HttpRequest;

import org.json.JSONObject;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OpenAPIServiceImpl implements OpenAPIService {

    private final Context context;
    private final Consumer<Runnable> runOnUiThread;
    private final Supplier<String> dataSupplier;
    private final Supplier<Boolean> validator;
    private final Consumer<SpannableString> applyResponse;
    private final Runnable waitAction;
    private final Runnable cleanAction;
    private final Runnable cleanQuery;
    private final AppProperties properties;
    private final HttpManager httpManager;

    public OpenAPIServiceImpl(Context context,
                              Consumer<Runnable> runOnUiThread,
                              Supplier<String> dataSupplier,
                              Supplier<Boolean> validator,
                              Consumer<SpannableString> applyResponse,
                              Runnable waitAction,
                              Runnable cleanAction,
                              Runnable cleanQuery,
                              AppProperties properties,
                              HttpManager httpManager) {
        this.context = context;
        this.runOnUiThread = runOnUiThread;
        this.dataSupplier = dataSupplier;
        this.validator = validator;
        this.applyResponse = applyResponse;
        this.waitAction = waitAction;
        this.cleanAction = cleanAction;
        this.cleanQuery = cleanQuery;
        this.properties = properties;
        this.httpManager = httpManager;
    }

    @Override
    public void sendRequestAsync(View view) {
        sendRequestAsync(EditorInfo.IME_ACTION_SEND);
    }

    @Override
    public boolean sendRequestAsync(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            if (validator.get()) {
                TaskRunner taskRunner = new TaskRunner();
                taskRunner.executeAsync(
                        () -> {
                            runOnUiThread.accept(waitAction);
                            var data = dataSupplier.get();
                            runOnUiThread.accept(() -> {
                                var userSpan = new BackgroundColorSpan(Color.argb(100, 3, 208, 244));
                                var spanDada = new SpannableString(data);
                                spanDada.setSpan(userSpan, 0, data.length(), 0);
                                applyResponse.accept(spanDada);
                            });
                            runOnUiThread.accept(cleanQuery);
                            var jsonObject = new JSONObject(Map.of(
                                    MODEL, properties.getProperty(MSG_OPENAI_MODEL),
                                    MESSAGES, singletonList(new JSONObject(Map.of(ROLE, USER, CONTENT, data)))
                            ));
                            var headers = Map.of(
                                    CONTENT_TYPE, APPLICATION_JSON,
                                    AUTHORIZATION, BEARER + SPACE + properties.getProperty(MSG_OPENAI_KEY),
                                    CONNECTION, CLOSE);
                            return new HttpRequest(properties.getProperty(MSG_OPENAI_URL), jsonObject.toString(), headers,
                                    properties.getIntProperty(MSG_OPENAI_TIMEOUT));
                        },
                        request -> parseResult(httpManager.sendPostRequest(request)),
                        result -> {
                            runOnUiThread.accept(cleanAction);
                            runOnUiThread.accept(() -> {
                                var assistanceSpan = new BackgroundColorSpan(Color.DKGRAY);
                                var spanDada = new SpannableString(result);
                                spanDada.setSpan(assistanceSpan, 0, result.length(), 0);
                                applyResponse.accept(spanDada);
                            });
                        });
            } else {
                Toast.makeText(context, context.getString(R.string.pls_type_query), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }
}
