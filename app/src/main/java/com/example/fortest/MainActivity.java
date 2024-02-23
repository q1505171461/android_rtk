package com.example.fortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private EditText ntripServerEditText, ntripPortEditText, ntripMountEditText, ntripUserEditText, ntripPasswordEditText;
    private TextView scrolstautsTextView,scrolGGATextView;
    private static final String SSR_PREFERENCES_NAME = "ssrNtripSettingsPrefs";
    private static final String EPH_PREFERENCES_NAME = "ephNtripSettingsPrefs";
    private static final String OBS_PREFERENCES_NAME = "obsNtripSettingsPrefs";
    NtripConnectTask taskSSR,taskOBS,taskEPH;

    private boolean hadSdkInit = false;
    private ScrollView statusScrollView, GGAScrollView;

    // 图表
    private LineChart lineChart;
    private final ArrayList<Entry> entries1 = new ArrayList<>();
    private final ArrayList<Entry> entries2 = new ArrayList<>();
    private final ArrayList<Entry> entries3 = new ArrayList<>();
    private final Handler chartHandler = new Handler();
    private Runnable runnable;
    private long startTime;
    private LinearLayout logShowBodyLayout;
    private LinearLayout llMsgHeader;
    private LinearLayout lineChartLayout;
    private RadioButton logShowRadioButton, chartShowRadioButton;
    private CheckBox ssrCheckBox, ggaCheckBox;

    // 坐标系
    private EditText editTextXLeft, editTextXRight, editTextYTop, editTextYBottom;
    private GraphView graphView;
    private Button btnChartXY;
    private float defaultXLeft, defaultXRight, defaultYTop, defaultYBottom;
//    private Boolean showGGA = false, showSSR = false;
    List<GraphView.Point> coordinateList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch solveSwitch = findViewById(R.id.solve_switch);

        statusScrollView = findViewById(R.id.scrollview_status);
        scrolstautsTextView = findViewById(R.id.scrollable_textview);
        GGAScrollView = findViewById(R.id.scrollView_GGA);
        scrolGGATextView = findViewById(R.id.scrollable_GGA_textview);

        logShowBodyLayout = findViewById(R.id.scrollable_GGA_textview_layout);
        lineChartLayout = findViewById(R.id.line_chart_layout);
        llMsgHeader = findViewById(R.id.ll_msg_header);
        chartShowRadioButton = findViewById(R.id.chart_show);
        logShowRadioButton = findViewById(R.id.log_show);
        lineChart = findViewById(R.id.line_chart);
        ssrCheckBox = findViewById(R.id.checkbox_ssr);
        ggaCheckBox = findViewById(R.id.checkbox_gga);
        findViewById(R.id.btnIntvSettings).setOnClickListener(view -> {
            Config.INSTANCE.setIntv(((EditText) findViewById(R.id.editTextSamplingRate)).getText().toString());
            if (Config.INSTANCE.getIntv().equals("")){
                Config.INSTANCE.setIntv("1");
            }
            double[] res = Utils.xyz2blh(new double[]{-2258208.214700, 5020578.919700, 3210256.397500},1,0,0,0,0,0);
            System.out.printf("ffffffff %.10f %.10f %.10f\n", res[0]*57.295779513,res[1]*57.295779513,res[2]);
            res = Utils.blhxyz( res[0] ,res[1] , res[2], 0,0 );
            System.out.printf("ffffffff %.10f %.10f %.10f\n", res[0] ,res[1] ,res[2]);
            double[][] array = Utils.rot_xyz2enu_rad( 0.53086257,1.99347594);
            for (int i = 0; i < array.length; i++) {
                System.out.print("ffffffff");
                for (int j = 0; j < array[i].length; j++) {
                    System.out.print(array[i][j] + " ");
                }
                System.out.println();
            }
            Utils.main2();
        });
        logShowRadioButton.setSelected(true);
        lineChartLayout.setVisibility(View.GONE);

        editTextXLeft = findViewById(R.id.editTextXLeft);
        editTextXRight = findViewById(R.id.editTextXRight);
        editTextYTop = findViewById(R.id.editTextYTop);
        editTextYBottom = findViewById(R.id.editTextYBottom);
        graphView = findViewById(R.id.graphImageView);
        btnChartXY = findViewById(R.id.confirm_button);

        btnChartXY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String xLeftStr = editTextXLeft.getText().toString();
                String xRightStr = editTextXRight.getText().toString();
                String yTopStr = editTextYTop.getText().toString();
                String yBottomStr = editTextYBottom.getText().toString();

                if (TextUtils.isEmpty(xLeftStr) || TextUtils.isEmpty(xRightStr) || TextUtils.isEmpty(yTopStr) || TextUtils.isEmpty(yBottomStr)) {
                    // 如果有一个值为空，弹出 Toast 提示用户
                    Toast.makeText(MainActivity.this, "请确保所有值都已输入", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        defaultXLeft = Integer.parseInt(xLeftStr);
                        defaultXRight = Integer.parseInt(xRightStr);
                        defaultYTop = Integer.parseInt(yTopStr);
                        defaultYBottom = Integer.parseInt(yBottomStr);

                        graphView.setAxisRange(defaultXLeft, defaultXRight, defaultYBottom, defaultYTop);
                    } catch (NumberFormatException e) {
                        // 如果转换失败，弹出 Toast 提示用户
                        Toast.makeText(MainActivity.this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        defaultSetting();
        solveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                show_Status_msg(logWithTime("connecting to the channel ..."));
                testupdateChart();
//                updateChartData(0,0,0);

                testGraph();
                start();
            }else {
                end();
            }
        });

        findViewById(R.id.btnSSRSettings).setOnClickListener(v -> showNtripSettingsDialog(SSR_PREFERENCES_NAME));
        findViewById(R.id.btnEPHSettings).setOnClickListener(v -> showNtripSettingsDialog(EPH_PREFERENCES_NAME));
        findViewById(R.id.btnOBSSettings).setOnClickListener(v -> showNtripSettingsDialog(OBS_PREFERENCES_NAME));

        chartShowRadioButton.setOnClickListener(view -> logShowOpenLayout());
        logShowRadioButton.setOnClickListener(view -> showChartLayout());

        // 图表初始化
        setupChart();
        startTime = System.currentTimeMillis();

        ggaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
//                    showGGA = true;
            } else {
//                    showGGA = false;
            }
        });

        ssrCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
//                    showSSR = true;
            } else {
//                    showSSR = false;
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "no permission to read");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        } else {
            copyAssets();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        entries1.clear();
        entries2.clear();
        entries3.clear();
    }

    private void copyAssets() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getPath();
        Log.i(TAG, "开始拷贝文件");
        Utils.copyAssetsToStorage(this, "configures", path);
        Log.i(TAG, "结束拷贝文件");
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
                .setPositiveButton("OK", (dialog, which) -> saveNtripUserInfo(ntripPreferencesName))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadSavedInfo(String ntripPreferencesName) {
        SharedPreferences preferences = getSharedPreferences(ntripPreferencesName, Context.MODE_PRIVATE);
        ntripServerEditText.setText(preferences.getString("IP", ""));
        ntripPortEditText.setText(preferences.getString("Port", ""));
        ntripMountEditText.setText(preferences.getString("MountPoint", ""));
        ntripUserEditText.setText(preferences.getString("User", ""));
        ntripPasswordEditText.setText(preferences.getString("Password", ""));
    }

    private void saveNtripUserInfo(String ntripPreferencesName) {
        SharedPreferences preferences = getSharedPreferences(ntripPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("IP", ntripServerEditText.getText().toString());
        editor.putString("Port", ntripPortEditText.getText().toString());
        editor.putString("MountPoint",ntripMountEditText.getText().toString());
        editor.putString("User", ntripUserEditText.getText().toString());
        editor.putString("Password",ntripPasswordEditText.getText().toString());
        editor.apply();
    }

    public void show_Status_msg(String str){
        Log.i(TAG ," show_Login_msg: " + str);
        if (scrolstautsTextView.getText().toString().equals("")) {
            scrolstautsTextView.setText(str);
        } else {
            String con = scrolstautsTextView.getText().toString() + "\n" +str;
            scrolstautsTextView.setText(con.substring(Math.max(con.length() - Config.SCROLLVIEW_MAX_TEXT_LENGTH, 0)));
        }
        statusScrollView.post(() -> statusScrollView.fullScroll(View.FOCUS_DOWN));
    }

    public void show_GGA_or_SSR_msg(String str){
        Log.i(TAG ," show_GGA_msg: " + str);
        String con = scrolGGATextView.getText().toString() + str;
        scrolGGATextView.setText(con.substring(Math.max(con.length() - Config.SCROLLVIEW_MAX_TEXT_LENGTH, 0)));
        GGAScrollView.post(() -> GGAScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private final Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case Config.SSR_LOGIN_SUCCESS:
                show_Status_msg(logWithTime("Received SSR data."));
                break;
            case Config.EPH_LOGIN_SUCCESS:
                show_Status_msg(logWithTime("Received EPH data."));
                break;
            case Config.OBS_LOGIN_SUCCESS:
                show_Status_msg(logWithTime("Received OBS data."));
                break;
            case Config.MSG_GGA:
            case Config.MSG_SSR:
                show_GGA_or_SSR_msg(msg.obj.toString());
                break;
            default:
                break;
        }
        return false;
    });

    public void start() {
        SharedPreferences SSRsharedPreferences = getSharedPreferences(SSR_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences EPHsharedPreferences = getSharedPreferences(EPH_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences OBSsharedPreferences = getSharedPreferences(OBS_PREFERENCES_NAME, MODE_PRIVATE);
        Map<String, String> SSRhashMap = new HashMap<>();
        Map<String, String> EPHhashMap = new HashMap<>();
        Map<String, String> OBShashMap = new HashMap<>();
        for (String argname : Config.INSTANCE.getConnectConfig()) {
            if ("badarg".equals(SSRsharedPreferences.getString(argname, "badarg"))
                    || "badarg".equals(EPHsharedPreferences.getString(argname, "badarg"))
                    || "badarg".equals(OBSsharedPreferences.getString(argname, "badarg"))){
                show_Status_msg("Ntrip config error");
                return;
            }else{
                SSRhashMap.put(argname, SSRsharedPreferences.getString(argname,"badarg"));
                EPHhashMap.put(argname, EPHsharedPreferences.getString(argname,"badarg"));
                OBShashMap.put(argname, OBSsharedPreferences.getString(argname,"badarg"));
            }
        }
        String mode = "kinematic";
        double[] pos = {-2258208.214700, 5020578.919700, 3210256.397500};
        double[] enu  = new double[3];
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getPath();
        Log.i(TAG, "SDKInit begin");
        if (!hadSdkInit){
            SDK.SDKInit(mode,"", pos, enu, 7, 1,path);
            hadSdkInit = true;
        }else {
            SDK.SDKRestart();
        }
        int intv = Integer.parseInt(Config.INSTANCE.getIntv());
        SDK.SDKSetIntv(intv);
        Log.i(TAG, "SDKInit over");
        taskEPH = new NtripConnectTaskEph(EPHhashMap, mHandler);
        taskSSR = new NtripConnectTaskSsr(SSRhashMap, mHandler);
        taskOBS = new NtripConnectTaskObs(OBShashMap, mHandler);
        Log.i(TAG, "Eph接收数据开始执行");
        taskEPH.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i(TAG, "Ssr接收数据开始执行");
        taskSSR.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i(TAG, "Obs接收数据开始执行");
        taskOBS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void end() {
        Log.i(TAG, "Eph接收数据结束执行");
        taskEPH.exit();
        Log.i(TAG, "Ssr接收数据结束执行");
        taskSSR.exit();
        Log.i(TAG, "Obs接收数据结束执行");
        taskOBS.exit();
    }
    void defaultSetting(){
        SharedPreferences SSRsharedPreferences = getSharedPreferences(SSR_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = SSRsharedPreferences.edit();
        editor.putString("IP", "103.143.19.54");
        editor.putString("Port", "8060");
        editor.putString("MountPoint","SSRKPL0CLK");
        editor.putString("User", "test052");
        editor.putString("Password","46391");
        editor.apply();
        SharedPreferences EPHsharedPreferences = getSharedPreferences(EPH_PREFERENCES_NAME, MODE_PRIVATE);
        editor = EPHsharedPreferences.edit();
        editor.putString("IP", "119.96.169.117");
        editor.putString("Port", "7001");
        editor.putString("Port", "12101");
        editor.putString("MountPoint","RTCM32EPH");
        editor.putString("User", "kplcors");
        editor.putString("Password","123");
        editor.apply();
        SharedPreferences OBSsharedPreferences = getSharedPreferences(OBS_PREFERENCES_NAME, MODE_PRIVATE);
        editor = OBSsharedPreferences.edit();
        editor.putString("IP", "119.96.165.202");
        editor.putString("Port", "8600");
        editor.putString("MountPoint","TEST");
        editor.putString("User", "test");
        editor.putString("Password","test");
        editor.apply();
    }

    String logWithTime(String logContent){
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);
        return formattedDate + ": " +logContent;
    }

    private void showChartLayout() {
        findViewById(R.id.scrollable_GGA_textview_layout).setVisibility(View.VISIBLE);
        llMsgHeader.setVisibility(View.VISIBLE);
        lineChartLayout.setVisibility(View.GONE);
    }

    private void logShowOpenLayout() {
        logShowBodyLayout.setVisibility(View.GONE);
        llMsgHeader.setVisibility(View.GONE);
        lineChartLayout.setVisibility(View.VISIBLE);
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long timestamp = startTime + (long) value * 1000;
                return dateFormat.format(new Date(timestamp));
            }
        });

//        YAxis yAxisLeft = lineChart.getAxisLeft();
//        YAxis yAxisRight = lineChart.getAxisRight();
//        yAxisLeft.setAxisMinimum(0f); // 如果你的数据永远不会小于0
//        yAxisRight.setAxisMinimum(0f);
//        updateChartData(0, 0, 0);

        // only for test
        lineChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
    }

    private void updateChartData(float value1, float value2, float value3) {
        long currentTimeMillis = System.currentTimeMillis();

        // 向数据集添加新的数据点
        entries1.add(new Entry(currentTimeMillis, value1));
        entries2.add(new Entry(currentTimeMillis, value2));
        entries3.add(new Entry(currentTimeMillis, value3));

        // 创建数据集并添加数据
        LineDataSet dataSet1 = new LineDataSet(entries1, "E");
        LineDataSet dataSet2 = new LineDataSet(entries2, "N");
        LineDataSet dataSet3 = new LineDataSet(entries3, "U");

        // 设置数据集样式（可根据需要自定义）
        dataSet1.setColor(Color.BLUE);
        dataSet1.setCircleColor(Color.BLUE);
        dataSet2.setColor(Color.RED);
        dataSet2.setCircleColor(Color.RED);
        dataSet3.setColor(Color.GREEN);
        dataSet3.setCircleColor(Color.GREEN);

        // 创建 LineData 对象并添加数据集
        LineData lineData = new LineData(dataSet1, dataSet2, dataSet3);
        lineChart.setData(lineData);
        lineChart.invalidate(); // 刷新图表
    }

    private void testupdateChart() {
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // seconds

                entries1.add(new Entry(elapsedTime, 1 + entries1.size()));
                entries2.add(new Entry(elapsedTime, 2 + entries2.size()));
                entries3.add(new Entry(elapsedTime, 3 + entries3.size()));

                LineDataSet dataSet1 = new LineDataSet(entries1, "E");
                dataSet1.setColor(Color.BLUE);
                dataSet1.setCircleColor(Color.BLUE);

                LineDataSet dataSet2 = new LineDataSet(entries2, "N");
                dataSet2.setColor(Color.RED);
                dataSet2.setCircleColor(Color.RED);

                LineDataSet dataSet3 = new LineDataSet(entries3, "U");
                dataSet3.setColor(Color.GREEN);
                dataSet3.setCircleColor(Color.GREEN);

                LineData data = new LineData(dataSet1, dataSet2, dataSet3);
                lineChart.setData(data);
                lineChart.invalidate(); // Refresh the chart

                chartHandler.postDelayed(this, 3000);
            }
        };
        chartHandler.postDelayed(runnable, 3000);
    }


    private void testGraph() {
        coordinateList.add(new GraphView.Point(1,1));
        coordinateList.add(new GraphView.Point(2,2));
        coordinateList.add(new GraphView.Point(-2,2));
        coordinateList.add(new GraphView.Point(2,-2));
        coordinateList.add(new GraphView.Point(-2,-2));

        graphView.setCoordinates(coordinateList);
    }
}

