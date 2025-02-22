package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

public class Enemigo {
    private Juego juego;
    private Bitmap sprite;
    public int spriteHeight, spriteWidth;
    public float posX, posY;
    public float velX, velY;
    private boolean activo;

    public Enemigo(Juego j, Bitmap sprite) {
        this.juego = j;
        this.sprite = sprite;
        this.spriteHeight = sprite.getHeight();
        this.spriteWidth = sprite.getWidth();
        this.posX = new Random().nextInt(juego.maxX - spriteWidth);
        this.posY = -spriteHeight;
        this.velY = (float)((Math.random()*20)+11);
        this.activo=true;
    }

    public void update() {
        posY += velY;
        if (posY > juego.maxY) {
            activo=false;
        }
    }

    public void render(Canvas canvas, Paint paint) {
            canvas.drawBitmap(sprite, posX, posY, paint);
    }

    public Rect getHitbox() {
        return new Rect((int) posX, (int) posY, (int) (posX + spriteWidth), (int) (posY + spriteHeight));
    }
}