package com.example.salvage.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salvage.MainActivity;
import com.example.salvage.R;

public class SplashActivity extends AppCompatActivity {

    //Constant Time Delay (4000 = 4s)
    private final int SPLASHDELAY = 4000;

    //Fields
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        getWindow().setBackgroundDrawable(null);

        //Call Methods
        initializeView();
        animateLogo();
        goToLoginActivity();

    }


    private void initializeView() {
        imageView =findViewById(R.id.imageView);
    }

    private void animateLogo(){

        Animation fading = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fading.setDuration(SPLASHDELAY);

        imageView.startAnimation(fading);

    }

       // Sends user to the MainActivity after animation is complete
    private void goToLoginActivity() {
        new Handler().postDelayed(()-> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASHDELAY);



    }
}