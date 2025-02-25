package edu.pmdm.turbogranny;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private boolean juegoPausado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int carId=getIntent().getIntExtra("carId",R.drawable.car1); //Por defecto sera el coche1
        j = new Juego(this);
        setContentView(j);
        j.carId=carId;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Bloqueamos actividad en vertical
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (j.bucleJuego != null && j.bucleJuego.JuegoEnEjecucion) {
            juegoPausado = true;
            j.pausarJuego(); // Pausar el juego completamente
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (juegoPausado) {
            mostrarDialogoReanudar();
        }
    }

    private void mostrarDialogoReanudar() {
        new AlertDialog.Builder(this)
                .setTitle("Juego en pausa")
                .setMessage("¿Quieres reanudar la partida o salir?")
                .setPositiveButton("Reanudar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        juegoPausado = false;
                        j.reanudarJuego();
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        j.terminarPartida(); // Terminamos la partida
                        finish(); // Cerramos la actividad
                    }
                })
                .setCancelable(false) // Evitamos que el usuario cierre el diálogo sin elegir algo
                .show();
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