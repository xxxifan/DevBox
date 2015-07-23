package com.xxxifan.devbox.library.callbacks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xxxifan.devbox.library.tools.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xifan on 15-7-22.
 */
public class ImageDownloadCallback extends DownloadCallback {

    private int mDesireWidth, mDesireHeight;


    public ImageDownloadCallback(File targetFile) {
        super(targetFile);
    }

    /**
     * return bitmap with specified ImageView size
     */
    public ImageDownloadCallback(File targetFile, ImageView imageView) {
        super(targetFile);
        mDesireWidth = imageView.getWidth();
        mDesireHeight = imageView.getHeight();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        super.onResponse(response);
        onLoadImage();
    }

    public void onLoadImage() {
        new LoadTask().execute();
    }

    @Override
    public void onFailure(Request request, IOException e) {
        super.onFailure(request, e);
        onFinish(null);
    }

    public void onFinish(Bitmap bitmap) {

    }

    private class LoadTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            InputStream stream;
//            Rect rect = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            if (mDesireHeight > 0 || mDesireWidth > 0) {
                options.inJustDecodeBounds = true;
                try {
                    stream = new FileInputStream(getTargetFile());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                BitmapFactory.decodeStream(stream, null, options);

//                // crop
//                int rawWidth = options.outWidth;
//                int rawHeight = options.outHeight;
//                Log.e("", "get width " + rawWidth + " height " + rawHeight);
//                if (rawWidth > mDesireWidth && rawHeight < mDesireHeight) {
//                    float ratio = rawWidth / (float) rawHeight;
//                    int newWidth = (int) (mDesireWidth / ratio);
//                    int left = (rawWidth - newWidth) / 2;
//                    rect = new Rect(left, 0, left + newWidth, rawHeight);
//                    Log.e("", "rect " + left + ", 0, " + (left + newWidth) + ", " + rawHeight);
//                }

                options.inSampleSize = IOUtils.calculateInSampleSize(options, mDesireWidth, mDesireHeight);
                options.inJustDecodeBounds = false;
            }
            // retrieve stream
            try {
                stream = new FileInputStream(getTargetFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            return BitmapFactory.decodeStream(stream, null, options);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mDesireWidth = 0;
            mDesireHeight = 0;
            onFinish(bitmap);
        }
    }
}
