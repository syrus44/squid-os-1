package com.example.squidos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Syrus on 21/12/13.
 */
public class VMStatus extends Activity {

    ToggleButton on_off;
    final String LOGIN = "user_login";
    String true_login, av_size, u_size, vm, result;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_vmstat);

        Intent in = getIntent();
        if (in != null)
            true_login = in.getStringExtra(LOGIN);

        try {
            this.readDB(true_login);
            this.retrieveVMName(true_login);
            setInLayout(vm, av_size, u_size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final TextView vm_status = (TextView) findViewById(R.id.vm_status);
        try {
            this.getCurrentRole(vm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkAllStatus(result, vm_status);

        on_off = (ToggleButton) findViewById(R.id.on_off_btn);
        on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    try {
                        startVirtualMachine(vm);
                        if ((result.trim().equals("ReadyRole"))) {
                            vm_status.setText("VM Started");
                            vm_status.setTextColor(Color.parseColor("#006600"));
                        } else {
                            vm_status.setText("Unknown Role");
                            vm_status.setTextColor(Color.parseColor("#000000"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        alert("Error", "You don't have permissions to start VMs");
                        vm_status.setText("Error !");
                        vm_status.setTextColor(Color.parseColor("#000000"));
                    }
                } else {
                    try {
                        stopVirtualMachine(vm);
                        if ((result.trim().equals("StoppingRole"))) {
                            vm_status.setText("VM Stopped");
                            vm_status.setTextColor(Color.parseColor("#CC0000"));
                        } else {
                            vm_status.setText("Unknown Role");
                            vm_status.setTextColor(Color.parseColor("#000000"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        alert("Error", "You don't have permissions to stop VMs");
                        vm_status.setText("Error !");
                        vm_status.setTextColor(Color.parseColor("#000000"));
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setTitle("About SquidOS")
                        .setMessage("Version 1.0\nCreated by SquidOS Dev Team !")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                VMStatus.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkAllStatus(String status, TextView vm_status) {
        Log.e("status", status);
        if ((status.trim().equals("ReadyRole"))) {
            vm_status.setText("VM Started");
            vm_status.setTextColor(Color.parseColor("#006600"));
            on_off = (ToggleButton) findViewById(R.id.on_off_btn);
            on_off.setSelected(true);
        } else if ((status.trim().equals("StoppedVM"))) {
            vm_status.setText("VM Stopped");
            vm_status.setTextColor(Color.parseColor("#CC0000"));
            on_off = (ToggleButton) findViewById(R.id.on_off_btn);
            on_off.setSelected(false);
        } else {
            vm_status.setText("Unknown Role");
            vm_status.setTextColor(Color.parseColor("#000000"));
            on_off = (ToggleButton) findViewById(R.id.on_off_btn);
            on_off.setSelected(false);
        }
    }

    public boolean retrieveVMName(String login) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            String script = "/models/mysql_OS.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIHandler")
            {
                @Override
                public synchronized void run()
                {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpPost request = new HttpPost(post);
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(true_login);
                        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = cli.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host "+e.toString());
                        value[0] = 0;
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
            Log.e("result - retrieveVMName", result);
            vm = extractFields(result, "vm");
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public boolean getCurrentRole(String vm_name) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final String url = String.format("http://python-squidos.rhcloud.com/status/?vm=%s", vm_name);
            final int[] value = new int[1];

            Thread uiThread = new HandlerThread("UiHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = cli.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host "+e.toString());
                        value[0] = 0;
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
            Log.e("result - getCurrentRole", result);
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public boolean startVirtualMachine(String vm_name) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final String url = String.format("http://python-squidos.rhcloud.com/power/?hash=64d27f02d8695d5e3206da587fdb5bfeea3af3a3&vm=%s&action=start", vm_name);
            final int[] value = new int[1];

            Thread uiThread = new HandlerThread("UiHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = cli.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host "+e.toString());
                        value[0] = 0;
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
            Log.e("result - startVM", result);
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public boolean stopVirtualMachine(String vm_name) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final String url = String.format("http://python-squidos.rhcloud.com/power/?hash=64d27f02d8695d5e3206da587fdb5bfeea3af3a3&vm=%s&action=stop", vm_name);
            final int[] value = new int[1];

            Thread uiThread = new HandlerThread("UiHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpGet request = new HttpGet(url);
                        HttpResponse response = cli.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host "+e.toString());
                        value[0] = 0;
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
            Log.e("result - stopVM", result);
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public boolean readDB(String login) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            String script = "/models/mysql_VMStat.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final int [] value = new int[1];

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
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(true_login);
                        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = co.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host "+e.toString());
                        value[0] = 0;
                    } catch (ConnectTimeoutException e) {
                        Log.e("log_tag", "Connection timed out "+e.toString());
                        value[0] = 0;
                    } catch (SocketTimeoutException e) {
                        Log.e("log_tag", "Remote host doesn't respond "+e.toString());
                        value[0] = 0;
                    } catch(Exception e) {
                        Log.e("log_tag", "Error in http connection "+e.toString());
                        value[0] = 0;
                    }
                }
            };
            uiThread.start();
            uiThread.join();
            Log.e("result - readDB VM", result);
            av_size = extractFields(result, "available_size");
            u_size = extractFields(result, "used_size");
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public String extractFields(String res, String param) {
        try {
            JSONObject obj = new JSONObject(res);
            String tmp = obj.getString(param);
            return tmp;
        } catch (JSONException e) {
            Log.e("Parsing error", "Failed with : "+e.toString());
            return null;
        }
    }

    public static String responseToString(InputStream is) {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return (sb.toString());
        } catch(Exception e) {
            Log.e("log_tag", "Error converting result "+e.toString());
            return null;
        }
    }

    public void setInLayout(String k_ver, String av_size, String u_size) {
        TextView os_name = (TextView) findViewById(R.id.os_name);
        TextView size_name = (TextView) findViewById(R.id.size_name);
        TextView size2_name = (TextView) findViewById(R.id.size2_name);

        av_size += " GB";
        u_size += " GB";

        os_name.setText(k_ver);
        size2_name.setText(u_size);
        size_name.setText(av_size);
    }

    public void alert(String title, String msg)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        VMStatus.super.onResume();
                    }
                }).create().show();
    }
}