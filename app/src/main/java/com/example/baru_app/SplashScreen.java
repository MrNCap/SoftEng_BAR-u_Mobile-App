package com.example.baru_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.baru_app.AUTHENTICATION.Login;

public class SplashScreen extends AppCompatActivity {
    ImageView logo_sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        logo_sp = findViewById(R.id.logo_sp);
        logo_sp.setAlpha(0f);
        logo_sp.animate().scaleX(.4f).scaleY(.4f).setDuration(1500).alpha(1f).withEndAction(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, Login.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }).start();
    }
}