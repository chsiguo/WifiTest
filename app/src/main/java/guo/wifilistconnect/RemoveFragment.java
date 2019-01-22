package guo.wifilistconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RemoveFragment extends DialogFragment {
    private TextView name;
    public String ssid;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_remove,null);
        name = view.findViewById(R.id.name);
        name.setText("AP: "+ssid);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialog);
//        DisplayMetrics metrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        double width = metrics.widthPixels * 0.9;
//        double height = metrics.heightPixels * 0.4;
        builder.setTitle("if remove the AP or not?")
                .setMessage("如果移除如下AP后，需要重新设置该AP。" )
                .setIcon(R.drawable.ic_delete).setCancelable(true);
        builder.setView(view).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WifiSupport.RemoveConfig(name.getText().toString(),getActivity());
//                Toast.makeText(getActivity()," name "+name.getText().toString(),
//                        Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        //  alertDialog.show();
        //这里设置大小请注意：show()需要在setLayout()之前。
     //   builder.create().getWindow().setLayout((int)width, ViewGroup.LayoutParams.WRAP_CONTENT);
        return builder.create();
    }

    public static boolean ifConfig(String SSID,Context context){
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs ;
        existingConfigs= mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return true;
            }
            //else {
//                if(getWifiCipher(capabilities)==WifiCipherType.WIFICIPHER_NOPASS) {
//                    WifiConfiguration config = createWifiConfig(SSID, null, WifiCipherType.WIFICIPHER_NOPASS);
//                    addNetWork(config, context);
//                }
//                else {
//                    noConfigurationWifi();
            // }}
        }
        return false;

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
