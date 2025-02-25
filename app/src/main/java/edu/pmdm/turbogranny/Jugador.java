package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Jugador {
    private Juego juego;
    private Bitmap sprite;
    public int spriteHeight, spriteWidth;
    public int spriteEstado;
    public float posX, posY;
    public float velX, velY;
    public float VELOCIDADX = 50f;
    public float VELOCIDADY=10f;
    public int vidas;
    public boolean activo;


    public Jugador(Juego j,Bitmap sprite) {
        this.juego=j;
        this.sprite = sprite;
        this.spriteHeight=sprite.getHeight();
        this.spriteWidth=sprite.getWidth()/4;
        this.vidas=3;
        this.activo=true;
    }

    public void update(){
        if(vidas>0){
            if(velX!=0){
                if(velX<0) spriteEstado=1;
                else spriteEstado=2;
                posX+=velX; //No se usa deltaTime porque la regulacion de frames ya se hace en BucleJuego usando Thread.sleep, lo que elimina la necesidad de usar deltaTime aqui
                if(posX<0 || posX+spriteWidth>juego.maxX) posX-=velX;
            }
            else spriteEstado=0;
            if(velY!=0){
                posY+=velY; //No se usa deltaTime porque la regulacion de frames ya se hace en BucleJuego usando Thread.sleep, lo que elimina la necesidad de usar deltaTime aqui
                if(posY<0 || posY+spriteHeight>juego.maxY) posY-=velY;
            }
        }
        else {
            posY+=velY;
            spriteEstado=3;
        }
    }
    public void render(Canvas canvas, Paint paint){
        int spriteNumber = spriteWidth * spriteEstado;
        canvas.drawBitmap(sprite, new Rect(spriteNumber, 0, spriteNumber + spriteWidth, spriteHeight),
                new Rect((int) posX, (int) posY, (int) posX + spriteWidth, (int) posY+spriteHeight), paint);
    }

    public Rect getHitbox() {
        return new Rect((int) posX, (int) posY, (int) (posX + spriteWidth), (int) (posY + spriteHeight));
    }
}