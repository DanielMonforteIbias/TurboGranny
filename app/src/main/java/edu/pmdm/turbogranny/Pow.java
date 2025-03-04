package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Pow {
    private final Juego juego;
    private final Bitmap spriteSheet;
    private final int totalFotogramas = 8;
    private int fotogramaActual = 0;
    private final int anchoFotograma;
    private final int altoFotograma;
    private float posX, posY;
    private final float velocidad = 35f;
    private boolean activa = true;
    public final int PUNTOS=1000;

    public Pow(Juego juego, Bitmap spriteSheet, float x, float y) {
        this.juego = juego;
        this.spriteSheet = spriteSheet;
        this.anchoFotograma = spriteSheet.getWidth() / totalFotogramas;
        this.altoFotograma = spriteSheet.getHeight();
        this.posX = x;
        this.posY = y;
    }

    public void update() {
        if(juego.isJuegoPausado()) return;

        // Movimiento
        posY += velocidad;

        // Animación
        if(juego.frameCount % 5 == 0) {
            fotogramaActual = (fotogramaActual + 1) % totalFotogramas;
        }

        // Desactivar si sale de pantalla
        if(posY > juego.maxY) {
            activa = false;
        }
    }

    public void render(Canvas canvas, Paint paint) {
        if(activa) {
            Rect origen = new Rect(
                    fotogramaActual * anchoFotograma,
                    0,
                    (fotogramaActual + 1) * anchoFotograma,
                    altoFotograma
            );

            Rect destino = new Rect(
                    (int)posX,
                    (int)posY,
                    (int)(posX + anchoFotograma),
                    (int)(posY + altoFotograma)
            );

            canvas.drawBitmap(spriteSheet, origen, destino, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(
                (int)posX,
                (int)posY,
                (int)(posX + anchoFotograma),
                (int)(posY + altoFotograma)
        );
    }

    public boolean isActivo() {
        return activa;
    }
}