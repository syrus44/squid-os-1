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
public class CreateAccount extends Activity {

    String new_firstname, new_lastname, new_email, new_tel, new_language, new_login, new_password;
    String result;
    int request1_status, request2_status, request3_status, request4_status;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_createaccount);

        final EditText ca_fname = (EditText) findViewById(R.id.ca_fname);
        final EditText ca_lname = (EditText) findViewById(R.id.ca_lname);
        final EditText ca_mail = (EditText) findViewById(R.id.ca_email);
        final EditText ca_phone = (EditText) findViewById(R.id.ca_phone);
        final EditText ca_lang = (EditText) findViewById(R.id.ca_lang);
        final EditText ca_login = (EditText) findViewById(R.id.ca_login);
        final EditText ca_passwd = (EditText) findViewById(R.id.ca_passwd);

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
                if (ca_login.getText() == null)
                    error_alert("Login Error", "Empty field ! Please enter a login");
                new_login = ca_login.getText().toString();

                if (ca_passwd.getText() == null)
                    error_alert("Password Error", "Empty field ! Please enter a password");
                new_password = sha1(ca_passwd.getText().toString());

                if (ca_phone.getText() == null)
                    error_alert("Phone Number Error", "Empty field ! Please enter a phone number");
                new_tel = ca_phone.getText().toString();

                if (ca_fname.getText() == null)
                    error_alert("Firstname Error", "Empty field ! Please enter a firstname");
                new_firstname = ca_fname.getText().toString();

                if (ca_lname.getText() == null)
                    error_alert("Lastname Error", "Empty field ! Please enter a lastname");
                new_lastname = ca_lname.getText().toString();

                if (ca_mail.getText() == null)
                    error_alert("Email Error", "Empty field ! Please enter a email address");
                new_email = ca_mail.getText().toString();

                if (ca_lang.getText() == null)
                    error_alert("Language Error", "Empty field ! Please enter a language");
                new_language = ca_lang.getText().toString();

                try {
                    if (changeInfos(new_login, new_password, new_tel, new_lastname, new_firstname, new_email, new_language))
                        alert("Success", String.format("Your account is now created\nYour login is : %s", new_login));
                    else
                        error_alert("Failure", "Wrong informations");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean changeInfos(String login, String password, String telephone, String lastname, String firstname, String email_addr, String lang) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            String script = "/models/mysql_Insert.php";
            final String post = "http://php-squidos1.rhcloud.com" + script;
            final BasicNameValuePair up_login = new BasicNameValuePair("login", login);
            final BasicNameValuePair up_pass = new BasicNameValuePair("password", password);
            final BasicNameValuePair up_fname = new BasicNameValuePair("firstname", firstname);
            final BasicNameValuePair up_lname = new BasicNameValuePair("lastname", lastname);
            final BasicNameValuePair up_email = new BasicNameValuePair("email", email_addr);
            final BasicNameValuePair up_tel = new BasicNameValuePair("tel", telephone);
            final BasicNameValuePair up_lang = new BasicNameValuePair("language", lang);
            final int [] value = new int[1];

            Thread uiThread = new HandlerThread("UIHandler")
            {
                @Override
                public synchronized void run() {
                    try {
                        HttpClient cli = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(cli.getParams(), 15000);
                        HttpPost request = new HttpPost(post);
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
                        nameValuePairs.add(up_login);
                        nameValuePairs.add(up_pass);
                        nameValuePairs.add(up_fname);
                        nameValuePairs.add(up_lname);
                        nameValuePairs.add(up_email);
                        nameValuePairs.add(up_tel);
                        nameValuePairs.add(up_lang);
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
            Log.e("result - changeInfos", result);
            String [] parts = result.split(" ");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];
            String part4 = parts[3];
            request1_status = extractResponse(part1, "rowsAffected");
            request2_status = extractResponse(part2, "rowsAffected");
            request3_status = extractResponse(part3, "rowsAffected");
            request4_status = extractResponse(part4, "rowsAffected");
            Log.e("stat1", ""+request1_status);
            Log.e("stat2", ""+request2_status);
            Log.e("stat3", ""+request3_status);
            Log.e("stat4", ""+request4_status);
            if (request1_status > 0 && request2_status > 0 && request3_status > 0 && request4_status > 0)
                value[0] = 1;
            else
                value[0] = 0;

            if (value[0] == 1)
                return true;
            else
                return false;
        } catch (Exception e) {
            throw e;
        }
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
                        CreateAccount.super.finish();
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
                        CreateAccount.super.onResume();
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
                                CreateAccount.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}
