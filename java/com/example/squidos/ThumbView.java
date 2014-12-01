package com.example.squidos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Syrus on 21/12/13.
 */
public class ThumbView extends Activity {
    private ListView list_view;
    String result, logo, name, true_login;
    final String LOGIN = "LOGIN";
    int nbApps = 0;
    ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    int[] igs = new int[]{
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3,
            R.drawable.img4,
            R.drawable.img5,
            R.drawable.img6,
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_thumb);

        Intent in = getIntent();
        if (in != null)
            true_login = in.getStringExtra(LOGIN);

        try {
            this.readDBApps(true_login);
            this.readDB(true_login);
        } catch (Exception e) {
            e.printStackTrace();
        }

        list_view = (ListView) findViewById(R.id.list_view);

        SimpleAdapter sa = new SimpleAdapter(this.getBaseContext(), listItem, R.layout.affitems,
              new String[] {"image", "title", "description"},
              new int[] {R.id.img, R.id.title, R.id.description});

        list_view.setAdapter(sa);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                 HashMap<String, String> map = (HashMap<String, String>) list_view.getItemAtPosition(i);
                 new AlertDialog.Builder(ThumbView.this)
                        .setTitle("Result")
                        .setMessage("Your choice is : "+map.get("title"))
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                              ThumbView.super.onResume();
                         }
                      }).create().show();
                    } catch (Exception e) {
                        Log.e("error", "invalid choice");
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
                                ThumbView.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean readDBApps(String login) throws Exception {
        try {
            final CountDownLatch countdown = new CountDownLatch(1);
            String script = "/models/mysql_Count.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIThread") {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
                        HttpPost request = new HttpPost(post);
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(true_login);

                        request.setHeader("Content-Type", "application/x-www-urlencoded");
                        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = client.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        countdown.countDown();
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
                        Log.e("log_tag", "Error in http connection"+e.toString());
                        value[0] = 0;
                    }
                }
            };
            uiThread.start();
            uiThread.join();
            Log.e("result - readDBApps", result);
            nbApps = extractNumber(result, "nbApps");
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
            final CountDownLatch countdown = new CountDownLatch(1);
            String script = "/models/mysql_Apps.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
                        HttpPost request = new HttpPost(post);
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(true_login);

                        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = client.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);

                        countdown.countDown();
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
                        Log.e("log_tag", "Error in http connection"+ e.toString());
                        value[0] = 0;
                    }
                }
            };
            uiThread.start();
            uiThread.join();
            Log.e("res", result);
            extractFields(result, "name");
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public static String responseToString(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return (sb.toString());
        } catch (Exception e) {
            Log.e("log_tag", "Error converting result"+e.toString());
            return null;
        }
    }

    public void extractFields(String result, String param1) {
        try {
            JSONArray obj = new JSONArray(result);
            JSONObject current;
            for (int i = 0 ; i < obj.length() ; i++) {
                current = obj.getJSONObject(i);
                name = current.getString(param1);
                String name_version = name + " (final release)";
                map = new HashMap<String, String>();
                map.put("title", name);
                map.put("description", name_version);
                map.put("image", Integer.toString(igs[i]));
                if (i == nbApps)
                    i = 0;
                listItem.add(map);
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error while parsing response"+e.toString());
        }
    }

    public int extractNumber(String result, String param) {
        try {
            JSONObject obj = new JSONObject(result);
            int nb = obj.getInt(param);
            return nb;
        } catch (JSONException e) {
            Log.e("log_tag", "Error while parsing response"+e.toString());
            return 0;
        }
    }
}
