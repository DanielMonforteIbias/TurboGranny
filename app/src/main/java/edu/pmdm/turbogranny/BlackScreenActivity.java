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
    private Handler handler;
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View blackView = new View(this);
        blackView.setBackgroundColor(Color.BLACK);
        setContentView(blackView);
        //Retrasar el cambio a la actividad del juego
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BlackScreenActivity.this, ActividadJuego.class);
                intent.putExtras(getIntent().getExtras()); //Pasamos los extras recibidos
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };
        handler.postDelayed(runnable,1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable); //Evitamos que se lance la actividad
        finish();
    }
}