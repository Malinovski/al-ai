package com.al.ai.util;

import static com.al.ai.util.Constants.CHOICES;
import static com.al.ai.util.Constants.CONTENT;
import static com.al.ai.util.Constants.EMPTY;
import static com.al.ai.util.Constants.END_LINE;
import static com.al.ai.util.Constants.MESSAGE;

import android.util.Log;

import androidx.annotation.NonNull;

import com.al.ai.model.HttpResult;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    private static final String CLASS_NAME = Utils.class.getSimpleName();
    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * @param cs
     *            a CharSequence or {@code null}
     * @return CharSequence length or {@code 0} if the CharSequence is
     *         {@code null}.
     * @since 2.4
     * @since 3.0 Changed signature from length(String) to length(CharSequence)
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    @NonNull
    public static String parseResult(HttpResult result) {
        if (result.statusCode() != 200) {
            return result.response();
        }
        try {
            return END_LINE + new JSONObject(result.response())
                    .getJSONArray(CHOICES)
                    .getJSONObject(0)
                    .getJSONObject(MESSAGE)
                    .getString(CONTENT);
        } catch (JSONException e) {
            Log.e(CLASS_NAME, e.getMessage(), e);
            return EMPTY;
        }
    }
}
