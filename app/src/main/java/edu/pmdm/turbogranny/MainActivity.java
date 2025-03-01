package edu.pmdm.turbogranny;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import edu.pmdm.turbogranny.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int carIndex = 0;
    private int[] cars = {R.drawable.car1, R.drawable.car2, R.drawable.car3, R.drawable.car4, R.drawable.car5,R.drawable.car6,R.drawable.car7}; //Necesario para mostrar y animar solo la imagen de los coches quietos
    private int[] carsSpriteSheets = {R.drawable.car1spritesheet, R.drawable.car2spritesheet, R.drawable.car3spritesheet, R.drawable.car4spritesheet, R.drawable.car5spritesheet,R.drawable.car6spritesheet,R.drawable.car7spritesheet}; //Necesario para pasar el spritesheet correcto
    private int[] carLocation = new int[2];
    private int carX, screenWidth;
    private boolean changingCar = false, activeGame = false; //para bloquear otro cambio mientras se cambia
    private String nickname = "USER";
    private ArrayList<Puntuacion> maximasPuntuaciones = new ArrayList<>();
    private SharedPreferences preferencias;
    private SharedPreferences.Editor editor;

    private SoundPool soundPoolMain;
    int changeCarSound, buySound, okaySound, coinSound, carStartSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencias = getSharedPreferences("DatosJuego", MODE_PRIVATE);
        editor = preferencias.edit();

        cargarSoundPoolMain();
        int totalMonedas = preferencias.getInt("monedas", 0); // Obtener y mostrar el total de monedas guardado en SharedPreferences
        nickname = preferencias.getString("nickname", "USER"); //Obtener el nickname de preferencias

        cargarPuntuaciones();

        binding.txtNickname.setText(nickname);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
        binding.imgCar.setImageBitmap(BitmapFactory.decodeResource(getResources(), cars[carIndex]));

        binding.startButton.setOnClickListener(v -> {
            if (!activeGame) {
                Intent intent = new Intent(MainActivity.this, BlackScreenActivity.class);
                intent.putExtra("carId", carsSpriteSheets[carIndex]);
                activeGame = true;
                playSound(carStartSound);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        binding.imgBtnLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!changingCar) {
                    carIndex = (carIndex - 1 + cars.length) % cars.length; //Nos movemos 1 hacia atras, asegurando que vaya al ultimo si estamos en el primero
                    changeCar(-1);
                    playSound(changeCarSound);
                }
            }
        });
        binding.imgBtnRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!changingCar) {
                    carIndex = (carIndex + 1) % cars.length; //Nos movemos 1 hacia delante, asegurando que vaya al primero si estamos en el ultimo
                    changeCar(1);
                    playSound(changeCarSound);
                }

            }
        });
        binding.imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoNickname();
            }
        });
        binding.txtNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoNickname();
            }
        });
        binding.imgBtnLeaderboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_leaderboards, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(new PuntuacionAdapter(maximasPuntuaciones));
                dialog.show();
            }
        });
        binding.imgCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(coinSound);
            }
        });
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        binding.imgCar.getLocationOnScreen(carLocation);
        carX = carLocation[0];
        animarBotones();
        animarMoneda();
    }

    private void dialogoNickname() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nickname, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        EditText input = dialogView.findViewById(R.id.nicknameEditText);
        input.setText(nickname);
        Button btnAccept = dialogView.findViewById(R.id.btnAceptar);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNickname = input.getText().toString().trim();
                if (!newNickname.isEmpty()) {
                    nickname = newNickname;
                    editor.putString("nickname", newNickname);
                    editor.apply();
                    binding.txtNickname.setText(nickname);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "El nickname no puede estar vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void cargarPuntuaciones() {
        String jsonMaximasPuntuaciones = preferencias.getString("maximasPuntuaciones", "[]");
        Type listType = new TypeToken<ArrayList<Puntuacion>>() {}.getType();
        maximasPuntuaciones = new Gson().fromJson(jsonMaximasPuntuaciones, listType);
        if (maximasPuntuaciones == null) {
            maximasPuntuaciones = new ArrayList<>();
        }
    }

    private void cargarSoundPoolMain() {
        soundPoolMain = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        changeCarSound = soundPoolMain.load(this, R.raw.change, 1);
        buySound = soundPoolMain.load(this, R.raw.buy, 1);
        okaySound = soundPoolMain.load(this, R.raw.okay, 1);
        coinSound = soundPoolMain.load(this, R.raw.coin, 1);
        carStartSound=soundPoolMain.load(this,R.raw.carstart,1);
        binding.imgBtnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Comming soon!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        screenWidth=getResources().getDisplayMetrics().widthPixels;
        binding.imgCar.getLocationOnScreen(carLocation);
        carX = carLocation[0];
        animarBotones();
        animarMoneda();
    }

    private void changeCar(int direccion) { //Direccion es -1 a la izquierda, 1 a la derecha
        changingCar = true;
        AnimatorSet carAnimator = new AnimatorSet();
        float salidaX = direccion * screenWidth;
        float entradaX = -direccion * screenWidth;
        ObjectAnimator trasladarSalida = ObjectAnimator.ofFloat(binding.imgCar, "translationX", carX, salidaX);
        trasladarSalida.setDuration(400);
        trasladarSalida.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.imgCar.setImageBitmap(BitmapFactory.decodeResource(getResources(), cars[carIndex]));
                binding.imgCar.setX(entradaX);
                ObjectAnimator trasladarEntrada = ObjectAnimator.ofFloat(binding.imgCar, "translationX", entradaX, carX);
                trasladarEntrada.setDuration(400);
                trasladarEntrada.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changingCar = false;
                    }
                });
                trasladarEntrada.start();
            }
        });
        carAnimator.play(trasladarSalida);
        carAnimator.start();
    }

    private void animarBotones() {
        AnimatorSet btnAnimator = new AnimatorSet();
        ObjectAnimator colorBtn = ObjectAnimator.ofArgb(binding.startButton, "backgroundColor", ContextCompat.getColor(this, R.color.car1red), ContextCompat.getColor(this, R.color.car1blue));
        colorBtn.setDuration(1500);
        colorBtn.setRepeatCount(ObjectAnimator.INFINITE);
        colorBtn.setRepeatMode(ObjectAnimator.REVERSE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.startButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.startButton, "scaleY", 1f, 1.05f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        btnAnimator.play(colorBtn).with(scaleX).with(scaleY);
        btnAnimator.start();

        /*AnimatorSet settingsAnimator=new AnimatorSet();
        ObjectAnimator rotationSettings=ObjectAnimator.ofFloat(binding.imgBtnSettings,"rotation",0,360);
        rotationSettings.setDuration(15000);
        rotationSettings.setRepeatCount(ObjectAnimator.INFINITE);
        rotationSettings.setInterpolator(new LinearInterpolator());
        settingsAnimator.play(rotationSettings);
        settingsAnimator.start();*/
    }

    private void animarMoneda(){
        AnimationDrawable coinAnimation;
        ImageView coinImage = findViewById(R.id.imgCoin);
        coinImage.setBackgroundResource(R.drawable.coin_animation);
        coinAnimation= (AnimationDrawable) coinImage.getBackground();
        coinAnimation.start();
    }

    private void playSound(int soundId) {
        soundPoolMain.play(soundId, 0.5f, 0.5f, 0, 0, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int totalMonedas = preferencias.getInt("monedas", 0);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
        long ultimaPuntuacion = preferencias.getLong("ultimaPuntuacion", 0); //Recuperamos la ultima puntuacion
        if (ultimaPuntuacion > 0) {
            guardarPuntuacion(ultimaPuntuacion);
            editor.putLong("ultimaPuntuacion", 0); //Quitamos la ultima puntuacion para que no se haga varias veces al cerrar y abrir o volver a la app. La ultima puntuacion solo cambiara al terminar una partida, pasando por aqui y vaciandose de nuevo
            editor.apply();
        }
        activeGame = false;
    }

    private void guardarPuntuacion(long nuevaPuntuacion) {
        Puntuacion nueva = new Puntuacion(nickname, nuevaPuntuacion);
        maximasPuntuaciones.add(nueva);
        Collections.sort(maximasPuntuaciones, new Comparator<Puntuacion>() { //Ordenamos la lista de puntuaciones
            /*Esto funciona porque sort por defecto lo hace de menor a mayor, Long.compare devuelve 1 si el primer argumento es mayor que el segundo, -1 si es menor
             * y los intercambia cuando el resultado es 1. Por ejemplo
             * Lista: [10000,20000,101,50000]
             * Compara 10000 (p1) con 20000 (p2), al poner primero p2 en los argumentos compara 20000 con 10000, y como x>y devuelve 1 y los intercambia
             * quedando la lista [20000,10000,101,50000]
             * Ahora 10000 con 101, al reves queda 101 en p2 y 10000 en p1. y>x, asi que devuelve -1 y no los cambia
             * Por ultimo 101 y 50000. Al cambiarlos queda x 50000 y 101, x>y asi que los cambia
             * Y queda de resultado[20000,10000,50000,101], ordenada de mayor a menor*/
            @Override
            public int compare(Puntuacion p1, Puntuacion p2) {
                return Long.compare(p2.getPuntos(), p1.getPuntos()); //Se ordena en orden descendente
            }
        });
        if (maximasPuntuaciones.size() > 5) { //Si hay mas de 5 puntuaciones, nos quedaremos solo con 5
            maximasPuntuaciones.remove(maximasPuntuaciones.size() - 1);
        }
        String jsonPuntuaciones = new Gson().toJson(maximasPuntuaciones);
        editor.putString("maximasPuntuaciones", jsonPuntuaciones);
        editor.apply();
    }
}