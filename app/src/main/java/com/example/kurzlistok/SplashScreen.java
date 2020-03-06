package com.example.kurzlistok;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


import androidx.appcompat.app.AppCompatActivity;
//42 touto aktivitou celá aplikácia začína (je to nastavené v android manifeste)
//42  pričom po 1 sekunde sa intentom spustí MainActivity
//42 splashscreen sa zobrazuje kvôli tomu že sa vykreslí nie bežné okno ale v res/background_splash.xml
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
