package edu.pmdm.turbogranny;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActividadJuego extends AppCompatActivity {

    Juego j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int carId=getIntent().getIntExtra("carId",R.drawable.car1); //Por defecto sera el coche1
        j = new Juego(this);
        setContentView(j);
        j.carId=carId;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Bloqueamos actividad en vertical
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            j.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE //diseño estable sin cambio al ocultar la barras
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //permite que la aplicacion de dibuje detras de la barra de navegacion
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // que se dibuje detras de la barra de estado
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar (botones virtuales)
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar (barra de estado hora y bateria..)
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //matiene el modo inmersivo incluso si el usuario toca la pantalla
            //cuando se presiona volumen, por ej, se cambia la visibilidad, hay que volver
            //a ocultar
            j.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    hideSystemUI();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        j.terminarPartida();
    }
}