package guo.wifilistconnect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class CurrentWifiInfo{
        public WifiInfo wifiInfo;
        public DhcpInfo dhcpInfo;
        String wifiProperty;

        public String getInfo(WifiManager wifiManager,ScanResult srtse,String ssid){

            dhcpInfo = wifiManager.getDhcpInfo();
            wifiInfo = wifiManager.getConnectionInfo();

            String ip = srtse!=null?srtse.BSSID:null;
            int level = srtse!=null?srtse.level:-1;
            wifiProperty = "当前连接WIFI信息如下:\n"+
                    "ssid:"+ ssid + '\n' +
                    "macAddress(AP_def):"+wifiInfo.getMacAddress()+'\n'+
                    "macAddress(AP)"+ip+'\n' +
                    "macAddress(device)"+getMacAddr()+'\n' +
                    "signal level: "+level+" = (LEV)"+wifiManager.calculateSignalLevel(level,4)+'\n' +
                    "ip:" + FormatString(wifiInfo.getIpAddress()) + '\n' +
                    // "ip:" + FormatString(dhcpInfo.ipAddress) + '\n' +
                    "mask:" + FormatString(dhcpInfo.netmask) + '\n' +
                    "netgate:" + FormatString(dhcpInfo.gateway) + '\n' +
                    "dns:" + FormatString(dhcpInfo.dns1) + '\n' +
                    "rssi:"+ wifiInfo.getRssi() +'\n' +
                    DisByRssi(wifiInfo.getRssi());

            return wifiProperty;
        }
        //下面用别的方法去取得真正的mac地址，（这方法是要打开wifi才能获取得到）
        public static String macAddress() {
            String address = null;
            try {
                // 把当前机器上的访问网络接口的存入 Enumeration集合中
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                Log.d("TEST_BUG", " interfaceName = " + interfaces );
                while (interfaces.hasMoreElements()) {
                    NetworkInterface netWork = interfaces.nextElement();
                    // 如果存在硬件地址并可以使用给定的当前权限访问，则返回该硬件地址（通常是 MAC）。
                    byte[] by = netWork.getHardwareAddress();
                    if (by == null || by.length == 0) {
                        continue;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (byte b : by) {
                        builder.append(String.format("%02X:", b));
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    String mac = builder.toString();
                    Log.d("TEST_BUG", "interfaceName="+netWork.getName()+", mac="+mac);
                    // 从路由器上在线设备的MAC地址列表，可以印证设备Wifi的 name 是 wlan0
                    if (netWork.getName().equals("wlan0")) {
                        Log.d("TEST_BUG", " interfaceName ="+netWork.getName()+", mac="+mac);
                        address = mac;
                    }
                }
            }catch (Exception ex) {
            }
            return address;
        }

        public static String getMacAddr() {
            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:",b));
                    }
                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            } catch (Exception ex) {
            }
            return "02:00:00:00:00:00";
        }

        public Double DisByRssi(int rssi) {
            int iRssi = Math.abs(rssi);
            double power = (iRssi - 35) / (10 * 2.1);
            return Math.pow(10, power);
        }
        public String FormatString(int value){
            String strValue ="";
            byte[] ary = intToByteArray(value);
            for(int i = ary.length-1;i>=0;i--){
                strValue += (ary[i]& 0xFF);
                if(i>0){
                    strValue +=".";
                }
            }
            return strValue;
        }
        public static byte[] intToByteArray(int a) {
            return new byte[]{
                    (byte) ((a >> 24) & 0xFF),
                    (byte) ((a >> 16) & 0xFF),
                    (byte) ((a >> 8) & 0xFF),
                    (byte) (a & 0xFF)
            };
        }
}
