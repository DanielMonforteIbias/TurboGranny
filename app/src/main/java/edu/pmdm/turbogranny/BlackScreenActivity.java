package edu.pmdm.turbogranny;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BlackScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View blackView = new View(this);
        blackView.setBackgroundColor(Color.BLACK);
        setContentView(blackView);
        //Retrasar el cambio a la actividad del juego
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(BlackScreenActivity.this, ActividadJuego.class);
            intent.putExtras(getIntent().getExtras()); //Pasamos los extras recibidos
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1000);
    }
}