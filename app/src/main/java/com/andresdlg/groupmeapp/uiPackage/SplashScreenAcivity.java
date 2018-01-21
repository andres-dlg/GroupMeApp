package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.andresdlg.groupmeapp.R;

public class SplashScreenAcivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bitmap bmp;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        // BACKGROUND AUTO ADAPTABLE
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if(SplashScreenAcivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            // PORTRAIT MODE
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background_vertical),size.x,size.y,true);
        } else {
            // LANDSCAPE MODE
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background),size.x,size.y,true);
        }
        ImageView iv_background = findViewById(R.id.ivLogin);
        iv_background.setImageBitmap(bmp);

        int SPLASH_TIME_OUT = 500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashScreenAcivity.this,MainActivity.class);
                startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
