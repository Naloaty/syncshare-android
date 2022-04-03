package com.naloaty.streamshare.activity;

import com.naloaty.streamshare.app.SSActivity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.streamshare.R;

/**
 * This activity represents the management screen of albums on the local device.
 * NOTE: This activity is not used on tablets. In this case, LocalAlbumsFragment is located on MainActivity.
 *
 * Related fragment:
 * @see com.naloaty.streamshare.fragment.LocalAlbumsFragment
 */
public class LocalDeviceActivity extends SSActivity {

    private static final String TAG = "LocalDeviceActivity";

    /* UI elements */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_device);
        
        mAppBarLayout = findViewById(R.id.local_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.local_device_toolbar_layout);
        mToolBar = findViewById(R.id.local_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.text_localDevice));
        mAppBarLayout.setExpanded(true, true);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolBar);

        //To make "close" animation (instead of using "parent activity")
        mToolBar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");
    }
}
