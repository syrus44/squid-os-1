package com.example.squidos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.http.message.BasicNameValuePair;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Home extends Activity {

    static final int OK_RES = 1;
    boolean Internet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Internet = checkInternetConnection();
        if (!Internet) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning : Internet isn't reachable")
                    .setMessage("The application needs to be connected to the Internet.\nDo you want to check it now ?")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            quitApp();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), OK_RES);
                            if (OK_RES == RESULT_OK)
                                Home.super.onResume();
                            else
                                checkInternetConnection();
                        }
                    }).create().show();
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null && ni.isConnectedOrConnecting())
            return true;
        return false;
    }

    public void quitApp() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("SquidOS will now quit ...")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Home.super.finish();
                    }
                }).create().show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you really quit the SquidOS ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Home.super.onBackPressed();
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
                                Home.super.onResume();
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

    public void launchTwitter(View v)
    {
        Uri uri = Uri.parse("https://twitter.com/Squid_os");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void launchFacebook(View v) {
        Uri uri = Uri.parse("https://www.facebook.com/squiidos");
        Intent in = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(in);
    }

    public void dataBaseConnect(View v) throws InterruptedException {
        EditText log = (EditText) this.findViewById(R.id.squidlog);
        EditText pass = (EditText) this.findViewById(R.id.squidpass);
        final String LOGIN = "user_login";
        final String PASSWD = "user_passwd";

        if (log.getText().toString().isEmpty() == true || pass.getText().toString().isEmpty() == true)
            alert("error", "empty login or password.");
        else
        {
            BasicNameValuePair logpair = new BasicNameValuePair("login", log.getText().toString());
            BasicNameValuePair passpair = new BasicNameValuePair("password", pass.getText().toString());

            String script = "/models/android.php";
            DatabaseConnection db = new DatabaseConnection("http://php-squidos1.rhcloud.com");
            if (!(db.connect(script, logpair, passpair))) {
                Log.e("error", "Connection problem OR invalid login/password");
                alert("Error", "Invalid login or Password\nConnection timed out");
            }
            else
            {
                Intent in = new Intent(Home.this, MainMenu.class);
                in.putExtra(LOGIN, log.getText().toString());
                in.putExtra(PASSWD, pass.getText().toString());
                startActivity(in);
            }
        }
    }

    public void createAccount(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("You will create your account ! Are you sure ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent in = new Intent(Home.this, CreateAccount.class);
                        startActivity(in);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Home.super.onResume();
                    }
                }).create().show();
    }

    public void alert(String title, String msg)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Home.super.onResume();
                    }
                }).create().show();
    }
}
