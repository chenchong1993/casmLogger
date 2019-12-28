package com.kubolab.gnss.casmLogger;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    static int size = 0;

    public static void sendToCloud(final String path, final String content,
                                   final HttpCallbackListener listener) {
        size++;
        Log.e("size",String.valueOf(size));
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(path+content);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);  //设置连接超时时间5s
                    connection.setReadTimeout(5000);//设置读取的超时时间
                    connection.setRequestMethod("POST"); //设置以Post方式提交数据
                    connection.setUseCaches(false);  //使用post方式不能使用缓存
                    connection.setDoOutput(true);//打开输出流，以便从服务器获取数据
                    connection.setDoInput(true);//接收数据，默认为true
                    connection.connect();
                    //字节类型数据流
                   // DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    //out.writeBytes(content);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == connection.HTTP_OK){//=200
                        reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(),"utf-8"));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null){
                            response.append(line);
                        }
                        if (listener != null){
                            listener.onFinish(/*String.valueOf(responseCode)+*/response.toString());
                        }
                        size--;
                    }
                } catch (IOException e) {
                    if (listener != null){
                        listener.onError(e);
                    }
                    e.printStackTrace();
                    size--;
                }finally {
                    if (reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
