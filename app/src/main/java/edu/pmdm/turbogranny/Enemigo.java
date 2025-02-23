package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
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


    public void update(ArrayList<Enemigo> otrosEnemigos) {
        // Ajustamos  la velocidad si está cerca de otros coches
        ajusteVelocidad(otrosEnemigos, 100, 5); // 100 es la distancia mínima de seguridad, 5 es la reducción de velocidad

        // Avanza
        posY += velY;

        // vemos si colisiona con algún otro enemigo
        for (Enemigo otroEnemigo : otrosEnemigos) {
            if (otroEnemigo != this && this.chocara(otroEnemigo)) {
                // Si colisiona, ajustamos la velocidad
                if (this.velY <= 5) {
                    this.velY += 5; // Aumentamos la velocidad en 5 unidades
                } else {
                    this.velY = Math.min(this.velY, otroEnemigo.velY - 5); // restamos
                }

                // nso aseguramos que la velocidad no sea menor que un valor mínimo
                float minSpeed = 10; // necesitamos una velocidad minima para no quedar atascados
                this.velY = Math.max(this.velY, minSpeed);

                break; // No es necesario seguir verificando
            }
        }

        // Si el enemigo sale de la pantalla, desactívalo
        if (posY > juego.maxY) {
            activo = false;
        }
    }

    public boolean chocara(Enemigo otroEnemigo) {
        Rect thisHitbox = new Rect(
                (int) posX,
                (int) posY,
                (int) (posX + spriteWidth),
                (int) (posY + spriteHeight)
        );

        Rect otherHitbox = new Rect(
                (int) otroEnemigo.posX,
                (int) otroEnemigo.posY,
                (int) (otroEnemigo.posX + otroEnemigo.spriteWidth),
                (int) (otroEnemigo.posY + otroEnemigo.spriteHeight)
        );

        // Verifica si los hitboxes se superponen
        return thisHitbox.intersect(otherHitbox);
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
                    float minSpeed = 10; // Velocidad mínima para evitar que se queden atascados
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