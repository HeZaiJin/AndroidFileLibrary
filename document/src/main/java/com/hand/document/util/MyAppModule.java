package com.hand.document.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;

/**
 * @author HaoZhang.
 * @date 2018/6/5.
 * @email handhaozhang@gmail.com
 */
@GlideModule
public class MyAppModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.prepend(String.class, Drawable.class, new ApkLoaderFactory(context));
    }


    /**
     * analyzer with apk file
     */
    public static final class ApkLoaderFactory implements ModelLoaderFactory<String, Drawable> {
        private Context mContext;

        public ApkLoaderFactory(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public ModelLoader<String, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new CustomModelLoader(mContext);
        }

        @Override
        public void teardown() {
        }
    }
}
