package edu.pmdm.turbogranny;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import edu.pmdm.turbogranny.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int carIndex=0;
    private int[]cars={R.drawable.car1,R.drawable.car2,R.drawable.car3,R.drawable.car4,R.drawable.car5}; //Necesario para mostrar y animar solo la imagen de los coches quietos
    private int[]carsSpriteSheets={R.drawable.car1spritesheet,R.drawable.car2spritesheet,R.drawable.car3spritesheet,R.drawable.car4spritesheet,R.drawable.car5spritesheet}; //Necesario para pasar el spritesheet correcto
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    // Obtener y mostrar el total de monedas guardado en SharedPreferences

        int totalMonedas = getSharedPreferences("DatosJuego", MODE_PRIVATE).getInt("monedas", 000);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
        changeCar();
        binding.startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActividadJuego.class);
            intent.putExtra("carId",carsSpriteSheets[carIndex]);
            startActivity(intent);
        });
        binding.imgBtnLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carIndex =(carIndex-1+cars.length)%cars.length; //Nos movemos 1 hacia atras, asegurando que vaya al ultimo si estamos en el primero
                changeCar();
            }
        });
        binding.imgBtnRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carIndex=(carIndex+1)%cars.length; //Nos movemos 1 hacia delante, asegurando que vaya al primero si estamos en el ultimo
                changeCar();
            }
        });
        animarBoton();
        animarMoneda();
    }

    private void changeCar(){
        animacionCambioCoche();
    }

    private void animarBoton(){
        AnimatorSet btnAnimator=new AnimatorSet();
        Button btn=findViewById(R.id.start_button);
        ObjectAnimator color=ObjectAnimator.ofArgb(btn,"backgroundColor", ContextCompat.getColor(this,R.color.car1red),ContextCompat.getColor(this,R.color.car1blue));
        color.setDuration(1500);
        color.setRepeatCount(ObjectAnimator.INFINITE);
        color.setRepeatMode(ObjectAnimator.REVERSE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.05f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);

        btnAnimator.play(color).with(scaleX).with(scaleY);
        btnAnimator.start();
    }

    private void animarMoneda(){
        AnimationDrawable coinAnimation;
        ImageView coinImage=findViewById(R.id.imgCoin);
        coinImage.setBackgroundResource(R.drawable.coin_animation);
        coinAnimation= (AnimationDrawable) coinImage.getBackground();
        coinAnimation.start();
    }

    private void animacionCambioCoche(){
        AnimatorSet carAnimator=new AnimatorSet();
        ObjectAnimator trasladar=ObjectAnimator.ofFloat(binding.imgCar,"translationX",binding.imgCar.getX(),getResources().getDisplayMetrics().widthPixels);
        trasladar.setDuration(500);
        carAnimator.play(trasladar);
        binding.imgCar.setImageBitmap(BitmapFactory.decodeResource(getResources(),cars[carIndex]));
        ObjectAnimator trasladarVuelta=ObjectAnimator.ofFloat(binding.imgCar,"translationX",getResources().getDisplayMetrics().widthPixels,binding.imgCar.getX());
        trasladarVuelta.setDuration(500);
        carAnimator.play(trasladarVuelta);
        carAnimator.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        int totalMonedas = getSharedPreferences("DatosJuego", MODE_PRIVATE).getInt("monedas", 0);
        binding.txtCoins.setText(String.valueOf(totalMonedas));
    }

}