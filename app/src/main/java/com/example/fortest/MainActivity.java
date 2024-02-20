package com.example.fortest;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static boolean isInitialized = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private Button ssrBtnSettings, ephBtnSettings, obsBtnSettings, correctBtnSettings;
    private EditText ntripServerEditText, ntripPortEditText, ntripMountEditText, ntripUserEditText, ntripPasswordEditText;
    private EditText tcpServerEditText, tcpPortEditText;
    private Switch solveSwitch;
    private TextView scrollableTextView;
    private static String SSR_PREFERENCES_NAME = "ssrNtripSettingsPrefs";
    private static String EPH_PREFERENCES_NAME = "ephNtripSettingsPrefs";
    private static String OBS_PREFERENCES_NAME = "obsNtripSettingsPrefs";
    private static String CORRECT_PREFERENCES_NAME = "correctNtripSettingsPrefs";
    private static final int SSR_LOGIN_SUCCESS = 1;
    private static final int EPH_LOGIN_SUCCESS = 2;
    private static final int OBS_LOGIN_SUCCESS = 3;
    private static final int SSR_LOGIN_FAIL = 1001;
    private static final int EPH_LOGIN_FAIL = 1002;
    private static final int OBS_LOGIN_FAIL = 1003;
    private LinearLayout loginLayout;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssrBtnSettings = findViewById(R.id.btnSSRSettings);
        ephBtnSettings = findViewById(R.id.btnEPHSettings);
        obsBtnSettings = findViewById(R.id.btnOBSSettings);
        correctBtnSettings = findViewById(R.id.btnCORRECTSettings);
        solveSwitch = findViewById(R.id.solve_switch);
        scrollableTextView = findViewById(R.id.scrollable1_textview);
        loginLayout = findViewById(R.id.login_body_layout);
        mScrollView = findViewById(R.id.login_body_scrollview);

        solveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    Intent intent = new Intent(MainActivity.this, NtripActivity.class);
//                    startActivity(intent);
                    new NtripConnectTaskSsr().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    show_Login_msg("Start logging in and connecting to the channel!");
                } else {

                }
            }
        });

        ssrBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNtripSettingsDialog(SSR_PREFERENCES_NAME);
            }
        });

        ephBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNtripSettingsDialog(EPH_PREFERENCES_NAME);
            }
        });

        obsBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNtripSettingsDialog(OBS_PREFERENCES_NAME);
            }
        });

        correctBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNtripSettingsDialog(CORRECT_PREFERENCES_NAME);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "no permission to read");
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        } else {
            copyAssets();
        }
    }

    private void copyAssets() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getPath();
        Log.i(TAG, "开始拷贝文件");
        FileUtils.copyAssetsToStorage(this, "configures", path);
        Log.i(TAG, "结束拷贝文件");

//        String mode = "kinematic";
//        double[] pos = {-2258208.214700, 5020578.919700, 3210256.397500};
//        double[] enu  = new double[3];
//        SDK.SDKInit(mode,"", pos, enu, 7, 1,path);

//        Log.i(TAG, "SDKInit over");
//        isInitialized = true;
//        Log.i(TAG, "TCP接收数据开始执行");
//        new TcpActivity.TcpClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.i(TAG, "Ssr接收数据开始执行");
//        new NtripActivity.NtripConnectTaskSsr().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.i(TAG, "Obs接收数据开始执行");
//        new NtripActivity.NtripConnectTaskObs().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                copyAssets();
            } else {
                Log.e(TAG, "Write external storage permission denied");
            }
        }
    }

    private void showNtripSettingsDialog(String ntripPreferencesName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.setntrip, null);

        ntripServerEditText = dialogView.findViewById(R.id.ntrip_server);
        ntripPortEditText = dialogView.findViewById(R.id.ntrip_port);
        ntripMountEditText = dialogView.findViewById(R.id.ntrip_mp);
        ntripUserEditText = dialogView.findViewById(R.id.ntrip_user);
        ntripPasswordEditText = dialogView.findViewById(R.id.ntrip_psw);
        loadSavedInfo(ntripPreferencesName);

        builder.setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 处理OK按钮点击事件
                        saveNtripUserInfo(ntripPreferencesName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 处理Cancel按钮点击事件
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadSavedInfo(String ntripPreferencesName) {
        SharedPreferences preferences = getSharedPreferences(ntripPreferencesName, Context.MODE_PRIVATE);
//        if (ntripPreferencesName.contains("SSR")) {
        ntripServerEditText.setText(preferences.getString("ntripServer", ""));
        ntripPortEditText.setText(preferences.getString("ntripPort", ""));
        ntripMountEditText.setText(preferences.getString("ntripMountPoint", ""));
        ntripUserEditText.setText(preferences.getString("ntripUser", ""));
        ntripPasswordEditText.setText(preferences.getString("ntripPassword", ""));
//        }
    }

    private void saveNtripUserInfo(String ntripPreferencesName) {
        SharedPreferences preferences = getSharedPreferences(ntripPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
//        if (ntripPreferencesName.contains("SSR")) {
        editor.putString("ntripServer", ntripServerEditText.getText().toString());
        editor.putString("ntripPort", ntripPortEditText.getText().toString());
        editor.putString("ntripMountPoint",ntripMountEditText.getText().toString());
        editor.putString("ntripUser", ntripUserEditText.getText().toString());
        editor.putString("ntripPassword",ntripPasswordEditText.getText().toString());
//        }
        editor.apply();
    }

    public void show_Login_msg(String str){
        Log.i(TAG ," show_Login_msg: " + str);
        if (scrollableTextView.getText().toString() == "") {
            scrollableTextView.setText(str);
        } else {
            scrollableTextView.setText(scrollableTextView.getText().toString() + "\n" +str);
        }
        if (scrollableTextView.getText().toString().length()>4000) {
            String nstr = scrollableTextView.getText().toString();
            nstr = nstr.substring(80,nstr.length());
            scrollableTextView.setText(nstr);
        }
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SSR_LOGIN_SUCCESS:
                    // 处理接收到的消息
                    break;
                default:
                    break;
            }
            return false;
        }
    });
}