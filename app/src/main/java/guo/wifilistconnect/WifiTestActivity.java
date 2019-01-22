package guo.wifilistconnect;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import guo.wifilistconnect.adapter.WifiListAdapter;
import guo.wifilistconnect.app.AppContants;
import guo.wifilistconnect.bean.WifiBean;
import guo.wifilistconnect.dialog.WifiLinkDialog;
import guo.wifilistconnect.utils.CollectionUtils;

public class WifiTestActivity extends AppCompatActivity implements View.OnClickListener  {

    private static final String TAG = "WifiTestActivity";
    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mHasPermission;

    ProgressBar pbWifiLoading;

    List<WifiBean> realWifiList = new ArrayList<>();

    private WifiListAdapter adapter;

    private RecyclerView recyWifiList;

    private WifiBroadcastReceiver wifiReceiver;

    private int connectType = 0;//1：连接成功？ 2 正在连接（如果wifi热点列表发生变需要该字段）

    private Button buttonp,button,buttonc;

    private boolean ifshow = false;

    private WifiManager mWifiManager;

    private CurrentWifiInfo mInfo = new CurrentWifiInfo();

    private ConnectivityManager mConnManaer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        button = (Button) findViewById(R.id.starttest);
        buttonp = (Button)findViewById(R.id.stoptest);
        buttonc = (Button)findViewById(R.id.current_bt);
        button.setOnClickListener(this);
        buttonp.setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mConnManaer = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        setButtonEnable();
        pbWifiLoading = (ProgressBar) this.findViewById(R.id.pb_wifi_loading);

        hidingProgressBar();
        mHasPermission = checkPermission();
        if (!mHasPermission && WifiSupport.isOpenWifi(this)) {  //未获取权限，申请权限
            requestPermission();
        }else if(mHasPermission && WifiSupport.isOpenWifi(this)){  //已经获取权限
            initRecycler();
        }else{
            Toast.makeText(this,"WIFI处于关闭状态",Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecycler() {
        recyWifiList = (RecyclerView) this.findViewById(R.id.recy_list_wifi);
        if(!ifshow) {
            realWifiList.clear();
        }
        adapter = new WifiListAdapter(this,realWifiList);//realWifiList
        //show list
        recyWifiList.setLayoutManager(new LinearLayoutManager(this));
        recyWifiList.setAdapter(adapter);

        if(WifiSupport.isOpenWifi(this) && mHasPermission){
            sortScaResult();
        }else{
            Toast.makeText(this,"WIFI处于关闭状态或权限获取失败22222",Toast.LENGTH_SHORT).show();
        }
        //采用匿名内部类的形式实现了onItemClickListener、onItemLongClickListener接口
        adapter.setOnItemClickListener(new WifiListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Object o) {
                WifiBean wifiBean = realWifiList.get(postion);
                if(wifiBean.getState().equals(AppContants.WIFI_STATE_UNCONNECT) || wifiBean.getState().equals(AppContants.WIFI_STATE_CONNECT)){
                    String capabilities = realWifiList.get(postion).getCapabilities();
                    if(WifiSupport.getWifiCipher(capabilities) == WifiSupport.WifiCipherType.WIFICIPHER_NOPASS){//无需密码
                        WifiConfiguration tempConfig  = WifiSupport.isExsits(wifiBean.getWifiName(),WifiTestActivity.this);
                        if(tempConfig == null){//无密码，但是没有配置过
                            Log.d("mypadd"," getWifiName SSID: "+wifiBean.getWifiName()+" password: null, ssid "+wifiBean.getWifiName().split("\\:")[0]);
                            WifiConfiguration config = WifiSupport.createWifiConfig(wifiBean.getWifiName().split("\\:")[0], null, WifiSupport.WifiCipherType.WIFICIPHER_NOPASS);
                            boolean  flag = WifiSupport.addNetWork(config, WifiTestActivity.this);
                            Toast.makeText(WifiTestActivity.this, "启动连接：" + config.SSID + " [" + flag + "]\n", Toast.LENGTH_LONG).show();
                        }else{//无密码，有配置过
                            Log.d("mypadd"," 有配置过: "+wifiBean.getWifiName().split("\\:")[0]);
                            boolean  flag = WifiSupport.addNetWork(tempConfig, WifiTestActivity.this);
                            Toast.makeText(WifiTestActivity.this, "启动连接：" + tempConfig.SSID + " [" + flag + "]\n", Toast.LENGTH_LONG).show();
                        }
                    }else{   //需要密码，弹出输入密码dialog
                        Toast.makeText(WifiTestActivity.this,"you click "+wifiBean.getWifiName().split("\\:")[0],Toast.LENGTH_SHORT).show();
                        noConfigurationWifi(postion);
                    }
                }
            }
        });
        adapter.setOnItemLongClickListener(new WifiListAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position, Object o) {
                WifiBean wifiBean = realWifiList.get(position);
                Toast.makeText(WifiTestActivity.this,"you long click "+wifiBean.getWifiName(),Toast.LENGTH_SHORT).show();
                String ssid = wifiBean.getWifiName();
                showDialog(ssid.split("\\:")[0]);
            }
        });
    }
    private void showDialog(String ssid){
        //getWindowManager()
        RemoveFragment removeFragment = new RemoveFragment();
        removeFragment.ssid = ssid;
        removeFragment.show(getSupportFragmentManager(),"REMOVE");
    }

    private void noConfigurationWifi(int position) {//之前没配置过该网络， 弹出输入密码界面
        String ssid = realWifiList.get(position).getWifiName().split("\\:")[0];
        WifiLinkDialog linkDialog = new WifiLinkDialog(this,R.style.dialog_dl,ssid, realWifiList.get(position).getCapabilities());
        if(!linkDialog.isShowing()){
            linkDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.starttest:
                setWifiEnable(true);
                break;
            case R.id.stoptest:
                setWifiEnable(false);
                break;
            default:
                //
        }
    }

    private void setButtonEnable(){
        int status = getWifiState();
        if(status==WifiManager.WIFI_STATE_ENABLED) {
            button.setEnabled(false);
            buttonp.setEnabled(true);
            buttonc.setEnabled(true);
            buttonc.setBackgroundColor(getResources().getColor(R.color.homecolor1));
        } else if(status==WifiManager.WIFI_STATE_DISABLED) {
            button.setEnabled(true);
            buttonp.setEnabled(false);
            buttonc.setEnabled(false);
            buttonc.setBackgroundColor(getResources().getColor(R.color.color_red));
        }
    }
    public void toCurrentAp(View view){
        getCurrentWifi();
    }
    private String getCurrentWifi(){
        int wifiState = getWifiState();
        WifiInfo info = getConnectionInfo();
        String ssid = info != null ? info.getSSID() : "null";
        String sSID = "";
        if(!ssid.equals("null")&&!ssid.equals("<unknown ssid>")) {
            sSID = info.getSSID().split("\"")[1];
        }
        String state = "";

        List<ScanResult> list = getScanResults();
        ScanResult currentScanResult =null;
        for(ScanResult srt:list) {
            if(ssid.equals("\""+srt.SSID+"\"")){
                currentScanResult = srt;//
            }
        }
        switch (wifiState) {
            case 0:
                state = "WIFI_STATE_DISABLING";
                break;
            case 1:
                state = "WIFI_STATE_DISABLED";
                break;
            case 2:
                state = "WIFI_STATE_ENABLING";
                break;
            case 3:
                state = "WIFI_STATE_ENABLED";
                break;
            case 4:
                state = "WIFI_STATE_UNKNOWN";
                break;
            default:
                break;
        }
        //  AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String ssids = sSID;
        new AlertDialog.Builder(this,R.style.AlertDialog).setTitle("Current Wifi Information")
                .setMessage("[ " + sSID + " ](" + state + ")\n"+ mInfo.getInfo(mWifiManager,currentScanResult,sSID))
                .setIcon(getImageId(currentScanResult)).setCancelable(true)
                .setPositiveButton("Level overview", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(WifiTestActivity.this,"(-∞,-88): 0\n[-88,-75): 1\n[-75,-62): 2\n[-62,+∞): 3",
                                Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("disconnect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog.dismiss();
                   mWifiManager.disconnect();
                   WifiSupport.RemoveConfig(ssids,WifiTestActivity.this);
            }
        }).create().show();
        return ssid;
    }
    private int getVisiable(ScanResult sr){
        if(sr!=null) {
            if("no password".equals(getEncryption(sr))){
                return View.INVISIBLE;
            }else
                return View.VISIBLE;
        }else
            return View.GONE;
    }

    private int getImageId(ScanResult sr){
        if(sr!=null) {
            Log.d("mytestin","ssid "+sr.SSID+ " level"+sr.level);
            switch (mWifiManager.calculateSignalLevel(sr.level, 4)) {
                case 0:
                    if ("no password".equals(getEncryption(sr)))
                        return R.drawable.ic_wifi_signal_1_dark;
                    else
                        return R.drawable.ic_wifi_lock_signal_1_dark;
                case 1:
                    if ("no password".equals(getEncryption(sr)))
                        return R.drawable.ic_wifi_signal_2_dark;
                    else
                        return R.drawable.ic_wifi_lock_signal_2_dark;
                case 2:
                    if ("no password".equals(getEncryption(sr)))
                        return R.drawable.ic_wifi_signal_3_dark;
                    else
                        return R.drawable.ic_wifi_lock_signal_3_dark;
                case 3:
                    if ("no password".equals(getEncryption(sr)))
                        return R.drawable.ic_wifi_signal_4_dark;
                    else
                        return R.drawable.ic_wifi_lock_signal_4_dark;
                default:
                    return -1;
            }
        }else
            return R.drawable.wifivector;
    }
    private String getEncryption(ScanResult scanResult) {
        if (!TextUtils.isEmpty(scanResult.SSID)) {
            String capabilities = scanResult.capabilities;//性能，加密方式
            Log.i("hefeng", "[" + scanResult.SSID + "]" + capabilities);

            if (!TextUtils.isEmpty(capabilities)) {
                if (capabilities.contains("WPA")
                        || capabilities.contains("wpa")) {
                    Log.i("hefeng", "wpa");
                    return "WPA";
                } else if (capabilities.contains("WPA2")
                        || capabilities.contains("wpa2")) {
                    return "WPA2";
                } else if (capabilities.contains("WPS")
                        || capabilities.contains("wps")) {
                    return "WPS";
                } else if (capabilities.contains("WEP")
                        || capabilities.contains("wep")) {
                    Log.i("hefeng", "wep");
                    return "WEP";
                } else {
                    Log.i("hefeng", "no");
                    return "no password";
                }
            }
        }
        return scanResult.capabilities.toString();
    }

    public void setWifiEnable(boolean state){
        //首先，用Context通过getSystemService获取wifimanager   //getContext() fragment
        //  WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //调用WifiManager的setWifiEnabled方法设置wifi的打开或者关闭，只需把下面的state改为布尔值即可（true:打开 false:关闭）
        mWifiManager.setWifiEnabled(state);
    }
    private int getWifiState(){
        return mWifiManager.getWifiState();
    }

    private List<ScanResult> getScanResults(){
        return mWifiManager.getScanResults();
    }
    private WifiInfo getConnectionInfo(){
        return mWifiManager.getConnectionInfo();
    }
    private NetworkInfo getNetworkInfo() {
        return mConnManaer.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        this.registerReceiver(wifiReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(wifiReceiver);
    }

    //监听wifi状态
    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state){
                    /**
                     * WIFI_STATE_DISABLED    WLAN已经关闭
                     * WIFI_STATE_DISABLING   WLAN正在关闭
                     * WIFI_STATE_ENABLED     WLAN已经打开
                     * WIFI_STATE_ENABLING    WLAN正在打开
                     * WIFI_STATE_UNKNOWN     未知
                     */
                    case WifiManager.WIFI_STATE_DISABLED:{
                        Log.d(TAG,"已经关闭");
                        realWifiList.clear();
                        ifshow = false;
                        initRecycler();
                        setButtonEnable();
                        Toast.makeText(WifiTestActivity.this,"WIFI处于关闭状态",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING:{
                        Log.d(TAG,"正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED:{
                        Log.d(TAG,"已经打开");
                        Log.d("myscan","startScan()");
                        mWifiManager.startScan();
                        sortScaResult();
                        ifshow = true;
                        initRecycler();
                        setButtonEnable();
                        Toast.makeText(WifiTestActivity.this,"WIFI处于开启状态",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING:{
                        Log.d(TAG,"正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN:{
                        Log.d(TAG,"未知状态");
                        break;
                    }
                }
            }else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "--NetworkInfo--" + info.toString());
                if(NetworkInfo.State.DISCONNECTED == info.getState()){//wifi没连接上
                    Log.d(TAG,"wifi没连接上");
                    hidingProgressBar();
                    for(int i = 0;i < realWifiList.size();i++){//没连接上将 所有的连接状态都置为“未连接”
                        realWifiList.get(i).setState(AppContants.WIFI_STATE_UNCONNECT);
                    }
                    if(adapter!=null)
                    adapter.notifyDataSetChanged();//null exp
                }else if(NetworkInfo.State.CONNECTED == info.getState()){//wifi连接上了
                    Log.d(TAG,"wifi连接上了");
                    hidingProgressBar();
                    WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(WifiTestActivity.this);
                    //连接成功 跳转界面 传递ip地址
                    Toast.makeText(WifiTestActivity.this,"wifi连接上了",Toast.LENGTH_SHORT).show();
                    connectType = 1;
                    wifiListSet(connectedWifiInfo.getSSID(),connectType);
                }else if(NetworkInfo.State.CONNECTING == info.getState()){//正在连接
                    Log.d(TAG,"wifi正在连接");
                    showProgressBar();
                    WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(WifiTestActivity.this);
                    connectType = 2;
                    wifiListSet(connectedWifiInfo.getSSID(),connectType );
                }
            }else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())){
                Log.d(TAG,"网络列表变化了");
                if(ifshow)
                wifiListChange();
            }
        }
    }

    /**
     * //网络状态发生改变 调用此方法！
     */
    public void wifiListChange(){
        sortScaResult();
        WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(this);
        if(connectedWifiInfo != null){
            wifiListSet(connectedWifiInfo.getSSID(),connectType);
        }
    }

    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     * @param wifiName
     * @param type
     */
    public void wifiListSet(String wifiName , int type){
        int index = -1;
        WifiBean wifiInfo = new WifiBean();
        if(CollectionUtils.isNullOrEmpty(realWifiList)){
            return;
        }
        for(int i = 0;i < realWifiList.size();i++){
            realWifiList.get(i).setState(AppContants.WIFI_STATE_UNCONNECT);
        }
        Collections.sort(realWifiList);//根据信号强度降序排序
        for(int i = 0;i < realWifiList.size();i++){
            WifiBean wifiBean = realWifiList.get(i);
            String[] ssids = wifiBean.getWifiName().split("\\:");
            //if(!" no password".equals(ssidss[1])) {
            if(index == -1 && ("\"" + ssids[0]+ "\"").equals(wifiName)){
                index = i;
                wifiInfo.setLevel(wifiBean.getLevel());
                wifiInfo.setImageId(wifiBean.getImageId());
                wifiInfo.setIfLock(wifiBean.getIfLock());
                wifiInfo.setWifiName(wifiBean.getWifiName());
                wifiInfo.setCapabilities(wifiBean.getCapabilities());
                //更新AP的连接状态
                if(type == 1){
                    wifiInfo.setState(AppContants.WIFI_STATE_CONNECT);
                }else{
                    wifiInfo.setState(AppContants.WIFI_STATE_ON_CONNECTING);
                }
            }
        }
        if(index != -1){
            realWifiList.remove(index);
            realWifiList.add(0, wifiInfo);
            if(adapter!=null)
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 检查是否已经授予权限
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    public void sortScaResult(){
        List<ScanResult> scanResults = WifiSupport.noSameName(WifiSupport.getWifiScanResult(this));
        realWifiList.clear();
        if(!CollectionUtils.isNullOrEmpty(scanResults)){
            for(int i = 0;i < scanResults.size();i++){
                WifiBean wifiBean = new WifiBean();
                wifiBean.setWifiName(scanResults.get(i).SSID+": "+ getEncryption(scanResults.get(i)));
                wifiBean.setState(AppContants.WIFI_STATE_UNCONNECT);   //只要获取都假设设置成未连接，真正的状态都通过广播来确定
                wifiBean.setCapabilities(scanResults.get(i).capabilities);
                // wifiBean.setLevel(WifiSupport.getLevel(scanResults.get(i).level)+""/*int to String*/);
                wifiBean.setLevel(mWifiManager.calculateSignalLevel(scanResults.get(i).level,4)+"");
                wifiBean.setImageId(getImageId(scanResults.get(i)));
                wifiBean.setIfLock(getVisiable(scanResults.get(i)));
                realWifiList.add(wifiBean);
                //排序
                Collections.sort(realWifiList);//compareTo
                Log.d("myscan","adapter "+adapter);
                if(adapter!=null)
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;   //判断用户是否同意获取权限
                    break;
                }
            }

            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if(WifiSupport.isOpenWifi(WifiTestActivity.this) && mHasPermission){  //如果wifi开关是开 并且 已经获取权限
                 //   Toast.makeText(WifiTestActivity.this,"WIFI处于开启状态",Toast.LENGTH_SHORT).show();
                    initRecycler();
                }else{
                    Toast.makeText(WifiTestActivity.this,"WIFI处于关闭状态或权限获取失败1111",Toast.LENGTH_SHORT).show();
                }
            } else {  //用户不同意权限
                mHasPermission = false;
                Toast.makeText(WifiTestActivity.this,"获取权限失败",Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void showProgressBar() {
        pbWifiLoading.setVisibility(View.VISIBLE);
    }

    public void hidingProgressBar() {
        pbWifiLoading.setVisibility(View.GONE);
    }
}
