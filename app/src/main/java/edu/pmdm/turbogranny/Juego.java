package edu.pmdm.turbogranny;

import static android.content.Context.MODE_PRIVATE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
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


    private Jugador jugador;
    public int carId;

    private SoundPool soundPool;
    private int engineSoundId;
    private Bitmap heart;

    private ArrayList<Enemigo> enemigos=new ArrayList<>();
    private int[] enemigosImagenes={R.drawable.enemy1,R.drawable.enemy2,R.drawable.enemy3,R.drawable.enemy4,R.drawable.enemy5,R.drawable.enemy6,R.drawable.enemy7,R.drawable.enemy8,R.drawable.enemy9};

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
    public int[]explosionesSonidos={R.raw.explosion1,R.raw.explosion2,R.raw.explosion3};
    public int[]claxonSonidos={R.raw.claxon1,R.raw.claxon2,R.raw.claxon3,R.raw.claxon4};

    public BucleJuego bucleJuego;
    private ArrayList<Moneda> monedas = new ArrayList<>();

    private Handler manejadorMonedas = new Handler();
    private Runnable generarMoneda = new Runnable() {
        @Override
        public void run() {
            crearMoneda();
            programarSiguienteMoneda();
        }
    };
    private Bitmap[] fotogramasMoneda;
    private int monedasPartida = 0;

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
        jugador=new Jugador(this,BitmapFactory.decodeResource(getResources(), carId));
        jugador.posY = maxY - jugador.spriteHeight;
        jugador.posX = maxX / 2 - jugador.spriteWidth / 2;
        //Sonido de motor
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        engineSoundId = soundPool.load(context, R.raw.engine, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundPool.play(engineSoundId, 1, 1, 0, -1, 1);// Reproducir en bucle sin cortes
            }
        });

        heart =BitmapFactory.decodeResource(getResources(),R.drawable.heart);
        heart=Bitmap.createScaledBitmap(heart, (int)(heart.getWidth()*1.3), (int)(heart.getHeight()*1.3), true); //Hacemos el sprite un 1.3 mas grande


        // Cargar fotogramas de la moneda
        fotogramasMoneda = new Bitmap[6];
        fotogramasMoneda[0] = BitmapFactory.decodeResource(getResources(), R.drawable.coin0);
        fotogramasMoneda[1] = BitmapFactory.decodeResource(getResources(), R.drawable.coin1);
        fotogramasMoneda[2] = BitmapFactory.decodeResource(getResources(), R.drawable.coin2);
        fotogramasMoneda[3] = BitmapFactory.decodeResource(getResources(), R.drawable.coin3);
        fotogramasMoneda[4] = BitmapFactory.decodeResource(getResources(), R.drawable.coin4);
        fotogramasMoneda[5] = BitmapFactory.decodeResource(getResources(), R.drawable.coin5);

        for (int i = 0; i < fotogramasMoneda.length; i++) {
            fotogramasMoneda[i] = Bitmap.createScaledBitmap(
                    fotogramasMoneda[i],
                    (int)(fotogramasMoneda[i].getWidth() * 1.5),
                    (int)(fotogramasMoneda[i].getHeight() * 1.5),
                    true
            );
        }
        //GENERAR ENEMIGOS
        generacionEnemigos();
        programarSiguienteMoneda();

        //Creamos el Gameloop
        bucleJuego = new BucleJuego(getHolder(), this);
        setFocusable(true);
        setOnTouchListener(this);
        bucleJuego.start();
    }

    private void crearMoneda() {
        // Posición X aleatoria dentro de la pantalla
        int x = random.nextInt(maxX - fotogramasMoneda[0].getWidth());
        // La moneda inicia justo arriba de la pantalla
        int y = -fotogramasMoneda[0].getHeight();

        Moneda moneda = new Moneda(this, fotogramasMoneda, x, y);
        monedas.add(moneda);
    }

    private void programarSiguienteMoneda() {
        // Intervalo aleatorio entre 1.5 y 3.5 segundos
        double delay = 1500.0 + (random.nextDouble() * 2000.0);
        manejadorMonedas.postDelayed(generarMoneda, (long) delay);
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

            //Pintamos los enemigos
            for(int i=0;i<enemigos.size();i++){ //Se usa for normal y no foreach para evitar ConcurrentModificationException
                enemigos.get(i).render(canvas,paint);
            }
            for(int i=0;i<explosiones.size();i++){
                explosiones.get(i).render(canvas,paint);
            }
            // Dibujar monedas
            for (int i = 0; i < monedas.size(); i++) {
                monedas.get(i).dibujar(canvas, paint);
            }

            //La interfaz se pinta lo ultimo para que esté por encima de lo demas
            //Pintamos las vidas
            for (int i = 0; i < jugador.vidas; i++) {
                canvas.drawBitmap(heart, 50 +heart.getWidth()*i, 50, null);
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

        // Actualizar monedas
        for (int i = 0; i < monedas.size(); i++) {
            Moneda moneda = monedas.get(i);
            moneda.actualizar();

            // Si la moneda colisiona con el jugador, se recoge
            if (moneda.obtenerHitbox().intersect(jugador.getHitbox())) {
                monedasPartida++; // Se suma la moneda a SharedPreferences
                monedas.remove(i);
            } else if (moneda.posY > maxY) {
                // Si la moneda se sale de la pantalla, se elimina
                monedas.remove(i);
            }
        }

        //Si el jugador se ha quedado sin vidas, pierde
        if(jugador.vidas<=0 && jugador.activo){
            jugador.spriteEstado=3; //Coche roto
            jugador.velX=0; //Paramos el coche en horizontal
            jugador.velY=10;
            jugador.activo=false; //Desactivamos el jugador
            soundPool.stop(engineSoundId); //Paramos el sonido de motor
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



    private void actualizarMonedasGanadas() {
        int monedasTotales = context.getSharedPreferences("DatosJuego", MODE_PRIVATE)
                .getInt("monedas", 0);
        monedasTotales += monedasPartida;
        context.getSharedPreferences("DatosJuego", MODE_PRIVATE)
                .edit()
                .putInt("monedas", monedasTotales)
                .apply();
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
                    if (x < jugador.posX) { // Toque en el primer tercio
                        jugador.velX = -jugador.VELOCIDADX;
                    } else if (x>jugador.posX+jugador.spriteWidth){ // Toque en el tercer tercio
                        jugador.velX = jugador.VELOCIDADX;
                    }
                    if(y<jugador.posY){
                        jugador.velY=-jugador.VELOCIDADY;
                    }
                    else if(y>jugador.posY+jugador.spriteHeight){
                        jugador.velY=jugador.VELOCIDADY;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP: // un dedo levanta el toque pero hay otros tocando
                case MotionEvent.ACTION_UP: //Ultimo dedo levanta el toque
                    jugador.velX = 0; // Dejar de girar el coche cuando se levanta el toque
                    jugador.velY=0;
                    break;
            }
        }
        return true;
    }
    public void terminarPartida(){
        soundPool.stop(engineSoundId);
        bucleJuego.fin();
        context.finish();
    }
}