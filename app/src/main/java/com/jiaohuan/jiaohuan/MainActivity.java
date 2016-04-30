package com.jiaohuan.jiaohuan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity {

    ViewPager mViewPager = null;
    ImageView mLeft;
    ImageView mCenter;
    ImageView mRight;
    TextView mSettings;
    private ShakeDetector mShaker;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mPopupWindow;
    private LinearLayout mLinearLayout;
    private TextView mTime;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Button mGPS;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new MyLocationListener();

        mLinearLayout = (LinearLayout) findViewById(R.id.main_activity_layout);
        mGPS = (Button) findViewById(R.id.gps);

        // These are the bottom buttons
        mLeft = (ImageView) findViewById(R.id.left);
        mCenter = (ImageView) findViewById(R.id.center);
        mRight = (ImageView) findViewById(R.id.right);

        // App starts on the main page, so obviously the arrow will be the 'on' one
        mCenter.setImageResource(R.drawable.white_arrow);
        mLeft.setImageDrawable(null);
        mRight.setImageDrawable(null);

        // Settings is off by default, because app starts on the 'main' screen
        mSettings = (TextView) findViewById(R.id.settings);
        mSettings.setVisibility(View.INVISIBLE);

        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
            }
        });

        // Shake listener
        mShaker = new ShakeDetector(getApplicationContext());
        mShaker.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            public void onShake() {
                // Make card_expand here
                mLayoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup mContainer = (ViewGroup) mLayoutInflater.inflate(R.layout.shake_popup, null);

                // Set the location where the pop up occurs
                mPopupWindow = new PopupWindow(mContainer, 1000, 1300, true);

                // Connect button and TextView
                mTime = (TextView) mPopupWindow.getContentView().findViewById(R.id.time);

                mPopupWindow.showAtLocation(mLinearLayout, Gravity.CENTER_HORIZONTAL, 0, 0);

                // When anywhere is tapped, the pop up dismisses, it also resumes the shaker
                mContainer.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mPopupWindow.dismiss();
                        mShaker.resume();
                        return true;
                    }
                });

                // Gets current time (UNIX time) and displays it
                long time= System.currentTimeMillis();
                Log.d("Time", time + "");
                String stringTime = Long.toString(time);
                mTime.setText(stringTime);

                // THIS IS THE VALUE THAT GETS SENT TO SERVER
                // time -> Database

                // Shaker is paused when pop up is displayed
                mShaker.pause();
            }
        });
        mShaker.pause();

        // Code the start the view pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new MyAdapter(fragmentManager));
        mViewPager.setCurrentItem(1);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            // Either pause or resume shaker, and assign appropriate bottom icon
            public void onPageSelected(int position) {
                if (position == 1) {
                    mSettings.setVisibility(View.INVISIBLE);
                    //mShaker.resume();
                    changeToMain();
                } else if (position == 2) {
                    mSettings.setVisibility(View.VISIBLE);
                    mShaker.pause();
                    changeToProfile();
                } else if (position == 0) {
                    mSettings.setVisibility(View.INVISIBLE);
                    mShaker.pause();
                    changeToCards();
                }
            }
        });
    }

    // Various methods to change the pictures

    public void changeToCards(){
        mLeft.setImageResource(R.drawable.white_card);
        mCenter.setImageDrawable(null);
        mRight.setImageDrawable(null);
    }

    public void changeToMain (){
        mLeft.setImageDrawable(null);
        mCenter.setImageResource(R.drawable.white_arrow);
        mRight.setImageDrawable(null);
    }

    public void changeToProfile (){
        mLeft.setImageDrawable(null);
        mCenter.setImageDrawable(null);
        mRight.setImageResource(R.drawable.white_profile);
    }

    public void jumpToMain(View view) {
        mSettings.setVisibility(View.INVISIBLE);
        //mShaker.resume();
        mViewPager.setCurrentItem(1);
    }
    public void jumpToProfile(View view) {
        mShaker.pause();
        mSettings.setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(2);
    }

    public void jumpToCards(View view) {
        mShaker.pause();
        mSettings.setVisibility(View.INVISIBLE);
        mViewPager.setCurrentItem(0);
    }

    public void inflateSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void jumpToEdit(View view){
        mSettings.setVisibility(View.INVISIBLE);
        mRight.setImageDrawable(null);
    }

    private void getGPSWrapper() {
        int hasLocationPermission = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }

        getGPS();
    }

    private void getGPS(){

    }

}

class MyAdapter extends FragmentStatePagerAdapter {

    public MyAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i){
        Fragment fragment = null;
        if(i == 0){
            fragment = new MyCards();
        }
        if(i == 1){
            fragment = new MainFragment();
        }
        if(i == 2){
            fragment = new MyProfile();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
/*---------- Listener class to get coordinates ------------- */
class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location loc) {

        Log.wtf("CODE", "RUNNING");

        String longitude = "Longitude: " + loc.getLongitude();
        Log.e("HI", longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.e("HI", latitude);

    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}