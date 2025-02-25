package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Vida {
    private Juego juego;
    private Bitmap spriteSheet;
    private int totalFotogramas = 6; // Número de fotogramas en el sprite sheet
    private int fotogramaActual = 0; // Fotograma actual de la animación
    private int anchoFotograma, altoFotograma;
    public float posX, posY;
    public float velocidad = 35f; // Velocidad de caída (ajusta este valor para que sea rápido)
    private boolean activa = true;

    public Vida(Juego juego, Bitmap spriteSheet, float x, float y) {
        this.juego = juego;
        this.spriteSheet = spriteSheet;
        this.anchoFotograma = spriteSheet.getWidth() / totalFotogramas;
        this.altoFotograma = spriteSheet.getHeight();
        this.posX = x;
        this.posY = y;
    }

    public void update() {
        // Mover la vida hacia abajo
        posY += velocidad;

        // Cambiar de fotograma cada ciertos frames del juego
        if (juego.frameCount % 5 == 0) { // Cambia el fotograma cada 5 frames
            fotogramaActual = (fotogramaActual + 1) % totalFotogramas;
        }

        // Desactivar la vida si sale de la pantalla
        if (posY > juego.maxY) {
            activa = false;
        }
    }

    public void render(Canvas canvas, Paint paint) {
        if (activa) {
            // Calcular el rectángulo de origen (el trozo del spriteSheet que corresponde al fotogramaActual)
            int xOrigen = fotogramaActual * anchoFotograma;
            Rect origen = new Rect(xOrigen, 0, xOrigen + anchoFotograma, altoFotograma);

            // Rectángulo destino en pantalla (posX, posY)
            Rect destino = new Rect((int) posX, (int) posY, (int) (posX + anchoFotograma), (int) (posY + altoFotograma));

            // Dibujar la parte correspondiente del spriteSheet
            canvas.drawBitmap(spriteSheet, origen, destino, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect((int) posX, (int) posY, (int) (posX + anchoFotograma), (int) (posY + altoFotograma));
    }

    public boolean isActiva() {
        return activa;
    }
}