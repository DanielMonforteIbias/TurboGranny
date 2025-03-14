package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Random;

public class Enemigo {
    private Juego juego;
    private Bitmap sprite;
    public int spriteHeight, spriteWidth;
    public float posX, posY;
    public float velX, velY;
    private float minSpeed; // necesitamos una velocidad minima para no quedar atascados
    private boolean activo;
    public final int PUNTOS=-270;
    private MediaPlayer mediaPlayer;

    public Enemigo(Juego j, Bitmap sprite) {
        this.juego = j;
        this.sprite = sprite;
        this.spriteHeight = sprite.getHeight();
        this.spriteWidth = sprite.getWidth();
        minSpeed=juego.velMinEnemigos;
        this.posX = new Random().nextInt(juego.maxX - spriteWidth);
        this.posY = -spriteHeight;
        this.velY = (float)((Math.random()*20)+minSpeed);
        this.activo=true;
        int claxonIndex=new Random().nextInt(juego.claxonSonidos.length);
        mediaPlayer = MediaPlayer.create(j.getContext(), juego.claxonSonidos[claxonIndex]);
    }


    public void update(ArrayList<Enemigo> otrosEnemigos) {
        // Ajustamos  la velocidad si está cerca de otros coches
        ajusteVelocidad(otrosEnemigos, 100, 5); // 100 es la distancia mínima de seguridad, 5 es la reducción de velocidad

        // Avanza
        posY += velY;

        // vemos si colisiona con algún otro enemigo
        for (Enemigo otroEnemigo : otrosEnemigos) {
            if (otroEnemigo != this && this.chocara(otroEnemigo)) {
                // Si colisiona, ajustamos la velocidad
                if (this.posY < otroEnemigo.posY) {
                    this.velY-=5;
                    otroEnemigo.velY+=1;
                } else {
                    this.velY+=1;
                    otroEnemigo.velY-=5;
                }
                // nos aseguramos que la velocidad no sea menor que un valor mínimo
                this.velY = Math.max(this.velY, minSpeed);
                otroEnemigo.velY = Math.max(otroEnemigo.velY, minSpeed);
                mediaPlayer.start();
                break; // No es necesario seguir verificando
            }
        }

        // Si el enemigo sale de la pantalla, desactívalo
        if (posY > juego.maxY) {
            activo = false;
        }
    }

    public boolean chocara(Enemigo otroEnemigo) {
        return this.getHitbox().intersect(otroEnemigo.getHitbox());
    }

    public void ajusteVelocidad(ArrayList<Enemigo> otrosEnemigos, float minDistancia, float velocidadReducida) {
        for (Enemigo otroEnemigo : otrosEnemigos) {
            if (otroEnemigo != this) {
                float dx = this.posX - otroEnemigo.posX;
                float dy = this.posY - otroEnemigo.posY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Si está cerca de otro coche, ajusta la velocidad
                if (distance < minDistancia) {
                    if (this.velY <= 5) {
                        // Si la velocidad ya es 5 o menor, la aumenta en 5 unidades
                        this.velY += 5;
                    } else {
                        // Si la velocidad es mayor que 5, la reduce
                        this.velY = Math.min(this.velY, otroEnemigo.velY - velocidadReducida);
                    }

                    // Asegura que la velocidad no sea menor que un valor mínimo
                    this.velY = Math.max(this.velY, minSpeed);
                }
            }
        }
    }

    public void render(Canvas canvas, Paint paint) {
            canvas.drawBitmap(sprite, posX, posY, paint);
    }

    public Rect getHitbox() {
        return new Rect((int) posX, (int) posY, (int) (posX + spriteWidth), (int) (posY + spriteHeight));
    }
}