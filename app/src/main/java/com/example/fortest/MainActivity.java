package com.example.fortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static   boolean isInitialized = false;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button tcpButton = findViewById(R.id.bt1);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button ntrip1Button = findViewById(R.id.bt2);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button ntrip2Button = findViewById(R.id.bt3);
        tcpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TcpActivity.class);
                startActivity(intent);
            }
        });
        ntrip1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NtripActivity.class);
                startActivity(intent);
            }
        });
    }
    private void copyAssets() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getPath();
        Log.i(TAG, "开始拷贝文件");
        Log.i(TAG, Config.INSTANCE.getNtrip_ssr_ip());
        FileUtils.copyAssetsToStorage(this, "configures", path);
        Log.i(TAG, "结束拷贝文件");
        begin();
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

    public void begin() {
//        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
//
//        String ntrip_ssr_ip   =sharedPreferences.getString("ntrip_ssr_ip","");
//        String ntrip_ssr_port =sharedPreferences.getString("ntrip_ssr_port","");
//        String ntrip_obs_ip   =sharedPreferences.getString("ntrip_obs_ip","");
//        String ntrip_obs_port =sharedPreferences.getString("ntrip_obs_port","");
//        int intv = sharedPreferences.getInt("intv",1);

        String mode = "kinematic";
        double[] pos = {-2258208.214700, 5020578.919700, 3210256.397500};
        double[] enu  = new double[3];

        String path = Objects.requireNonNull(getExternalFilesDir(null)).getPath();
        SDK.SDKInit(mode,"", pos, enu, 7, 1,path);


        SDK.SDKSetIntv(1);
        Log.i(TAG, "SDKInit over");
        isInitialized = true;
        Log.i(TAG, "TCP接收数据开始执行");
        new TcpActivity.TcpClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i(TAG, "Ssr接收数据开始执行");
        new NtripActivity.NtripConnectTaskSsr().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i(TAG, "Obs接收数据开始执行");
        new NtripActivity.NtripConnectTaskObs().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}