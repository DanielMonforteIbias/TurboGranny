package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Moneda {
    private Juego juego;
    private Bitmap[] fotogramas;   // Array de fotogramas para la animación
    private int indiceFotograma = 0;
    public float posX, posY;       // Posición de la moneda
    private float velocidad = 10f; // Velocidad de caída (ajústala según necesites)
    private int anchoSprite, altoSprite;

    public Moneda(Juego juego, Bitmap[] fotogramas, float x, float y) {
        this.juego = juego;
        this.fotogramas = fotogramas;
        this.posX = x;
        this.posY = y;
        // Se asume que todos los fotogramas tienen el mismo tamaño
        this.anchoSprite = fotogramas[0].getWidth();
        this.altoSprite = fotogramas[0].getHeight();
    }

    public void actualizar() {
        // Mover la moneda hacia abajo
        posY += velocidad;

        // Cambiar de fotograma para la animación cada 5 frames del juego
        if (juego.frameCount % 5 == 0) {
            indiceFotograma++;
            if (indiceFotograma >= fotogramas.length) {
                indiceFotograma = 0;
            }
        }
    }

    public void dibujar(Canvas canvas, Paint pincel) {
        canvas.drawBitmap(fotogramas[indiceFotograma], posX, posY, pincel);
    }

    public Rect obtenerHitbox() {
        return new Rect(
                (int) posX,
                (int) posY,
                (int) (posX + anchoSprite),
                (int) (posY + altoSprite)
        );
    }
}
