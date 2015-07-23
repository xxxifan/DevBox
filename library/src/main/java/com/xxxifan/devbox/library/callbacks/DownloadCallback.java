package com.xxxifan.devbox.library.callbacks;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xxxifan.devbox.library.tools.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by xifan on 15-7-22.
 */
public class DownloadCallback implements Callback {
    File mTargetFile;

    public DownloadCallback(File targetFile) {
        this.mTargetFile = targetFile;
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (response.cacheResponse() == null) {
            IOUtils.saveToDisk(response.body().source(), mTargetFile);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        e.printStackTrace();
    }

    protected File getTargetFile() {
        return mTargetFile;
    }
}
