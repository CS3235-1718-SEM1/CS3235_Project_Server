package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLoginClick(View view) {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
    }

    public void onScanClick(View view) {
        new IntentIntegrator(this).initiateScan();
    }
}
