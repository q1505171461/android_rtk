package com.example.fortest;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Base64;

import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NtripActivity extends AppCompatActivity {
    private static final String TAG = "NtripActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntrip);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button ntripButton = findViewById(R.id.btnConnectNtrip);
        ntripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在 onCreate 方法中执行异步任务
                Log.i(TAG, "建立ntrip");
                new NtripConnectTaskObs().execute();
            }
        });
    }

    public static class NtripConnectTaskObs extends AsyncTask<Void, Void, Void> {
        private static final String NTRIP_SERVER_IP = "119.96.165.202";
        private static final int NTRIP_SERVER_PORT = 8600;
        private Socket socket;
        private  boolean isRunning = false; // 控制循环的标志
        private static final String MOUNTPOINT = "TEST";
        private static final String USERNAME = "test";
        private static final String PASSWORD = "test";

        @Override
        protected Void doInBackground(Void... voids) {
            isRunning = true;
            try {
                // 创建Socket连接
                socket = new Socket(NTRIP_SERVER_IP, NTRIP_SERVER_PORT);
                // 获取输入输出流
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                InputStream inputStream = socket.getInputStream();

                // 发送连接请求，包括挂载点和账号密码
                sendConnectRequest(out, MOUNTPOINT, USERNAME, PASSWORD);

                // 循环读取服务器发送的数据
                String response = in.readLine();
                Log.d(TAG, "Received Obs data from Ntrip server: " + response);
                while (isRunning) {
                    handleReceivedData(inputStream);
                }
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
            return null;
        }

        private void handleReceivedData(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
                // 服务器关闭连接或发生其他错误
                return;
            }
            // 处理从服务器获取的数据，例如更新UI或执行其他操作
            byte[] receivedBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
            System.out.print("收到Obs数据:");
            for (byte b : receivedBytes) {
                System.out.print(String.format("%02X ", b));

                byte[] buff_r = new byte[1024];
                if (1 == SDK.IOInputObsData(b)) {
                    SDK.SDKRetrieve("NMEA_GGA", buff_r, 104);
                    Log.d(TAG,  new String(buff_r, 0, 104, StandardCharsets.UTF_8));
                }
            }
            System.out.println();
        }

        private void sendConnectRequest(PrintWriter out, String mountpoint, String username, String password) {
            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append("GET /").append(mountpoint).append(" HTTP/1.0\r\n");
            requestBuilder.append("User-Agent: NTRIP NTRIPClient/1.0\r\n");
            // 添加账号密码信息
            if (!username.isEmpty()) {
                String authHeader = "Authorization: Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
                requestBuilder.append(authHeader).append("\r\n");
            }
            requestBuilder.append("\r\n");

            // 发送请求
            out.println(requestBuilder.toString());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isRunning = false; // 结束循环
        }
    }
    public static class NtripConnectTaskSsr extends AsyncTask<Void, Void, Void> {
        //域名: ssr.kplgnss.com (推荐使用), IP: 103.143.19.54
        //端口: 8060
        //源节点: SSRKPL0CLK
        private static final String NTRIP_SERVER_IP = "103.143.19.54";
        private static final int NTRIP_SERVER_PORT = 8060;
        private Socket socket;
        private  boolean isRunning = false; // 控制循环的标志
        // "test:test@119.96.223.176:8007/SSR_COM_BAK"
        private static final String MOUNTPOINT = "SSRKPL0CLK";
        private static final String USERNAME = "test052";
        private static final String PASSWORD = "46391";

        @Override
        protected Void doInBackground(Void... voids) {
            isRunning = true;
            try {
                // 创建Socket连接
                socket = new Socket(NTRIP_SERVER_IP, NTRIP_SERVER_PORT);
                // 获取输入输出流
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                InputStream inputStream = socket.getInputStream();

                // 发送连接请求，包括挂载点和账号密码
                sendConnectRequest(out, MOUNTPOINT, USERNAME, PASSWORD);

                // 循环读取服务器发送的数据
                String response = in.readLine();
                Log.d(TAG, "Received Obs data from Ntrip serverSSR: " + response);
                while (isRunning) {
                    handleReceivedData(inputStream);
                }
                // 关闭连接
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
            return null;
        }

        private void handleReceivedData(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
                // 服务器关闭连接或发生其他错误
                return;
            }

            // 处理从服务器获取的数据，例如更新UI或执行其他操作
            byte[] receivedBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
            System.out.print("收到Ssr数据:");
            for (byte b : receivedBytes) {
                System.out.print(String.format("%02X ", b));
                SDK.IOInputSsrData(b);
            }
            System.out.println();
            byte[] buff_r = new byte[1024000];
            SDK.SDKRetrieve("SSR-ALL", buff_r , 0);
            Log.d(TAG,  new String(buff_r,  StandardCharsets.UTF_8));
        }
        private void sendConnectRequest(PrintWriter out, String mountpoint, String username, String password) {
            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append("GET /").append(mountpoint).append(" HTTP/1.0\r\n");
            requestBuilder.append("User-Agent: NTRIP NTRIPClient/1.0\r\n");

            // 添加账号密码信息
            if (!username.isEmpty()) {
                String authHeader = "Authorization: Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
                requestBuilder.append(authHeader).append("\r\n");
            }

            requestBuilder.append("\r\n");

            // 发送请求
            out.println(requestBuilder.toString());
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isRunning = false; // 结束循环
        }
    }
}