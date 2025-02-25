package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Moneda {
    private Juego juego;
    private Bitmap spriteSheet;
    private int totalFotogramas = 6;   // Número total de fotogramas en la tira
    private int fotogramaActual = 0;   // Índice del fotograma que se está mostrando
    private int anchoFotograma;
    private int altoFotograma;

    public float posX, posY;          // Posición de la moneda
    private float velocidad = 20f;    // Velocidad de caída

    public Moneda(Juego juego, int recursoSpriteSheet, float x, float y) {
        this.juego = juego;
        this.posX = x;
        this.posY = y;

        // Cargamos el sprite sheet desde recursos
        spriteSheet = BitmapFactory.decodeResource(juego.getContext().getResources(), recursoSpriteSheet);


        this.anchoFotograma = spriteSheet.getWidth() / totalFotogramas;
        this.altoFotograma  = spriteSheet.getHeight();
    }

    public void actualizar() {
        // Mover la moneda hacia abajo
        posY += velocidad;

        // Cambiar de fotograma cada ciertos frames del juego
        if (juego.frameCount % 5 == 0) {
            fotogramaActual++;
            if (fotogramaActual >= totalFotogramas) {
                fotogramaActual = 0; // Volver al primer fotograma
            }
        }
    }

    public void dibujar(Canvas canvas, Paint pincel) {
        // Calculamos el rectángulo de origen (el trozo del spriteSheet que corresponde al fotogramaActual)
        int xOrigen = fotogramaActual * anchoFotograma;
        Rect origen = new Rect(
                xOrigen,
                0,
                xOrigen + anchoFotograma,
                altoFotograma
        );

        // Rectángulo destino en pantalla (posX, posY)
        Rect destino = new Rect(
                (int) posX,
                (int) posY,
                (int) (posX + anchoFotograma),
                (int) (posY + altoFotograma)
        );

        // Dibujamos la parte correspondiente del spriteSheet
        canvas.drawBitmap(spriteSheet, origen, destino, pincel);
    }

    public Rect obtenerHitbox() {
        return new Rect(
                (int) posX,
                (int) posY,
                (int) (posX + anchoFotograma),
                (int) (posY + altoFotograma)
        );
    }
}
