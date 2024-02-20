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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button ntripButton = findViewById(R.id.bt22);
        ntripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在 onCreate 方法中执行异步任务
                Log.i(TAG, "建立ntrip");
//                new NtripConnectTaskObs().execute();
            }
        });
    }
}
class NtripConnectTaskObs extends NtripConnectTask{
    protected void handleReceivedData(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return;
        }
        // 处理从服务器获取的数据，例如更新UI或执行其他操作
        byte[] receivedBytes = new byte[bytesRead];
        System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
        System.out.print("收到Obs数据:");
        for (byte b : receivedBytes) {
            System.out.print(String.format("%02X ", b));
            if (1 == SDK.IOInputObsData(b)) {
                Log.d(TAG, SDK.SDKRetrieve("NMEA_GGA",  104));
            }
        }
        System.out.println();
    }
    protected void setConfig(){
        NTRIP_SERVER_IP = "119.96.165.202";
        NTRIP_SERVER_PORT = 8600;
        MOUNTPOINT = "TEST";
        USERNAME = "test";
        PASSWORD = "test";
    }
}
class NtripConnectTaskSsr extends NtripConnectTask {
    //域名: ssr.kplgnss.com (推荐使用), IP: 103.143.19.54
    //端口: 8060
    //源节点: SSRKPL0CLK

    protected void handleReceivedData(InputStream inputStream) throws IOException {
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
        String logstr = SDK.SDKRetrieve("SSR-ALL",  0);
        for (String line : logstr.split("\n")){
            Log.i("ssrlog", line);
        }
    }

    protected void setConfig(){
        NTRIP_SERVER_IP = "103.143.19.54";
        NTRIP_SERVER_PORT = 8060;
        MOUNTPOINT = "SSRKPL0CLK";
        USERNAME = "test052";
        PASSWORD = "46391";
    }
}
abstract class NtripConnectTask extends AsyncTask<Void, Void, Void>{
    protected String TAG = "NtripActivity";
    protected  boolean isRunning = false; // 控制循环的标志
    protected String NTRIP_SERVER_IP = "103.143.19.54";
    protected int NTRIP_SERVER_PORT = 8060;
    protected Socket socket;
    protected String MOUNTPOINT = "SSRKPL0CLK";
    protected String USERNAME = "test052";
    protected String PASSWORD = "46391";

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        isRunning = false; // 结束循环
    }

    protected Void doInBackground(Void... voids) {
        isRunning = true;
        setConfig();
        Log.d(TAG, MOUNTPOINT);
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
    protected void sendConnectRequest(PrintWriter out, String mountpoint, String username, String password) {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("GET /").append(mountpoint).append(" HTTP/1.0\r\n");
        requestBuilder.append("User-Agent: NTRIP NTRIPClient/1.0\r\n");
        // 添加账号密码信息
        if (!username.isEmpty()) {
            String authHeader = "Authorization: Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
            requestBuilder.append(authHeader).append("\r\n");
        }
        requestBuilder.append("\r\n");
        out.println(requestBuilder);
    }
    protected abstract void handleReceivedData(InputStream inputStream) throws IOException;

    protected abstract void setConfig();
}

class TcpClientTask extends AsyncTask<Void, Void, Void> {
    protected String TAG = "TCPConnect";
    private String SERVER_IP = "119.96.169.117";
    private int SERVER_PORT = 7001;
    private boolean isRunning = false; // 用于控制循环的标志
    @Override
    protected Void doInBackground(Void... voids) {
        isRunning = true;
        setConfig("119.96.169.117", 7001);
        try {
            Log.i(TAG, "ycj 开始建立socket连接");
            // 创建Socket连接
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            // 获取输入输出流
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            InputStream inputStream = socket.getInputStream();
            // 向服务器发送请求
            out.println("Hello Server");
            byte[] buffer = new byte[1024];
            // 循环读取服务器发送的数据
            while (isRunning) {
                Log.i(TAG, "tcp Eph is running");
                int byteRead = inputStream.read(buffer);
                if (byteRead == -1) {
                    break;
                }
                // 处理从服务器获取的数据，例如更新UI或执行其他操作
                byte[] receivedBytes = new byte[byteRead];
                System.arraycopy(buffer, 0, receivedBytes, 0, byteRead);
                for (byte b : receivedBytes) {
                    System.out.print(String.format("%02X ", b));
                    SDK.IOInputEphData(b);
                }
                System.out.println();
            }
            // 关闭连接
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        isRunning = false; // 结束循环
    }
    public void setConfig(String SERVER_IP, int SERVER_PORT){
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }
}