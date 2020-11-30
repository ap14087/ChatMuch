package com.amanpandey.chatmuch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.view.View;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amanpandey.chatmuch.common.Util;



public class Message extends AppCompatActivity {

    private TextView tvMessage;
    private ProgressBar pbMessage;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        tvMessage = findViewById(R.id.tvMessage);
        pbMessage = findViewById(R.id.pvMessage);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    tvMessage.setText("No Internet");
                }
            };

            NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(request, networkCallback);

        }

    }

    public void btnRetryClick(View view){
        pbMessage.setVisibility(android.view.View.VISIBLE);
        if(Util.connectionAvailable(this)){
            finish();
        }
        else
        {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                      pbMessage.setVisibility(android.view.View.GONE);
                }
            },1000);
        }

    }

    public  void btnCloseClick(View view){
        finishAffinity();
    }
}