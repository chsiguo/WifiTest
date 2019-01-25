package guo.wifilistconnect;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "finger";
    private static final int MY_RECO = 1203;
    StringBuilder stringBuilder;

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    Button button,button1;
    TextView mTextView;
    FingerprintManager mManager;
    KeyguardManager keyguardManager ,mKeyManager;
    private Bundle saveBundle;
    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;
    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveBundle = savedInstanceState;
        setContentView(R.layout.activity_main);
        button =(Button) findViewById(R.id.button);
        button1 = (Button)findViewById(R.id.button1);
        mTextView = (TextView) findViewById(R.id.text_view);
        mManager = getSystemService(FingerprintManager.class);//拿到 fpm
        keyguardManager = getSystemService(KeyguardManager.class);
        mKeyManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        stringBuilder = new StringBuilder();
        stringBuilder.append("SDK version is "+ Build.VERSION.SDK_INT+" （must >= 28）");
        stringBuilder.append("\n");
        stringBuilder.append("isHardwareDetected : "+mManager.isHardwareDetected());
        stringBuilder.append("\n");
        stringBuilder.append("hasEnrolledFingerprints : "+mManager.hasEnrolledFingerprints());
        stringBuilder.append("\n");
        stringBuilder.append("请先通过验证指纹或者强认证，然后使用Wifi测试功能");
        stringBuilder.append("\n");
        //updateView();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canAuthenticate();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("锁屏密码", "测试锁屏密码");
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
                }
            }
        });
    }

    void updateView(){
        boolean ifhasFp = false;
        ifhasFp = mManager.hasEnrolledFingerprints();
        button.setEnabled(ifhasFp && Build.VERSION.SDK_INT>=Build.VERSION_CODES.P);
        button1.setEnabled(ifhasFp);
        mTextView.setText(stringBuilder.toString());
        if(!ifhasFp) {
            Log.d("mww", "here ");
            new AlertDialog.Builder(this,R.style.AlertDialog).setTitle("Fingerprint Settings")
                    .setMessage("need to enroll a fingerprint").setIcon(R.drawable.ic_fingerprint_24dp).setCancelable(false)
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent("android.settings.SECURITY_SETTINGS");
                            startActivityForResult(intent,MY_RECO);
                            Toast.makeText(MainActivity.this, "remeber to Home by pressing BACK", Toast.LENGTH_LONG).show();
                            //  finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }

    }
     void  canAuthenticate() {
        //这里的dialog依赖于authenticate()
        mBiometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("指纹验证")
                .setDescription("描述：这是系统提供的提示对话框")
                .setNegativeButton("取消", getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "Cancel button clicked");
                    }
                })
                .build();

        mCancellationSignal = new CancellationSignal();
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                //handle cancel result
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Canceled");
            }
        });

        mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();

                Log.i(TAG, "onAuthenticationError " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "onSucceeded", Toast.LENGTH_SHORT).show();
            //    Log.i(TAG, "onAuthenticationSucceeded " + result.toString());
                Intent intent = new Intent(MainActivity.this,WifiTestActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "onFailed", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onAuthenticationFailed ");
            }
        };

        mBiometricPrompt.authenticate(mCancellationSignal, getMainExecutor(), mAuthenticationCallback);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("mystart","resultCode："+resultCode+" Intent:"+data);

        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS&&resultCode==-1) {
            // Challenge completed, proceed with using cipher
           // Toast.makeText(MainActivity.this, "congratulations", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this,WifiTestActivity.class);
            startActivity(intent);
        }else if(requestCode == MY_RECO){
//
        }
    }
    //the third sdk 并不直接支持FingerprintDialogImpl.java

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // finish();
    }
}
