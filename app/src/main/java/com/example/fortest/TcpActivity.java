package com.example.fortest;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpActivity extends AppCompatActivity {

    private static final String TAG = "TcpActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "ycj on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 执行异步任务以连接并获取数据
                Log.i(TAG, "TCP接收数据开始执行");
                new TcpClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.i(TAG, "Ssr接收数据开始执行");
                new NtripActivity.NtripConnectTaskSsr().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.i(TAG, "Obs接收数据开始执行");
                new NtripActivity.NtripConnectTaskObs().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    public static class TcpClientTask extends AsyncTask<Void, Void, Void> {
        private static final String SERVER_IP = "119.96.165.202";
        private static final int SERVER_PORT = 8605;
        private boolean isRunning = false; // 用于控制循环的标志
        @Override
        protected Void doInBackground(Void... voids) {
            isRunning = true;
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
    }
}
