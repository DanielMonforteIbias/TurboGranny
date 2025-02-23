package edu.pmdm.turbogranny;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class BucleJuego extends Thread{
    private Juego juego;
    private SurfaceHolder surfaceHolder;
    public boolean JuegoEnEjecucion=true;
    public final static int MAX_FPS=30;
    public final static int TIEMPO_FRAME = 1000/ MAX_FPS;
    private final static int   MAX_FRAMES_SALTADOS = 5;

    public BucleJuego(SurfaceHolder sh, Juego s){
        juego=s;
        surfaceHolder=sh;
    }

    public void run(){
        Canvas canvas;
        Log.d(TAG,"Comienza el game loop");
        long tiempoComienzo;
        long tiempoDiferencia;
        int tiempoDormir=0;
        int framesASaltar;
        while(JuegoEnEjecucion){
            canvas=null;
            try{
                canvas= this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    tiempoComienzo = System.currentTimeMillis();
                    framesASaltar=0;
                    juego.update();
                    juego.render(canvas);
                    tiempoDiferencia= System.currentTimeMillis() - tiempoComienzo;
                    tiempoDormir = (int) (TIEMPO_FRAME-tiempoDiferencia);
                    if (tiempoDormir>0){
                        try{
                            Thread.sleep(tiempoDormir);
                        }catch (InterruptedException e){}
                    }
                    while (tiempoDormir<0 && framesASaltar > MAX_FRAMES_SALTADOS ){
                        juego.update();
                        tiempoDormir += TIEMPO_FRAME;
                        framesASaltar++;
                    }
                }

                }finally {
                if (canvas!=null){
                    surfaceHolder.unlockCanvasAndPost(canvas);

                }
            }
        }
    }
    public void fin(){JuegoEnEjecucion=false;}
}
