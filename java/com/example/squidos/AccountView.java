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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Syrus on 21/12/13.
 */
public class AccountView extends Activity {

    final String LOGIN = "user_login";
    final String PASSWD = "user_passwd";
    String true_login = "";
    String true_passwd = "";
    String f_name, l_name, mail, register_d;
    String result;
    int request1_status, request2_status, request3_status;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_accountview);

        Intent in = getIntent();
        if (in != null) {
            true_login = in.getStringExtra(LOGIN);
            true_passwd = in.getStringExtra(PASSWD);
        }

        try {
            this.readDB(true_login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editAccount(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("You will change your personnal informations ! Are you sure ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent in = new Intent(AccountView.this, EditInfos.class);
                        in.putExtra(LOGIN, true_login);
                        in.putExtra(PASSWD, true_passwd);
                        startActivity(in);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bis_alert("Information", "Action cancelled by user");
                    }
                }).create().show();
    }

    public void deleteAccount(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("You will delete your account ! Are you sure ?\nThis operation is definitive !")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (dropAccount(true_login))
                                alert("Success", "Logout ...");
                            else
                                bis_alert("Failure", "Error in deleting your account !");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bis_alert("Information", "Action cancelled by user");
                    }
                }).create().show();
    }

    public boolean dropAccount(String login) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            String script = "/models/mysql_Delete.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIHandler")
            {
                @Override
                public synchronized void run() {
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
            Log.e("result - dropAccount", result);
            String [] parts = result.split(" ");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];
            request1_status = extractResponse(part1, "rowsAffected");
            request2_status = extractResponse(part2, "rowsAffected");
            request3_status = extractResponse(part3, "rowsAffected");
            if (request1_status > 0 && request2_status > 0 && request3_status > 0)
                value[0] = 1;
            else
                value[0] = 0;

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
            String script = "/models/mysql_Account.php";
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
            Log.e("result - readDB", result);
            f_name = extractFields(result, "firstname");
            l_name = extractFields(result, "lastname");
            mail = extractFields(result, "email");
            register_d = extractFields(result, "date_inscription");
            setInLayout(f_name, l_name, mail, register_d);
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
            Log.e("Parsing error", "Fail with : "+e.toString());
            return null;
        }
    }

    public int extractResponse(String res, String param) {
        try {
            JSONObject obj = new JSONObject(res);
            int s = obj.getInt(param);
            return s;
        } catch (JSONException e) {
            Log.e("Parsing error", "Fail with : "+e.toString());
            return 0;
        }
    }

    public void setInLayout(String f_name, String l_name, String mail, String register_d) {
        TextView firstname = (TextView) findViewById(R.id.firstname_field);
        TextView lastname = (TextView) findViewById(R.id.lastname_field);
        TextView email = (TextView) findViewById(R.id.email_field);
        TextView date = (TextView) findViewById(R.id.date_field);

        try {
            firstname.setText(f_name);
            lastname.setText(l_name);
            email.setText(mail);
            date.setText(register_d);
        } catch (Exception e) {
            Log.e("log_tag", "Date format invalid "+e.toString());
        }
    }

    public static String responseToString(InputStream is) {
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
        } catch(Exception e) {
            Log.e("log_tag", "Error converting result "+e.toString());
            return null;
        }
    }

    public void alert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent in = new Intent(getApplicationContext(), Home.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        in.putExtra("EXIT", true);
                        startActivity(in);
                    }
                }).create().show();
    }

    public void bis_alert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AccountView.super.onResume();
                    }
                }).create().show();
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
                                AccountView.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}