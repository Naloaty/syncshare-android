package com.naloaty.streamshare.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.naloaty.streamshare.communication.SSOkHttpClient;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * GlideModule that uses StreamShare SSL certificate.
 * @see SSOkHttpClient
 */
@GlideModule
public class SSOkHttpGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        OkHttpClient client = SSOkHttpClient.getOkHttpClient(context);
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
