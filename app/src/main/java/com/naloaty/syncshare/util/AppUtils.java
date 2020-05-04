package com.naloaty.syncshare.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.config.KeyConfig;
import com.naloaty.syncshare.security.SecurityUtils;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;


public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    private static int mUniqueNumber = 0;
    private static SharedPreferences mSharedPreferences;
    private static final String DEFAULT_PREF = "default";
    public static final int OPTIMIZATION_DISABLE = 756;

    private static DNSSDHelper mDNSSDHelper;

    public static SharedPreferences getDefaultSharedPreferences(final Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(DEFAULT_PREF, Context.MODE_PRIVATE);
        }

        return mSharedPreferences;
    }

    public static SSDevice getLocalDevice(Context context)
    {
        SSDevice device = new SSDevice(getDeviceId(context), AppConfig.APP_VERSION);

        device.setBrand(Build.BRAND);
        device.setModel(Build.MODEL);
        device.setNickname(AppUtils.getLocalDeviceName());

        return device;
    }

    public static String getDeviceId(Context context)
    {
        File certFile = new File(context.getFilesDir(), KeyConfig.CERTIFICATE_FILENAME);
        X509Certificate myCert = SecurityUtils.loadCertificate(KeyConfig.CRYPTO_PROVIDER, certFile);

        Log.w(TAG, "My device id: " + SecurityUtils.calculateDeviceId(myCert));
        //Log.w(TAG, "My certificate: " + myCert.toString());

        return SecurityUtils.calculateDeviceId(myCert);
    }

    public static String getLocalDeviceName()
    {
        //TODO: add ability to change name
        return Build.MODEL.toUpperCase();
    }

    public static int getUniqueNumber()
    {
        return (int) (System.currentTimeMillis() / 1000) + (++mUniqueNumber);
    }

    public static DNSSDHelper getDNSSDHelper(Context context) {
        if (mDNSSDHelper == null)
            mDNSSDHelper = new DNSSDHelper(context);

        return mDNSSDHelper;
    }

    /*
     * Copied from https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android/5921190#5921190
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

    /*
     * Copied from https://tips.androidhive.info/2015/04/android-how-to-check-if-the-app-is-in-background-or-foreground/
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
