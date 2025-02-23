package edu.pmdm.turbogranny;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import java.util.ArrayList;
import java.util.Random;



public class Juego extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private SurfaceHolder holder; //Controla surfaceview para manejar el dibujo en pantalla
    private AppCompatActivity context;
    //MAPA
    public int maxX = 0, maxY = 0;
    private Bitmap mapa;
    private int mapHeight, mapWidth;
    private float velMapa = 80f;
    private int posMapaX = 0, posMapaY = 0;
    public int frameCount = 0;
    private static final int textoInicialX = 50;
    private static final int textoInicialY = 20;

    private Jugador jugador;
    private Bitmap heart;

    private ArrayList<Enemigo> enemigos=new ArrayList<>();
    private int[] enemigosImagenes={R.drawable.enemy1,R.drawable.enemy2,R.drawable.enemy3,R.drawable.enemy4,R.drawable.enemy5,R.drawable.enemy6};

    private Random random=new Random();
    private Handler handler=new Handler();
    private Runnable generarEnemigo=new Runnable() {
        @Override
        public void run() {
            int imagenId=enemigosImagenes[new Random().nextInt(enemigosImagenes.length)];
            enemigos.add(new Enemigo(Juego.this,BitmapFactory.decodeResource(getResources(),imagenId)));
            generacionEnemigos();
        }
    };

    private ArrayList<Explosion> explosiones=new ArrayList<Explosion>();

    private BucleJuego bucleJuego;

    public Juego(AppCompatActivity context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        this.context = context;

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        getHolder().addCallback(this);
        Canvas canvas = this.holder.lockCanvas();
        maxX = canvas.getWidth();
        maxY = canvas.getHeight();
        this.holder.unlockCanvasAndPost(canvas);

        mapa = BitmapFactory.decodeResource(getResources(), R.drawable.road);
        mapHeight = mapa.getHeight();
        mapWidth = mapa.getWidth();
        mapa = Bitmap.createScaledBitmap(mapa, maxX, (int) ((float) maxX / mapWidth * mapHeight), true);
        mapHeight = mapa.getHeight(); // Actualizar el nuevo alto después de la escala
        mapWidth = maxX; // Ancho ahora es el de la pantalla
        posMapaY = -mapHeight + maxY;
        //CREAMOS Y POSICIONAMOS JUGADOR
        jugador=new Jugador(this,BitmapFactory.decodeResource(getResources(), R.drawable.car1));
        jugador.posY = maxY - jugador.spriteHeight;
        jugador.posX = maxX / 2 - jugador.spriteWidth / 2;

        heart =BitmapFactory.decodeResource(getResources(),R.drawable.heart);

        //GENERAR ENEMIGOS
        generacionEnemigos();

        //Creamos el Gameloop
        bucleJuego = new BucleJuego(getHolder(), this);
        setFocusable(true);
        setOnTouchListener(this);
        bucleJuego.start();
    }

    private void generacionEnemigos(){
        double delay=1000.0+(random.nextDouble()*(2000.0));
        handler.postDelayed(generarEnemigo,(long)delay);
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawColor(Color.RED);
            canvas.drawBitmap(mapa, posMapaX, posMapaY, null);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(40);
            paint.setColor(Color.WHITE);
            Rect textBounds = new Rect();
            paint.getTextBounds("Frames ejecutados", 0, 1, textBounds);
            canvas.drawText("Frames ejecutados: " + frameCount, textoInicialX, maxY- textBounds.height(), paint);

            jugador.render(canvas,paint);
            //Pintamos las vidas
            for (int i = 0; i < jugador.vidas; i++) {
                canvas.drawBitmap(heart, 50 +heart.getWidth()*i, 50, null);
            }

            //Pintamos los enemigos
            for(int i=0;i<enemigos.size();i++){ //Se usa for normal y no foreach para evitar ConcurrentModificationException
                enemigos.get(i).render(canvas,paint);
            }
            for(int i=0;i<explosiones.size();i++){
                explosiones.get(i).render(canvas,paint);
            }
        }
    }

    public void update() {
        frameCount++;
        //MOVIMIENTO MAPA
        posMapaY += velMapa;
        if (posMapaY >= 0) {
            posMapaY = -mapHeight + maxY;
        }
        jugador.update();
        //Actualizamos los enemigos
        for(int i=0;i<enemigos.size();i++){ //Se usa for normal y no foreach para evitar ConcurrentModificationException
            Enemigo enemigo=enemigos.get(i);
            enemigo.update(enemigos);
            if(enemigo.getHitbox().intersect(jugador.getHitbox())){
                jugador.vidas--; //Restamos una vida al jugador
                explosiones.add(new Explosion(this,BitmapFactory.decodeResource(getResources(),R.drawable.explosion),enemigo.posX,enemigo.posY));
                enemigos.remove(i);
            }
            if(enemigo.posY>maxY){
                enemigos.remove(i);
            }
        }
        for(int i=0;i<explosiones.size();i++){
            explosiones.get(i).update();
            if(explosiones.get(i).finished()) explosiones.remove(i);
        }

        //Si el jugador se ha quedado sin vidas, pierde
        if(jugador.vidas<=0 && jugador.activo){
            jugador.spriteEstado=3; //Coche roto
            jugador.velX=0; //Paramos el coche en horizontal
            jugador.velY=10;
            jugador.activo=false; //Desactivamos el jugador
            explosiones.add(new Explosion(this,BitmapFactory.decodeResource(getResources(),R.drawable.explosion),jugador.posX,jugador.posY));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bucleJuego.fin(); //Paramos el bucle del juego
                    context.finish();
                }
            }, 1500); //Se cerrara tras 1 segundo y medio

        }
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index, x, y;
        index = MotionEventCompat.getActionIndex(event); //Obtener el pointer de la accion

        //Coordenadas del toque
        x = (int) MotionEventCompat.getX(event, index);
        y = (int) MotionEventCompat.getY(event, index);
        //Identificar evento
        if(jugador.activo){
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: //Primer dedo toca la pantalla
                case MotionEvent.ACTION_POINTER_DOWN: //Otro dedo toca la pantalla
                    if (x < maxX / 2) { // Toque en la mitad izquierda
                        jugador.velX = -jugador.VELOCIDAD;
                    } else { // Toque en la mitad derecha
                        jugador.velX = jugador.VELOCIDAD;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP: // un dedo levanta el toque pero hay otros tocando
                case MotionEvent.ACTION_UP: //Ultimo dedo levanta el toque
                    jugador.velX = 0; // Dejar de girar el coche cuando se levanta el toque
                    break;
            }
        }
        return true;
    }
}