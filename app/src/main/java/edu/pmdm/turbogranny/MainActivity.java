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

import java.util.HashMap;

import edu.pmdm.turbogranny.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int carIndex=0;
    private int[]cars={R.drawable.car1,R.drawable.car2,R.drawable.car3,R.drawable.car4,R.drawable.car5}; //Necesario para mostrar y animar solo la imagen de los coches quietos
    private int[]carsSpriteSheets={R.drawable.car1spritesheet,R.drawable.car2spritesheet,R.drawable.car3spritesheet,R.drawable.car4spritesheet,R.drawable.car5spritesheet}; //Necesario para pasar el spritesheet correcto
    private int[] carLocation = new int[2];
    private int carX, screenWidth;
    private boolean changingCar=false, activeGame=false; //para bloquear otro cambio mientras se cambia
    private String nickname="USER";
    private SharedPreferences preferencias;
    private SharedPreferences.Editor editor;

    private SoundPool soundPoolMain;
    int changeCarSound, buySound, okaySound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencias=getSharedPreferences("DatosJuego",MODE_PRIVATE);
        editor=preferencias.edit();

        cargarSoundPoolMain();
        int totalMonedas = preferencias.getInt("monedas", 000); // Obtener y mostrar el total de monedas guardado en SharedPreferences
        nickname=preferencias.getString("nickname","USER"); //Obtener el nickname de preferencias

        binding.txtNickname.setText(nickname);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
        binding.imgCar.setImageBitmap(BitmapFactory.decodeResource(getResources(),cars[carIndex]));

        binding.startButton.setOnClickListener(v -> {
            if(!activeGame){
                Intent intent = new Intent(MainActivity.this, BlackScreenActivity.class);
                intent.putExtra("carId",carsSpriteSheets[carIndex]);
                activeGame=true;
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        binding.imgBtnLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!changingCar){
                    carIndex =(carIndex-1+cars.length)%cars.length; //Nos movemos 1 hacia atras, asegurando que vaya al ultimo si estamos en el primero
                    changeCar(-1);
                    playSound(changeCarSound);
                }
            }
        });
        binding.imgBtnRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!changingCar){
                    carIndex=(carIndex+1)%cars.length; //Nos movemos 1 hacia delante, asegurando que vaya al primero si estamos en el ultimo
                    changeCar(1);
                    playSound(changeCarSound);
                }

            }
        });
        binding.imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_nickname,null);
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
                            nickname=newNickname;
                            editor.putString("nickname",newNickname);
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
        });

        binding.imgBtnLeaderboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_leaderboards,null);
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
                dialog.show();
            }
        });

        screenWidth=getResources().getDisplayMetrics().widthPixels;
        binding.imgCar.getLocationOnScreen(carLocation);
        carX = carLocation[0];
        animarBotones();
        animarMoneda();
    }

    private void cargarSoundPoolMain(){
        soundPoolMain=new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        changeCarSound=soundPoolMain.load(this,R.raw.change,1);
        buySound=soundPoolMain.load(this,R.raw.buy,1);
        okaySound=soundPoolMain.load(this,R.raw.okay,1);
    }
    private void changeCar(int direccion){ //Direccion es -1 a la izquierda, 1 a la derecha
        changingCar=true;
        AnimatorSet carAnimator=new AnimatorSet();
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
                        changingCar=false;
                    }
                });
                trasladarEntrada.start();
            }
        });
        carAnimator.play(trasladarSalida);
        carAnimator.start();
    }

    private void animarBotones(){
        AnimatorSet btnAnimator=new AnimatorSet();
        ObjectAnimator colorBtn=ObjectAnimator.ofArgb(binding.startButton,"backgroundColor", ContextCompat.getColor(this,R.color.car1red),ContextCompat.getColor(this,R.color.car1blue));
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

        AnimatorSet settingsAnimator=new AnimatorSet();
        ObjectAnimator rotationSettings=ObjectAnimator.ofFloat(binding.imgBtnSettings,"rotation",0,360);
        rotationSettings.setDuration(15000);
        rotationSettings.setRepeatCount(ObjectAnimator.INFINITE);
        rotationSettings.setInterpolator(new LinearInterpolator());
        settingsAnimator.play(rotationSettings);
        settingsAnimator.start();
    }

    private void animarMoneda(){
        AnimationDrawable coinAnimation;
        ImageView coinImage=findViewById(R.id.imgCoin);
        coinImage.setBackgroundResource(R.drawable.coin_animation);
        coinAnimation= (AnimationDrawable) coinImage.getBackground();
        coinAnimation.start();
    }

    private void playSound(int soundId){
        soundPoolMain.play(soundId,1,1,0,0,1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int totalMonedas = getSharedPreferences("DatosJuego", MODE_PRIVATE).getInt("monedas", 0);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
        activeGame=false;
    }

}