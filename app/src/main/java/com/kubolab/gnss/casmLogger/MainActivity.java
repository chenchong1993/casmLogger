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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;


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

    String[] ip = new String[8];
    String[] inType = new String[8];
    String ResultFileName = new String();
    private final String urlSendPosition = "http://121.28.103.199:5603/api/apiGetGridSendInfo";
    private static Context context;
    String gridType = "grid";
    String config = new String();
    String gridIP = new String();
   // static GridInfo[] gridInfo = new GridInfo[100];

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
        ip[1] = ":@211.101.24.55:20030/:";
        inType[1] = "4"; //差分数据
        ip[2] = "Example:Configs@products.igs-ip.net:2101/RTCM3EPH:";//星历
        inType[2] = "7";
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

        context = getApplicationContext();

        config = getConfig();

        GridInfo gridInfo[] = getGridInfoFromJson(config);
        String a = gridInfo[1].getGridDesc();
        initListener();
        getGPSLocationContinuously(gridInfo);

        ObsDataThread obsdataThread = new ObsDataThread();
        obsdataThread.start();


        StaticVariable.Status = passingPathJNI(ip, inType, ResultFileName);     //调用C接口（传路径进去）
        //----------------------------------------------------------------------
    }

    private void initListener() {
        //gsp_btn.setOnClickListener(this);
    }
    /**
     * 通过GPS获取定位信息
     */
    @SuppressLint("MissingPermission")
    //@RequiresApi(api = Build.VERSION_CODES.M)
    public void getGPSLocationContinuously (final GridInfo[] gridInfos) {
//        sendLocation2Cloud("23.33","111.33",gridType);

//        设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
        LocationUtils.addLocationListener(context, LocationManager.GPS_PROVIDER, new LocationUtils.ILocationListener() {
            @Override
            public void onSuccessLocation(Location location) {
                if (location != null) {
                    String lat = String.valueOf(location.getLatitude()).substring(0,7);
                    String lon = String.valueOf(location.getLongitude()).substring(0,7);
                    gridIP = getGrid(lat,lon,gridType,gridInfos);
                    passingGridIPJNI(gridIP);
                    //sendLocation2Cloud(lat,lon,gridType);
//                    StaticVariable.Status = passingPathJNI(ip, inType, ResultFileName);
                } else {
                    Toast.makeText(MainActivity.this, "未获取到位置...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 上传至云端,不用这个了
     */
    public void sendLocation2Cloud(String latitude, String longtitude, String gridType){
        if (latitude == null || longtitude == null)return;
        StringBuilder strLoc = new StringBuilder();
        strLoc.append("?userLat=").append(latitude)
                .append("&userLng=").append(longtitude)
                .append("&userGridType=").append(gridType);
        final String content = String.valueOf(strLoc);
        HttpUtil.sendToCloud(urlSendPosition, content, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (response.equals("not in grid")){
                    //Toast.makeText(context,"处于危险区域！！！",Toast.LENGTH_LONG).show();
                    Log.e("未找到格网",response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"未找到格网",Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    Log.e("格网：",response);
                    ip[1] = response;
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("HttpError",e.getMessage());
            }
        });

    }
    /**
     * 判断格网
     */
    public String getGrid(String latitude, String longtitude, String gridType,GridInfo[] gridInfo){

        for (int i=0; i<gridInfo.length; i++) {
//            如果用户获取的时规则格网，则其经纬度是从左下角开始的，使用一下逻辑
            if (gridType.equals("irrgrid")){
                if (gridType.equals(gridInfo[i].getGridType()) &&
                        Double.parseDouble(latitude)<Double.parseDouble(gridInfo[i].getstartLat()) &&
                        Double.parseDouble(latitude)>(Double.parseDouble(gridInfo[i].getstartLat())+Double.parseDouble(gridInfo[i].getLatInterval())) &&
                        Double.parseDouble(longtitude)>Double.parseDouble(gridInfo[i].getstartLng()) &&
                        Double.parseDouble(longtitude)<(Double.parseDouble(gridInfo[i].getstartLng())+Double.parseDouble(gridInfo[i].getLngInterval()))){
                    return ":@"+gridInfo[i].getGridIP()+":"+gridInfo[i].getGridPort();
                }
            }else{
                String a = gridInfo[i].getGridType();
                double b = Double.parseDouble(gridInfo[i].getstartLat());
                double c = Double.parseDouble(gridInfo[i].getstartLng());
                double d = Double.parseDouble(gridInfo[i].getLatInterval());
                double e = Double.parseDouble(gridInfo[i].getLngInterval());
                if (gridType.equals(gridInfo[i].getGridType()) &&
                        Double.parseDouble(latitude)>Double.parseDouble(gridInfo[i].getstartLat()) &&
                        Double.parseDouble(latitude)<(Double.parseDouble(gridInfo[i].getstartLat())+Double.parseDouble(gridInfo[i].getLatInterval())) &&
                        Double.parseDouble(longtitude)>Double.parseDouble(gridInfo[i].getstartLng()) &&
                        Double.parseDouble(longtitude)<(Double.parseDouble(gridInfo[i].getstartLng())+Double.parseDouble(gridInfo[i].getLngInterval()))){
                    return ":@"+gridInfo[i].getGridIP()+":"+gridInfo[i].getGridPort();
                }
            }

        }
        return "not in grid";

    }

    /**
     * 读取配置文件
     */
    public String getConfig(){
        String Result = "";
        try {
            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open("gridConfig.json") );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            while((line = bufReader.readLine()) != null)
                Result += line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    /**
     * 从JSON对象中转存到GridInfo类中
     */
    public GridInfo[]  getGridInfoFromJson(String json){

        JSONArray jsonArray= null;
        try {
            jsonArray = new JSONArray(config);
            int len = jsonArray.length();
            GridInfo[] gridInfo = new GridInfo[len];
            for(int i=0;i<len;i++){
                JSONObject object=jsonArray.getJSONObject(i);
                gridInfo[i] = new GridInfo();
                gridInfo[i].setGridID(object.getString("gridID"));
                gridInfo[i].setGridType(object.getString("gridType"));
                gridInfo[i].setGridDesc(object.getString("gridDesc"));
                gridInfo[i].setstartLat(object.getString("startLat"));
                gridInfo[i].setstartLng(object.getString("startLng"));
                gridInfo[i].setLatInterval(object.getString("latInterval"));
                gridInfo[i].setLngInterval(object.getString("lngInterval"));
                gridInfo[i].setGridPort(object.getString("gridPort"));
                gridInfo[i].setGridIP(object.getString("gridIP"));
            }
            return gridInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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


    public native String passingGridIPJNI(String ip);
    //接收手机观测数据线程
    public class ObsDataThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(true) {
                try {
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

