package com.al.ai.service;

import static com.al.ai.util.Constants.COMMA;
import static com.al.ai.util.Utils.isBlank;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AppProperties {
    private final String className = getClass().getSimpleName();

    private static final Properties settings = new Properties();
    public static final String APP_PROPERTIES = "app.properties";
    private final AssetManager asset;
    private final Supplier<AppProperties> instance;

    private final Supplier<java.util.Properties> supplier = () -> loadFromFile().map(inputStream -> {
        try {
            settings.load(inputStream);
            Optional<InputStream> fis = loadFromFile();
            if (fis.isPresent()) {
                settings.load(fis.get());
            }
        } catch (IOException e) {
            Log.e(className, e.getMessage(), e);
        }
        return settings;
    }).orElse(settings);

    private java.util.Properties getSettings() {
        return supplier.get();
    }

    public String getProperty(final String code) {
        return getSettings().getProperty(code);
    }

    public String getProperty(final String code, final String defaultValue) {
        String value = getProperty(code);
        if (isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    public int getIntProperty(final String code) {
        return getIntProperty(code, 0);
    }

    public int getIntProperty(final String code, final int defaultValue) {
        String value = getProperty(code);
        if (isBlank(value)) {
            return defaultValue;
        }
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
        }
        return result;
    }

    public List<String> getListProperty(final String code, final List<String> defaultValue) {
        String value = getProperty(code);
        if (isBlank(value)) {
            return defaultValue;
        }
        return Arrays.stream(value.split(COMMA)).map(String::trim).collect(Collectors.toList());
    }

    private Optional<InputStream> loadFromFile() {
        if (instance.get() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(instance.get().loadStreamFromFile(APP_PROPERTIES));
    }

    public AppProperties(AssetManager asset) {
        this.asset = asset;
        instance = () -> this;
    }

    public InputStream loadStreamFromFile(String path) {
        try {
            return asset.open(path);
        } catch (IOException e) {
            Log.e(className, e.getMessage(), e);
        }
        return null;
    }
}
