package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.Random;

public class Pow {
    private final Juego juego;
    private final Bitmap spriteSheet;
    private final int totalFotogramas = 8;
    private int fotogramaActual = 0;
    private final int anchoFotograma;
    private final int altoFotograma;
    public float posX, posY;
    private boolean activo = false;
    private final Random random = new Random();
    private long tiempoUltimaAparicion;
    private static final long MIN_INTERVALO = 40000; // 40 segundos
    private static final long MAX_INTERVALO = 60000; // 60 segundos
    private long intervaloActual = MIN_INTERVALO;

    // Constructor
    public Pow(Juego juego, Bitmap spriteSheet) {
        this.juego = juego;
        this.spriteSheet = spriteSheet;
        this.anchoFotograma = spriteSheet.getWidth() / totalFotogramas;
        this.altoFotograma = spriteSheet.getHeight();
    }

    public void generar() {
        if (System.currentTimeMillis() - tiempoUltimaAparicion > intervaloActual) {
            posX = random.nextInt(juego.maxX - anchoFotograma);
            posY = -altoFotograma;
            activo = true;
            tiempoUltimaAparicion = System.currentTimeMillis();
            intervaloActual = MIN_INTERVALO + (long) (random.nextFloat() * (MAX_INTERVALO - MIN_INTERVALO));
        }
    }


    public void update() {
        if (activo) {
            posY += 15; // Velocidad de caída

            // Animación
            if (juego.frameCount % 5 == 0) {
                fotogramaActual = (fotogramaActual + 1) % totalFotogramas;
            }

            // Verificar colisión con jugador (CORRECCIÓN PRINCIPAL)
            if (getHitbox().intersect(juego.getJugadorHitbox())) {
                activo = false;
                juego.activarEfectoPow();
            }
        }
    }

    public void render(Canvas canvas, Paint paint) {
        if (activo) {
            Rect origen = new Rect(
                    fotogramaActual * anchoFotograma,
                    0,
                    (fotogramaActual + 1) * anchoFotograma,
                    altoFotograma
            );

            Rect destino = new Rect(
                    (int) posX,
                    (int) posY,
                    (int) (posX + anchoFotograma),
                    (int) (posY + altoFotograma)
            );

            canvas.drawBitmap(spriteSheet, origen, destino, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(
                (int) posX,
                (int) posY,
                (int) (posX + anchoFotograma),
                (int) (posY + altoFotograma)
        );
    }
}