package com.al.ai;

import static com.al.ai.util.Constants.EMPTY;
import static com.al.ai.util.Constants.END2_LINE;
import static java.util.Objects.requireNonNull;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.al.ai.service.AppProperties;
import com.al.ai.service.HttpManager;
import com.al.ai.service.HttpManagerImpl;
import com.al.ai.service.OpenAPIService;
import com.al.ai.service.OpenAPIServiceImpl;
import com.google.android.material.textfield.TextInputEditText;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainActivity extends AppCompatActivity {
    private ScrollView scrollTV;
    private TextView responseTV;
    private TextView questionTV;
    private TextInputEditText queryEdt;
    private final MainActivity instance;

    private OpenAPIService openAPIService;

    private final Consumer<Runnable> runOnUiThread = this::runOnUiThread;
    private final Supplier<String> dataSupplier = () -> requireNonNull(queryEdt.getText()).toString();
    private final Supplier<Boolean> validator = () -> queryEdt.getText() != null && queryEdt.getText().toString().length() > 0;
    private final Consumer<SpannableString> applyResponse = spanDada -> {
        responseTV.append(END2_LINE);
        responseTV.append(spanDada);
        scrollTV.post(() -> scrollTV.fullScroll(View.FOCUS_DOWN));
    };
    private final Runnable waitAction = () -> questionTV.setText(getString(R.string.pls_wait));
    private final Runnable cleanAction = () -> questionTV.setText(EMPTY);
    private final Runnable cleanQuery = () -> queryEdt.setText(EMPTY);


    public MainActivity() {
        this.instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppProperties properties = new AppProperties(getAssets());
        HttpManager httpManager = new HttpManagerImpl(getApplicationContext());
        openAPIService = new OpenAPIServiceImpl(instance, runOnUiThread, dataSupplier, validator, applyResponse, waitAction, cleanAction,
                cleanQuery, properties, httpManager);

        responseTV = findViewById(R.id.idTVResponse);
        questionTV = findViewById(R.id.idTVQuestion);
        queryEdt = findViewById(R.id.idEdtQuery);
        scrollTV = findViewById(R.id.idSVTVResponse);
        ImageButton queryImgBtn = findViewById(R.id.idEdtQueryImgButton);

        var editorListener = (TextView.OnEditorActionListener) (v, actionId, event) -> openAPIService.sendRequestAsync(actionId);
        var clickListener = (View.OnClickListener) openAPIService::sendRequestAsync;

        queryEdt.setOnEditorActionListener(editorListener);
        queryImgBtn.setOnClickListener(clickListener);
    }

}