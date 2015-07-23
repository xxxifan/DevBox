package com.xxxifan.devbox.library.tools;

import com.google.gson.Gson;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by xifan on 15-7-17.
 */
public class HttpUtils {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private static OkHttpClient sHttpClient;
    private static Gson mGson;

    private HttpUtils() {
    }

    public static OkHttpClient getHttpClient() {
        if (sHttpClient == null) {
            sHttpClient = new OkHttpClient();
            Cache cache = new Cache(Utils.getCacheDir(), 100 * 1024 * 1024);
            sHttpClient.setCache(cache);
            sHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
            sHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
            sHttpClient.setWriteTimeout(20, TimeUnit.SECONDS);
            mGson = new Gson();
        }
        return sHttpClient;
    }

    private static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public static Call get(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        Call call = getHttpClient().newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call post(String url, Object jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, getGson().toJson(jsonBody, jsonBody
                .getClass()));
        return post(url, body, callback);
    }

    public static Call postImage(String url, File file, Callback callback) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_PNG, file);
        return post(url, body, callback);
    }

    public static Call postForm(String url, FormEncodingBuilder form, Callback callback) {
        return post(url, form.build(), callback);
    }

    public static Call post(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = getHttpClient().newCall(request);
        call.enqueue(callback);
        return call;
    }

//    public static Call download(String url, File path) {
//
//    }
}
