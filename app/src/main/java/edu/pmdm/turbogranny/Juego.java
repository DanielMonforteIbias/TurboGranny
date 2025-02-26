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
    private int engineSoundId, coinSoundId, healSoundId, accidentSoundId; //Necesario guardar todos los ids para que el soundpool funcione
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
    private int monedasPartida = 0;

    private boolean juegoPausado = false;
    private ArrayList<Vida> vidas = new ArrayList<>(); // Lista de vidas
    private Bitmap heartSpritesheet;
    private Handler manejadorVidas = new Handler();
    private Runnable generarVida = new Runnable() {
        @Override
        public void run() {
            crearVida();
            programarSiguienteVida();
        }
    };

    public Juego(AppCompatActivity context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        this.context = context;
        cargarSoundpool();
    }


    public void pausarJuego() {
        juegoPausado = true;
        if (bucleJuego != null) {
            bucleJuego.fin(); // Detenemos el bucle del juego
        }
        if (soundPool != null) {
            soundPool.autoPause(); // Pausamos todos los sonidos
        }
        handler.removeCallbacks(generarEnemigo); // Detenemos la generación de enemigos
        manejadorMonedas.removeCallbacks(generarMoneda); // Detener la generación de monedas
    }

    public void reanudarJuego() {
        juegoPausado = false;
        if (bucleJuego == null || !bucleJuego.JuegoEnEjecucion) {
            bucleJuego = new BucleJuego(getHolder(), this);
            bucleJuego.start(); // Reanudar el bucle del juego
        }
        if (soundPool != null) {
            soundPool.autoResume(); // Reanudar todos los sonidos
        }
        generacionEnemigos(); // Reanudar la generación de enemigos
        programarSiguienteMoneda(); // Reanudar la generación de monedas
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

        if(jugador==null){
            //CREAMOS Y POSICIONAMOS JUGADOR
            jugador=new Jugador(this,BitmapFactory.decodeResource(getResources(), carId));
            jugador.posY = maxY - jugador.spriteHeight;
            jugador.posX = maxX / 2 - jugador.spriteWidth / 2;
        }


        heart =BitmapFactory.decodeResource(getResources(),R.drawable.redheart);
        //heart=Bitmap.createScaledBitmap(heart, (int)(heart.getWidth()*1.3), (int)(heart.getHeight()*1.3), true); //Hacemos el sprite un 1.3 mas grande

        // Cargar el sprite del corazón
        heartSpritesheet = BitmapFactory.decodeResource(getResources(), R.drawable.redheartspritesheet);

        // Generar la primera vida
        programarSiguienteVida();

        //GENERAR ENEMIGOS
        generacionEnemigos();
        programarSiguienteMoneda();

        //Creamos el Gameloop
        bucleJuego = new BucleJuego(getHolder(), this);
        setFocusable(true);
        setOnTouchListener(this);
        bucleJuego.start();
    }

    private void cargarSoundpool(){
        //Sonido de motor
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0); //10 sonidos a la vez
        engineSoundId = soundPool.load(context, R.raw.engine2, 1);
        coinSoundId = soundPool.load(context, R.raw.coin, 1);
        healSoundId = soundPool.load(context, R.raw.heal, 1);
        accidentSoundId=soundPool.load(context,R.raw.carbreak,1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                //Importante el if para reproducir solo el motor 1 vez
                if(sampleId==engineSoundId)soundPool.play(engineSoundId, 1, 1, 0, -1, 1);// Reproducir en bucle sin cortes.
            }
        });
        //Sonidos de moneda y vida
        soundPool.load(context,R.raw.coin,1);
        soundPool.load(context,R.raw.heal,1);
    }

    private void crearVida() {
        // Posición X aleatoria dentro de la pantalla
        int x = random.nextInt(maxX - (heartSpritesheet.getWidth() / 6)); // Ajustar según el ancho del fotograma
        // La vida inicia justo arriba de la pantalla
        int y = -heartSpritesheet.getHeight();

        Vida vida = new Vida(this, heartSpritesheet, x, y);
        vidas.add(vida);
    }

    private void programarSiguienteVida() {
        // Intervalo aleatorio entre 30 y 50 segundos
        long delay = 30000 + (long) (random.nextDouble() * 20000); // 30,000 ms = 30 segundos, 20,000 ms = 20 segundos adicionales
        manejadorVidas.postDelayed(generarVida, delay);
    }

    private void crearMoneda() {
        // Posición X aleatoria dentro de la pantalla
        int x = random.nextInt(maxX - 150);
        // La moneda inicia justo arriba de la pantalla
        int y = -100;

        Moneda moneda = new Moneda(this, R.drawable.coin, x, y);
        monedas.add(moneda);
    }




    private void generacionEnemigos() {
        if (!juegoPausado) {
            double delay = 1000.0 + (random.nextDouble() * 2000.0);
            handler.postDelayed(generarEnemigo, (long) delay);
        }
    }

    private void programarSiguienteMoneda() {
        if (!juegoPausado) {
            double delay = 1500.0 + (random.nextDouble() * 2000.0);
            manejadorMonedas.postDelayed(generarMoneda, (long) delay);
        }
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

            // Dibujar las vidas
            for (int i = 0; i < vidas.size(); i++) {
                vidas.get(i).render(canvas, paint);
            }

            //La interfaz se pinta lo ultimo para que esté por encima de lo demas
            //Pintamos las vidas
            for (int i = 0; i < jugador.vidas; i++) {
                canvas.drawBitmap(heart, 50 +heart.getWidth()*i, 50, null);
            }
        }
    }

    public void update() {
        if (juegoPausado) {
            return; // No actualizar nada si el juego está pausado
        }
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
        //Actualizamos las explosiones
        for(int i=0;i<explosiones.size();i++){
            explosiones.get(i).update();
            if(explosiones.get(i).finished()) explosiones.remove(i);
        }

        //Actualizar monedas
        for (int i = 0; i < monedas.size(); i++) {
            Moneda moneda = monedas.get(i);
            moneda.actualizar();

            //Si la moneda colisiona con el jugador, se recoge
            if (moneda.obtenerHitbox().intersect(jugador.getHitbox())) {
                monedasPartida++; //Se suma la moneda a SharedPreferences
                playSound(coinSoundId);
                monedas.remove(i);
            } else if (moneda.posY > maxY) {
                //Si la moneda se sale de la pantalla, se elimina
                monedas.remove(i);
            }
        }
        //Actualizar vidas
        for (int i = 0; i < vidas.size(); i++) {
            Vida vida = vidas.get(i);
            vida.update();

            //Verificar colisión con el jugador
            if (vida.isActiva() && jugador.activo && vida.getHitbox().intersect(jugador.getHitbox())) { //Se coge solo si la vida y el jugador estan activos, para que no se coja si el jugador ha muerto
                jugador.vidas++; //Aumentar las vidas del jugador
                playSound(healSoundId);
                vidas.remove(i); //Eliminar la vida recolectada
            } else if (!vida.isActiva()) {
                vidas.remove(i); //Eliminar la vida si sale de la pantalla
            }
        }


        //Si el jugador se ha quedado sin vidas, pierde
        if(jugador.vidas<=0 && jugador.activo){
            jugador.spriteEstado=3; //Coche roto
            jugador.velX=0; //Paramos el coche en horizontal
            jugador.velY=10;
            jugador.activo=false; //Desactivamos el jugador
            soundPool.stop(engineSoundId); //Paramos el sonido de motor
            playSound(accidentSoundId);
            explosiones.add(new Explosion(this,BitmapFactory.decodeResource(getResources(),R.drawable.explosion),jugador.posX,jugador.posY));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bucleJuego.fin(); //Paramos el bucle del juego
                    actualizarMonedasGanadas();
                    context.finish();
                }
            }, 1500); //Se cerrara tras 1 segundo y medio

        }
    }

    private void playSound(int soundId){
        soundPool.play(soundId,1,1,0,0,1);
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
                        jugador.velY=jugador.VELOCIDADY*3; //Frenamos mas que aceleramos
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


    public void terminarPartida() {
        if (bucleJuego != null) {
            bucleJuego.fin(); // Detener el bucle del juego
        }
        if (soundPool != null) {
            soundPool.stop(engineSoundId); // Detener el sonido del motor
        }
        if (context != null) {
            context.finish(); // Cerrar la actividad
        }
    }
}