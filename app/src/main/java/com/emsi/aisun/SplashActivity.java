package com.emsi.aisun;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView sunIcon = findViewById(R.id.sun_icon);
        TextView appName = findViewById(R.id.app_name);
        TextView slogan = findViewById(R.id.slogan);

        // Rotation animation for sun icon
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_continuously);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        sunIcon.startAnimation(rotateAnimation);

        // Fade in animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        appName.startAnimation(fadeIn);
        slogan.startAnimation(fadeIn);

        // Handler to move to next activity after 3 seconds
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 3000);
    }
}