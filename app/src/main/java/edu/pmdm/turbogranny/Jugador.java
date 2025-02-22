package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Jugador {
    private Bitmap sprite;
    public int spriteHeight, spriteWidth;
    public int spriteEstado;
    public float posX, posY;
    public float velX, velY;
    public float VELOCIDAD = 50f;

    public Jugador(Bitmap sprite) {
        this.sprite = sprite;
        this.spriteHeight=sprite.getHeight();
        this.spriteWidth=sprite.getWidth()/4;
    }

    public void update(){

    }
    public void render(Canvas canvas, Paint paint){
        int spriteNumber = spriteWidth * spriteEstado;
        canvas.drawBitmap(sprite, new Rect(spriteNumber, 0, spriteNumber + spriteWidth, spriteHeight),
                new Rect((int) posX, (int) posY, (int) posX + spriteWidth, (int) posY+spriteHeight), null);
    }
}
