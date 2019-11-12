package com.kubolab.gnss.casmLogger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Calendar;

import static com.kubolab.gnss.casmLogger.DateUtil.getDate2String;


/** The activity for the application. */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_ID = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BODY_SENSORS
    };
    private static final int NUMBER_OF_FRAGMENTS = 4;
    private static final int FRAGMENT_INDEX_SETTING = 0;
    private static final int FRAGMENT_INDEX_LOGGER = 1;
    private static final int FRAGMENT_INDEX_LOGGER2 = 2;
    private static final int FRAGMENT_INDEX_LOGGER3 = 3;

    private GnssContainer mGnssContainer;
    private UiLogger mUiLogger;
    private FileLogger mFileLogger;
    private Fragment[] mFragments;
    private GnssNavigationDataBase mGnssNavigationDataBase;
    private SensorContainer mSensorContainer;
    private static MainActivity instance = null;

    public boolean GNSSRegister = false;

    //----------------------cc-----------------------------------
    static {
        System.loadLibrary("rtd");
    }
    DecimalFormat df3 = new DecimalFormat("00");
    DecimalFormat df4 = new DecimalFormat("0000");
    //-------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //アクションバーのロゴ設定
        getSupportActionBar().setDisplayShowHomeEnabled(true); //アイコン表示
        //getSupportActionBar().setIcon(R.mipmap.icon_casm_2);

        requestPermissionAndSetupFragments(this);
        instance = this;

        //---------------------------cc-------------------------------------------
        String[] ip = new String[8];
        String[] inType = new String[8];
        String ResultFileName = new String();
        ip[1] = "Example:Configs@products.igs-ip.net:2101/RTCM3EPH:";//星历
        inType[1] = "7";
        ip[2] = ":@106.53.66.239:21002";//改正数
        inType[2] = "4";
        Calendar calendar = Calendar.getInstance();
        String divide = "-";
        ResultFileName = String.valueOf("/sdcard/Alaas/")
//            ResultFileName = String.valueOf("data/data/A2laas/")
                + String.valueOf(df4.format(calendar.get(Calendar.YEAR))) + divide
                + String.valueOf(df3.format(calendar.get(Calendar.MONTH) + 1)) + divide
                + String.valueOf(df3.format(calendar.get(Calendar.DAY_OF_MONTH))) + divide
                + String.valueOf(df3.format(calendar.get(Calendar.HOUR_OF_DAY))) + divide
                + String.valueOf(df3.format(calendar.get(Calendar.MINUTE))) + divide
                + String.valueOf(df3.format(calendar.get(Calendar.SECOND)))
                + String.valueOf(".txt");


        ObsDataThread obsdataThread = new ObsDataThread();
        obsdataThread.start();

        StaticVariable.Status = passingPathJNI(ip, inType, ResultFileName);     //调用C接口（传路径进去）
        //----------------------------------------------------------------------
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermissions(this)) {
            mGnssContainer.registerAll();
            GNSSRegister = true;
            if (SettingsFragment.useDeviceSensor) {
                mSensorContainer.registerSensor();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasPermissions(this)) {
            mGnssContainer.unregisterAll();
            GNSSRegister = false;
            if (SettingsFragment.useDeviceSensor) {
                mSensorContainer.unregisterSensor();
            }
        }
        //データベースは今のところクリアする.
        SQLiteDatabase NavDB;
        SQLiteManager hlpr = new SQLiteManager(getApplicationContext());
        NavDB = hlpr.getWritableDatabase();
        deleteDatabase(NavDB.getPath());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FRAGMENT_INDEX_SETTING:
                    return mFragments[FRAGMENT_INDEX_SETTING];
                case FRAGMENT_INDEX_LOGGER:
                    return mFragments[FRAGMENT_INDEX_LOGGER];
                case FRAGMENT_INDEX_LOGGER2:
                    return mFragments[FRAGMENT_INDEX_LOGGER2];
                case FRAGMENT_INDEX_LOGGER3:
                    return mFragments[FRAGMENT_INDEX_LOGGER3];
                default:
                    throw new IllegalArgumentException("Invalid section: " + position);
            }
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 4;
        }

        Drawable myDrawable;
        String title;

        @Override
        public CharSequence getPageTitle(int position) {
            //Locale locale = Locale.getDefault();
            /*switch (position) {
                case 0:
                    title =  "Setting";
                    myDrawable = getResources().getDrawable(R.drawable.icon_101930_256);
                    break;
                case 1:
                    title = "Monitor&Log";
                    myDrawable = getResources().getDrawable(R.drawable.icon_160240_256);
                    break;
                case 2:
                    title = "skyplot.png";
                    myDrawable = getResources().getDrawable(R.drawable.icon_146290_256);
                    break;
                default:
                    break;
            }
            SpannableStringBuilder sb = new SpannableStringBuilder("   " + title);
            try {
                myDrawable.setBounds(2, 2, myDrawable.getIntrinsicWidth()/10, myDrawable.getIntrinsicHeight()/10);
                ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
                sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                Log.e("Drawable Error","Span Draw Error");
            }*/
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_ID) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //SettingsFragment.PermissionOK = true;
                setupFragments();
                mGnssContainer.registerAll();
            }
        }
    }

    private void setupFragments() {
        mUiLogger = new UiLogger(getApplicationContext());
        mFileLogger = new FileLogger(getApplicationContext());
        //mSensorContainer = new SensorContainer(getApplicationContext() ,mUiLogger, mFileLogger);
        mGnssContainer = new GnssContainer(getApplicationContext(), mUiLogger, mFileLogger);
        mGnssNavigationDataBase = new GnssNavigationDataBase(getApplicationContext());
        mFragments = new Fragment[NUMBER_OF_FRAGMENTS];
        SettingsFragment settingsFragment = new SettingsFragment();
        //settingsFragment.setSensorContainer(mSensorContainer);
        settingsFragment.setGpsContainer(mGnssContainer);
        settingsFragment.setFileLogger(mFileLogger);
        settingsFragment.setUILogger(mUiLogger);
        settingsFragment.setGnssContainer(mGnssContainer);
        mFragments[FRAGMENT_INDEX_SETTING] = settingsFragment;

        LoggerFragment loggerFragment = new LoggerFragment();
        loggerFragment.setUILogger(mUiLogger);
        loggerFragment.setFileLogger(mFileLogger);
        mFragments[FRAGMENT_INDEX_LOGGER] = loggerFragment;

        Logger2Fragment logger2Fragment = new Logger2Fragment();
        logger2Fragment.setUILogger(mUiLogger);
        logger2Fragment.setFileLogger(mFileLogger);
        mFragments[FRAGMENT_INDEX_LOGGER2] = logger2Fragment;

        Logger3Fragment logger3Fragment = new Logger3Fragment();
        logger3Fragment.setUILogger(mUiLogger);
        logger3Fragment.setFileLogger(mFileLogger);
        mFragments[FRAGMENT_INDEX_LOGGER3] = logger3Fragment;

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // The viewpager that will host the section contents.
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setTabsFromPagerAdapter(adapter);

        // Set a listener via setOnTabSelectedListener(OnTabSelectedListener) to be notified when any
        // tab's selection state has been changed.
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        // Use a TabLayout.TabLayoutOnPageChangeListener to forward the scroll and selection changes to
        // this layout
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
        //TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TabLayout.Tab tab1 = tabLayout.getTabAt(0);
        View tab1View = inflater.inflate(R.layout.main_tab1, null);
        tab1.setCustomView(tab1View);

        TabLayout.Tab tab2 = tabLayout.getTabAt(1);
        View tab2View = inflater.inflate(R.layout.main_tab2, null);
        tab2.setCustomView(tab2View);

        TabLayout.Tab tab3 = tabLayout.getTabAt(2);
        View tab3View = inflater.inflate(R.layout.main_tab3, null);
        tab3.setCustomView(tab3View);

        TabLayout.Tab tab4 = tabLayout.getTabAt(3);
        View tab4View = inflater.inflate(R.layout.main_tab4, null);
        tab4.setCustomView(tab4View);
    }

    private boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            // Permissions granted at install time.
            return true;
        }
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissionAndSetupFragments(final Activity activity) {
        if (hasPermissions(activity)) {
            setupFragments();
        } else {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, LOCATION_REQUEST_ID);
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String passingPathJNI(String[] ip, String[] port, String resultfilename);
    //接收手机观测数据线程
    public class ObsDataThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(true) {
                try {
                    //if(StaticGnssData.flag==1)
                    //{
                        String serviceString = Context.LOCATION_SERVICE;// 获取的是位置服务
                        LocationManager locationManager = (LocationManager) getSystemService(serviceString);// 调用getSystemService()方法来获取LocationManager对象
                        String provider = LocationManager.GPS_PROVIDER;// 指定LocationManager的定位方法
                        @SuppressLint("MissingPermission")
                        Location location = locationManager != null ? locationManager.getLastKnownLocation(provider) : null;// 调用getLastKnownLocation()方法获取当前的位置信息
                        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    Activity#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for Activity#requestPermissions for more details.
                                return;
                            }
                        }
                        //  locationManager.requestLocationUpdates(provider, 1000, 10, locationListener);// 产生位置改变事件的条件设定为距离改变10米，时间间隔为1秒，设定监听位置变化
                        if(location != null) {
                            StaticGnssData.lat = location.getLatitude();//获取纬度
                            StaticGnssData.lng = location.getLongitude();//获取经度
                            StaticGnssData.alt = location.getAltitude();//获取大地高wgs84
                            StaticGnssData.epho = getDate2String(location.getTime(), "yyyy MM dd HH mm ss");
                            Log.i("test:经纬度", " 经度: " + StaticGnssData.lng + " 纬度:" + StaticGnssData.lat + " 大地高:" + StaticGnssData.alt);
                            passingobsLBSdataJNI(StaticGnssData.lat, StaticGnssData.lng, StaticGnssData.alt, StaticGnssData.epho);
                        }

                            StaticGnssData.syns = passingobsDataJNI(
                                    StaticGnssData.GPSTweek,StaticGnssData.GPSTsecond,
                                    StaticGnssData.BDSTweek,StaticGnssData.BDSTsecond,
                                    StaticGnssData.GLOTweek,StaticGnssData.GLOTsecond,
                                    StaticGnssData.GALTweek,StaticGnssData.GALTsecond,
                                    StaticGnssData.gpssvnum,StaticGnssData.bdssvnum,
                                    StaticGnssData.glosvnum,StaticGnssData.galsvnum,
                                    StaticGnssData.Gpsprn,  StaticGnssData.Bdsprn,
                                    StaticGnssData.Gloprn,  StaticGnssData.Galprn,
                                    StaticGnssData.GpsC1,StaticGnssData.GpsL1,StaticGnssData.GpsD1,
                                    StaticGnssData.GpsC2,StaticGnssData.GpsL2,StaticGnssData.GpsD2,
                                    StaticGnssData.GpsC3,StaticGnssData.GpsL3,StaticGnssData.GpsD3,
                                    StaticGnssData.BdsC1,StaticGnssData.BdsL1,StaticGnssData.BdsD1,
                                    StaticGnssData.BdsC2,StaticGnssData.BdsL2,StaticGnssData.BdsD2,
                                    StaticGnssData.BdsC3,StaticGnssData.BdsL3,StaticGnssData.BdsD3,
                                    StaticGnssData.GloC1,StaticGnssData.GloL1,StaticGnssData.GloD1,
                                    StaticGnssData.GloC2,StaticGnssData.GloL2,StaticGnssData.GloD2,
                                    StaticGnssData.GloC3,StaticGnssData.GloL3,StaticGnssData.GloD3,
                                    StaticGnssData.GalC1,StaticGnssData.GalL1,StaticGnssData.GalD1,
                                    StaticGnssData.GalC2,StaticGnssData.GalL2,StaticGnssData.GalD2,
                                    StaticGnssData.GalC3,StaticGnssData.GalL3,StaticGnssData.GalD3);
                    //}
                    Thread.sleep(500);//改为了500  12.23
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    public native int passingobsLBSdataJNI(double lat,double lng,double alt,String epho);

    public native int passingobsDataJNI(int[] gpsweek,double[] gpssecond,
                                        int[] bdsweek,double[] bdssecond,
                                        int[] gloweek,double[] glosecond,
                                        int[] galweek,double[] galsecond,
                                        int gpssvnum, int bdssvnum,
                                        int glosvnum, int galsvnum,
                                        int[] gpsprn, int[] bdsprn,
                                        int[] gloprn, int[] galprn,
                                        double[] gpsC1,double[] gpsL1,double[] gpsD1,
                                        double[] gpsC2,double[] gpsL2,double[] gpsD2,
                                        double[] gpsC3,double[] gpsL3,double[] gpsD3,
                                        double[] bdsC1,double[] bdsL1,double[] bdsD1,
                                        double[] bdsC2,double[] bdsL2,double[] bdsD2,
                                        double[] bdsC3,double[] bdsL3,double[] bdsD3,
                                        double[] gloC1,double[] gloL1,double[] gloD1,
                                        double[] gloC2,double[] gloL2,double[] gloD2,
                                        double[] gloC3,double[] gloL3,double[] gloD3,
                                        double[] galC1,double[] galL1,double[] galD1,
                                        double[] galC2,double[] galL2,double[] galD2,
                                        double[] galC3,double[] galL3,double[] galD3);

}

