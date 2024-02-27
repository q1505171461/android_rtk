package com.example.fortest;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.util.Base64;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

class NtripConnectTaskObs extends NtripConnectTask{
    NtripConnectTaskObs(Map<String, String> config, Handler handler){
        super(config, handler);
    }
    protected void handleReceivedData(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            System.out.print("收到Obs数据:bad");
            return;
        }
        // 处理从服务器获取的数据，例如更新UI或执行其他操作
        byte[] receivedBytes = new byte[bytesRead];
        System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
        System.out.print("收到Obs数据:");
        for (byte b : receivedBytes) {
            System.out.print(String.format("%02X ", b));
            int ret = SDK.IOInputObsData(b);
//            System.out.printf("\n收到Obs数据ret:%d\n",ret);
            if (1 == ret) {
                sendStatusMsg();
                String gga = SDK.SDKRetrieve("NMEA_GGA",  104);
                sendMsg_GGA(gga);
                Log.d(TAG, gga);
            }
        }
        System.out.println();
    }
    void sendMsg_GGA(String gga){
        Message msg = new Message();
        msg.what = Config.MSG_GGA;
        msg.obj = gga;
        handler.sendMessage(msg);
    }

    void sendStatusMsg(){
        Message msg = new Message();
        msg.what = Config.OBS_LOGIN_SUCCESS;
        handler.sendMessage(msg);
    }
}

class NtripConnectTaskEph extends NtripConnectTask{
    NtripConnectTaskEph(Map<String, String> config, Handler handler){
        super(config, handler);
    }
    protected void handleReceivedData(InputStream inputStream) throws IOException {
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        byte[] buffer = new byte[10240];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return;
        }
        // 处理从服务器获取的数据，例如更新UI或执行其他操作
        byte[] receivedBytes = new byte[bytesRead];
        System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
        System.out.print("收到Eph数据:");
        sendStatusMsg();
        for (byte b : receivedBytes) {
            System.out.print(String.format("%02X ", b));
            SDK.IOInputEphData(b);
        }
        System.out.println();
    }

    void sendStatusMsg(){
        Message msg = new Message();
        msg.what = Config.EPH_LOGIN_SUCCESS;
        handler.sendMessage(msg);
    }
}
class NtripConnectTaskSsr extends NtripConnectTask {
    NtripConnectTaskSsr(Map<String, String> config, Handler handler){
        super(config, handler);
    }
    protected void handleReceivedData(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[10240];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return;
        }
        // 处理从服务器获取的数据，例如更新UI或执行其他操作
        byte[] receivedBytes = new byte[bytesRead];
        System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
        System.out.print("收到Ssr数据:");
        for (byte b : receivedBytes) {
            System.out.printf("%02X ", b);
            int a = SDK.IOInputSsrData(b);
            if (a == 10){
                String logstr = SDK.SDKRetrieve("SSR-ALL",  0);
                for (String line : logstr.split("\n")){
                    sendStatusMsg();
                    sendSsrMsg(line);
                    Log.i("ssrlog", line);
                }
            }
        }
        System.out.println();

    }

    void sendStatusMsg(){
        Message msg = new Message();
        msg.what = Config.SSR_LOGIN_SUCCESS;
        handler.sendMessage(msg);
    }
    void sendSsrMsg(String str){
        Message msg = new Message();
        msg.what = Config.MSG_SSR;
        msg.obj = str;
        handler.sendMessage(msg);
    }
}
abstract class NtripConnectTask extends AsyncTask<Void, Void, Void>{
    protected String TAG = "NtripTask";
    protected  boolean enableRunning = false; // 控制循环的标志
    protected String NTRIP_SERVER_IP = "103.143.19.54";
    protected int NTRIP_SERVER_PORT = 8060;
    protected Socket socket;
    protected String MOUNTPOINT = "SSRKPL0CLK";
    protected String USERNAME = "test052";
    protected String PASSWORD = "46391";
    private Map<String, String> config;

    protected Handler handler;

    NtripConnectTask(Map<String, String> config, Handler handler){
        this.config = config;
        this.handler= handler;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        enableRunning = false; // 结束循环
    }

    protected Void doInBackground(Void... voids) {
        if (!setConfig()){
            return null;
        }
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
            Log.d(TAG, "Received data from Ntrip server: " + response);
            while (enableRunning) {
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
    public void exit(){
        enableRunning = false;
    }

    protected boolean setConfig(){
        if (null == config){
            return false;
        }
        String[] configs = Config.INSTANCE.getConnectConfig();
        if ( ! Objects.requireNonNull(config.get(configs[0])).matches("(\\d+\\.){3,3}\\d+")
                || ! Objects.requireNonNull(config.get(configs[1])).matches("\\d+")
                || ! Objects.requireNonNull(config.get(configs[1])).matches("\\d+")){
            return false;
        }
        NTRIP_SERVER_IP = config.get(configs[0]);
        NTRIP_SERVER_PORT = Integer.parseInt(Objects.requireNonNull(config.get(configs[1])));
        MOUNTPOINT = config.get(configs[2]);
        USERNAME = config.get(configs[3]);
        PASSWORD = config.get(configs[4]);
        enableRunning = true;
        return true;
    }
}

class TcpClientTask extends AsyncTask<Void, Void, Void> {
    protected String TAG = "TCPConnect";
    private String SERVER_IP = "119.96.169.117";
    private int SERVER_PORT = 7001;
    private boolean enableRunning = false; // 用于控制循环的标志
    private  Map<String, String> config;
    TcpClientTask(Map<String, String> config){
       this.config = config;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        if (!setConfig()){
            return null;
        }
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
            while (enableRunning) {
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
        enableRunning = false;
    }
    public boolean setConfig(){
        if (null == config){
            return false;
        }
        String[] configStr  =  Config.INSTANCE.getConnectConfig();
        this.SERVER_IP = config.get(configStr[0]);
        this.SERVER_PORT = Integer.parseInt(Objects.requireNonNull(config.get(configStr[1])));
        enableRunning = true;
        return true;
    }
}