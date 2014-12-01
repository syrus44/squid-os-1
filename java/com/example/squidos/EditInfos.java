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
import android.widget.Button;
import android.widget.EditText;

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
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Syrus on 19/02/14.
 */
public class EditInfos extends Activity {

    final String LOGIN = "user_login";
    final String PASSWD = "user_passwd";
    String true_login = "";
    String true_passwd = "";
    String new_firstname, new_lastname, new_email, new_tel, new_password;
    String result;
    int request1_status, request2_status;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_editinfos);

        Intent in = getIntent();
        if (in != null) {
            true_login = in.getStringExtra(LOGIN);
            true_passwd = in.getStringExtra(PASSWD);
        }

        final EditText new_fname = (EditText) findViewById(R.id.new_fname);
        final EditText new_lname = (EditText) findViewById(R.id.new_lname);
        final EditText new_mail = (EditText) findViewById(R.id.new_email);
        final EditText new_passwd = (EditText) findViewById(R.id.new_passwd);
        final EditText new_phone = (EditText) findViewById(R.id.new_phone);

        Button send = (Button) findViewById(R.id.send_btn);
        Button cancel = (Button) findViewById(R.id.cancel_btn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert("Information", "Action cancelled by user");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new_passwd.getText() == null)
                    error_alert("Password Error", "Empty field ! Please enter a password");
                new_password = sha1(new_passwd.getText().toString());

                if (new_phone.getText() == null)
                    error_alert("Phone Number Error", "Empty field ! Please enter a phone number");
                new_tel = new_phone.getText().toString();

                if (new_fname.getText() == null)
                    error_alert("Firstname Error", "Empty field ! Please enter a firstname");
                new_firstname = new_fname.getText().toString();

                if (new_lname.getText() == null)
                    error_alert("Lastname Error", "Empty field ! Please enter a lastname");
                new_lastname = new_lname.getText().toString();

                if (new_mail.getText() == null)
                    error_alert("Email Error", "Empty field ! Please enter a email address");
                new_email = new_mail.getText().toString();

                try {
                    if (changeInfos(true_login, new_password, new_tel, new_lastname, new_firstname, new_email))
                        alert("Success", "Your informations have been saved");
                    else
                        error_alert("Failure", "Wrong informations");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean changeInfos(String login, String password, String telephone, String lastname, String firstname, String email_addr) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            String script = "/models/mysql_Update.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair true_login = new BasicNameValuePair("login", login);
            final BasicNameValuePair up_pass = new BasicNameValuePair("password", password);
            final BasicNameValuePair up_fname = new BasicNameValuePair("firstname", firstname);
            final BasicNameValuePair up_lname = new BasicNameValuePair("lastname", lastname);
            final BasicNameValuePair up_email = new BasicNameValuePair("email", email_addr);
            final BasicNameValuePair up_tel = new BasicNameValuePair("tel", telephone);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpPost request = new HttpPost(post);
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
                        nameValuePairs.add(true_login);
                        nameValuePairs.add(up_pass);
                        nameValuePairs.add(up_fname);
                        nameValuePairs.add(up_lname);
                        nameValuePairs.add(up_email);
                        nameValuePairs.add(up_tel);
                        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = cli.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        result = responseToString(is);
                        latch.countDown();
                    } catch (UnknownHostException e) {
                        Log.e("log_tag", "Unknown host " + e.toString());
                        value[0] = 0;
                    } catch (SocketTimeoutException e) {
                        Log.e("log_tag", "Remote host doesn't respond "+e.toString());
                        value[0] = 0;
                    } catch (ConnectTimeoutException e) {
                        Log.e("log_tag", "Connection timed out "+e.toString());
                        value[0] = 0;
                    } catch (Exception e) {
                        Log.e("log_tag", "Error in http connection "+e.toString());
                        value[0] = 0;
                    }
                }
            };
            uiThread.start();
            uiThread.join();
            Log.e("result - changeInfos Edit", result);
            String [] parts = result.split(" ");
            String part1 = parts[0];
            String part2 = parts[1];
            request1_status = extractResponse(part1, "rowsAffected");
            request2_status = extractResponse(part2, "rowsAffected");
            value[0] = 1;
            if (value[0] == 1)
                return true;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public int extractResponse(String part, String param) {
        try {
            JSONObject obj = new JSONObject(part);
            int s = obj.getInt(param);
            return s;
        } catch (JSONException e) {
            Log.e("Parsing error", "Fail with : "+e.toString());
            return 0;
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
                        EditInfos.super.finish();
                    }
                }).create().show();
    }

    public void error_alert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditInfos.super.onResume();
                    }
                }).create().show();
    }

    public String sha1(String s) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.reset();
        byte[] data = digest.digest(s.getBytes());
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
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
                                EditInfos.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}
