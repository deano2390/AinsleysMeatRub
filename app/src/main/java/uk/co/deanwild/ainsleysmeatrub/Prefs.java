package uk.co.deanwild.ainsleysmeatrub;

import android.content.Context;
import android.content.SharedPreferences;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by deanwild on 29/09/15.
 */
public class Prefs {


    private static final String PREFS_NAME = "ainsley_prefs";
    private static final String KEY = "USE_ALTERNATE_RESOURCES";

    static void shouldUseAlternateResources(Context context, PrefsCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean shouldUSeAlternateResources = prefs.getBoolean(KEY, true);

        if (shouldUSeAlternateResources) {
            checkAgain(context, callback);
        } else {
            callback.callback(shouldUSeAlternateResources);
        }
    }


    static void updateShouldUseAlternateResources(Context context, boolean shouldUseAlternateResources) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY, shouldUseAlternateResources).commit();
    }


    static void checkAgain(final Context context, final PrefsCallback callback) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("http://svm17250.vps.tagadab.com/ainsley.txt")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.callback(true);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                if (result.equals("false")) {
                    updateShouldUseAlternateResources(context, false);
                    callback.callback(false);
                } else {
                    callback.callback(true);
                }
            }
        });
    }

    public interface PrefsCallback {
        void callback(boolean shouldUseAlternateResources);
    }
}
