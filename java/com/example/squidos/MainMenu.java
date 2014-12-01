package com.example.squidos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MainMenu extends Activity {

    Button vm_btn, direct_btn, thumb_btn, account_btn, disconnect_btn;
    final String LOGIN = "user_login";
    final String PASSWD = "user_passwd";
    String true_login, true_passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mainmenu);

        Intent in = getIntent();
        TextView uname = (TextView) findViewById(R.id.user_field);

        if (in != null) {
            true_login = in.getStringExtra(LOGIN);
            true_passwd = in.getStringExtra(PASSWD);
            uname.setText(in.getStringExtra(LOGIN));
        }

        vm_btn = (Button) findViewById(R.id.vm_status);
        vm_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                  Intent in1 = new Intent(getApplicationContext(), VMStatus.class);
                    in1.putExtra(LOGIN, true_login);
                    startActivity(in1);
                    }
                });

        direct_btn = (Button) findViewById(R.id.direct_view);
        direct_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in2 = new Intent(getApplicationContext(), DirectView.class);
                    startActivity(in2);
                    }
                });

        thumb_btn = (Button) findViewById(R.id.thumb_view);
        thumb_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in3 = new Intent(getApplicationContext(), ThumbView.class);
                    in3.putExtra(LOGIN, true_login);
                    startActivity(in3);
                    }
                });

        account_btn = (Button) findViewById(R.id.account);
        account_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in4 = new Intent(getApplicationContext(), AccountView.class);
                    in4.putExtra(LOGIN, true_login);
                    in4.putExtra(PASSWD, true_passwd);
                    startActivity(in4);
                }
                });

        disconnect_btn = (Button) findViewById(R.id.disconnect_btn);
        disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainMenu.this)
                        .setTitle("Disconnect Account")
                        .setMessage("Do you want to disconnect from your account ?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainMenu.super.onBackPressed();
                            }
                        }).create().show();
            }
        });
  }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setTitle("Disconnect Account")
                .setMessage("Do you want to disconnect from your account ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainMenu.super.onBackPressed();
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
                                MainMenu.super.onResume();
                            }
                        }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
