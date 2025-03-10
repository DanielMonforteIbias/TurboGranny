package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;

import java.util.Random;

public class Explosion {
    private Juego juego;
    private Bitmap sprite;
    public int spriteHeight, spriteWidth;
    public float posX, posY;
    private int estado;
    private final int SPRITE_COUNT=5;
    private MediaPlayer mediaPlayer;

    public Explosion(Juego j,Bitmap sprite,float x,float y) {
        this.juego=j;
        this.sprite = sprite;
        this.spriteHeight = sprite.getHeight();
        this.spriteWidth = sprite.getWidth()/SPRITE_COUNT;
        this.posX = x;
        this.posY = y;
        this.estado=-1;
        int explosionIndex=new Random().nextInt(juego.explosionesSonidos.length);
        mediaPlayer = MediaPlayer.create(j.getContext(), juego.explosionesSonidos[explosionIndex]);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    public void update() {
        if(juego.frameCount%2==0)estado++;
    }

    public void render(Canvas canvas, Paint paint) {
        int posicionSprite = estado * spriteWidth;
        if (!finished()) {
            Rect origen = new Rect(posicionSprite, 0, posicionSprite + spriteWidth, spriteHeight);
            Rect destino = new Rect((int) posX, (int) posY, (int) posX + spriteWidth, (int) posY + spriteHeight);
            canvas.drawBitmap(sprite, origen, destino, paint);
        }
    }
    public boolean finished() {
        return estado >= SPRITE_COUNT;
    }
}
