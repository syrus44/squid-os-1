package com.example.squidos;

import android.os.HandlerThread;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Louis on 18/12/13.
 */
public class DatabaseConnection
{
    private String _domain;

    public DatabaseConnection(String dom)
    {
        this._domain = dom;
    }

    public boolean connect(String script, final BasicNameValuePair log, final BasicNameValuePair pass) throws InterruptedException {
        final String post = this._domain + script;
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] value = new int[1];

        Thread uiThread = new HandlerThread("UIHandler")
    {
        @Override
        public synchronized void run()
        {
            try
            {
                HttpClient co = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(co.getParams(), 15000);
                HttpPost request = new HttpPost(post);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(log);
                nameValuePairs.add(pass);
                request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = co.execute(request);

                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                String res = responseToString(is);
                Log.e("result - connect", res);
                if (isConnected(res))
                    value[0] = 1;
                else value[0] = 0;
                latch.countDown();
            } catch (ConnectTimeoutException e) {
                Log.e("log_tag", "Connection timed out "+e.toString());
                value[0] = 0;
            } catch (SocketTimeoutException e) {
                Log.e("log_tag", "Remote host doesn't respond "+e.toString());
                value[0] = 0;
            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection "+e.toString());
                value[0] = 0;
            }

        }
    };
    uiThread.start();
    uiThread.join();
    if (value[0] == 1)
        return (true);
    return (false);
    }

    public static String responseToString(InputStream is)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return (sb.toString());
        }
        catch(Exception e)
        {
            Log.e("log_tag", "Error converting result "+e.toString());
            return null;
        }
    }

    public static boolean isConnected(String result)
    {
        try
        {
            JSONObject object = new JSONObject(result);
            boolean json_data = object.getBoolean("authent");
            return (json_data);
        }
        catch(JSONException e){
            Log.e("log_tag", "Error parsing response "+e.toString());
            return false;
        }
    }
}