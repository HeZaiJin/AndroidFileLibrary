package com.hand.document.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.hand.document.provider.IconProvider;

import java.io.File;

/**
 * @author HaoZhang.
 * @date 2018/5/8.
 * @email handhaozhang@gmail.com
 */
public class CustomModelLoader implements ModelLoader<String, Drawable> {
    private Context mContext;

    public CustomModelLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull String model, int width, int height, @NonNull Options options) {

        return new LoadData<>(new ObjectKey(model), /*fetcher=*/ new CustomDataFetcher(mContext, model));
    }

    @Override
    public boolean handles(@NonNull String s) {
        File file = new File(s);
        if (file.exists() && file.isFile() && file.getName().endsWith(".apk")) {
            return true;
        }
        return false;
    }

    public static class CustomDataFetcher implements DataFetcher<Drawable> {

        private final String mModel;
        private Context mContext;
        //static icon
        public CustomDataFetcher(Context context, String model) {
            mContext = context;
            mModel = model;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
            if (null != mContext) {
                Drawable apkIcon = IconProvider.getApkIcon(mContext, mModel);
                if (null != apkIcon) {
                    callback.onDataReady(apkIcon);
                } else {
                    callback.onLoadFailed(new RuntimeException("Error to analyzer apk icon with :" + mModel));
                }
            }
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @NonNull
        @Override
        public Class<Drawable> getDataClass() {
            return Drawable.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
