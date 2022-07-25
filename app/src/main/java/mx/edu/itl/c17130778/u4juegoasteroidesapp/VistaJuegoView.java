package mx.edu.itl.c17130778.u4juegoasteroidesapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Vector;

/**
 * Created by LGV on 25/07/2015.
 */
public class VistaJuegoView extends View implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private int x ;
    private int y;
    private int radio = 50;
    private int ancho;
    private int alto;

    private Vector<Grafico> asteroides;
    private Drawable        drawableAsteroide [] = new Drawable [ 3 ];
    private int             numAsteroides = 5;
    private int             numFragmentos = 3;
    private Grafico         nave;
    private int             giroNave        = 0;
    private float           aceleracionNave = 2.5f ;
    private static int      PERIODO_PROCESO = 20;
    private long            ultimoProceso   = 0;
    private boolean         hayValorInicial = false;
    private float           valorInicial;

    public static final int   PASO_GIRO_NAVE        = 5;
    public static final float PASO_ACELERACION_NAVE = 0.5f;

    // ******* MISIL  *******
    private Grafico         misil;
    private static int      PASO_VELOCIDAD_MISIL = 12;
    private boolean         misilActivo;
    private int             distanciaMisil;

    // ******* PARA PANTALLA TACTIL *****
    private float           mX = 0, mY = 0;
    private boolean         disparo = false;

    private VistaJuegoThread vistaJuegoThread;
    private boolean corriendo;

    private MediaPlayer mplayAudioDisparo;
    //----------------------------------------------------------------------------------------------
    public void iniciar(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void detener(){
        sensorManager.unregisterListener(this, sensor);
    }

    public VistaJuegoView ( Context context, AttributeSet attrs ) {
        super(context, attrs);

        Drawable drawableNave, drawableMisil;

        drawableNave = context.getResources().getDrawable(R.drawable.nave);
        drawableMisil = context.getResources().getDrawable(R.drawable.misil1);
        drawableAsteroide [0] = context.getResources().getDrawable(R.drawable.asteroide1);
        drawableAsteroide [1] = context.getResources().getDrawable(R.drawable.asteroide2);
        drawableAsteroide [2] = context.getResources().getDrawable(R.drawable.asteroide3);


        nave = new Grafico(this, drawableNave);
        nave.setIncX(Math.random() * 4 - 2);
        nave.setIncY(Math.random() * 4 - 2);
        nave.setAngulo(0);
        nave.setRotacion(5);


        misil = new Grafico ( this, drawableMisil );

        asteroides = new Vector<Grafico>();
        for (int i = 0; i < numAsteroides; i++) {
            Grafico asteroide = new Grafico(this, drawableAsteroide [0] );
            asteroide.setIncY(Math.random() * 4 - 2);
            asteroide.setIncX(Math.random() * 4 - 2);
            asteroide.setAngulo((int) (Math.random() * 360));
            asteroide.setRotacion((int) (Math.random() * 8 - 4));
            asteroides.add(asteroide);
        }

        vistaJuegoThread = new VistaJuegoThread(this, PERIODO_PROCESO);
        vistaJuegoThread.start();
        corriendo = true;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onSizeChanged ( int w, int h, int oldw, int oldh ) {
        super.onSizeChanged(w, h, oldw, oldh);

        // lgv: conocer el ancho y alto en pixeles de VistaJuego ( ocupa toda la pantalla por tanto da la resolucion )
        Toast.makeText ( getContext(), getWidth() + "," + getHeight(), Toast.LENGTH_SHORT ).show ();

        nave.setPosX((w - nave.getAncho()) / 2);
        nave.setPosY((h - nave.getAncho()) / 2);

        for ( Grafico asteroide : asteroides ) {
            do {
                asteroide.setPosX(Math.random() * (w - asteroide.getAncho()));
                asteroide.setPosY(Math.random() * (h - asteroide.getAlto()));
            } while ( asteroide.distancia ( nave ) < ( w + h ) / 5 );
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onDraw ( Canvas canvas ) {
        super.onDraw(canvas);

        synchronized ( this ) {
            for (Grafico asteroide : asteroides) {
                asteroide.dibujarGrafico(canvas);
            }
            nave.dibujarGrafico(canvas);

            if (misilActivo)
                misil.dibujarGrafico(canvas);
        }
    }

    //----------------------------------------------------------------------------------------------

    protected void actualizarFisica () {

        long ahora = System.currentTimeMillis();

        double retardo = ( ahora - ultimoProceso ) / PERIODO_PROCESO;
        nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo ));
        double nIncX = x;
        double nIncY = y ;
            nave.setIncX ( nIncX );
            nave.setIncY ( nIncY );


        nave.incrementaPosNave();
        for ( int i = 0; i < asteroides.size(); i++ ){
            asteroides.get (i).incrementaPos();
        }

        ultimoProceso = ahora;

        if ( misilActivo ) {
            misil.incrementaPos();
            distanciaMisil--;
            if ( distanciaMisil < 0 ) {
                misilActivo = false;
            } else {
                for ( int i = 0; i < asteroides.size (); i++ ) {
                    if ( misil.verificaColision ( asteroides.elementAt ( i ) ) ) {
                        destruyeAsteroide ( i );
                    }
                }
            }
        }}

    //----------------------------------------------------------------------------------------------

    private void destruyeAsteroide ( int i ) {
        int tam;

        synchronized ( this ) {
            if (asteroides.get(i).getDrawable() != drawableAsteroide[2]) {
                if (asteroides.get(i).getDrawable() == drawableAsteroide[1]) {
                    tam = 2;
                } else {
                    tam = 1;
                }
                for (int n = 0; n < numFragmentos; n++) {
                    Grafico asteroide = new Grafico(this, drawableAsteroide[tam]);
                    asteroide.setPosX(asteroides.get(i).getPosX());
                    asteroide.setPosY(asteroides.get(i).getPosY());
                    asteroide.setIncX(Math.random() * 7 - 2 - tam);
                    asteroide.setIncY(Math.random() * 7 - 2 - tam);
                    asteroide.setAngulo((int) (Math.random() * 360));
                    asteroide.setRotacion((int) (Math.random() * 8 - 4));
                    asteroides.add(asteroide);
                }
            }

            asteroides.remove(i);
            misilActivo = false;
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event ) {
        super.onTouchEvent(event);
        float x1 = event.getX ();
        float y2 = event.getY ();

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN :
                disparo = true;
                break;
            case MotionEvent.ACTION_MOVE :
                float dx = Math.abs ( x1 - mX );
                float dy = Math.abs ( y2 - mY );
                if ( dy < 6 && dx > 6 ) {
                    giroNave = Math.round ( ( x1- mX ) / 2 );
                    disparo = false;
                } else if ( dx < 6 && dy > 6  ) {
                    aceleracionNave = Math.round ( ( mY - y2 ) / 25 );
                    disparo = false;
                }
                break;
            case MotionEvent.ACTION_UP :
                giroNave = 0;
                aceleracionNave = 0;
                if ( disparo ) {
                    activaMisil();
                }
                break;
        }
        mX = x1;
        mY = y2;
        return true;
    }

    //----------------------------------------------------------------------------------------------

    private void activaMisil () {
        misil.setPosX ( nave.getPosX() + nave.getAncho() / 2 - misil.getAncho () / 2 );
        misil.setPosY(nave.getPosY() + nave.getAlto() / 2 - misil.getAlto() / 2);
        misil.setAngulo(nave.getAngulo());
        misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
        misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
        distanciaMisil = (int) Math.min ( this.getWidth() / Math.abs ( misil.getIncX() ),
                                          this.getHeight() / Math.abs ( misil.getIncY () )
                                        ) - 2;
        misilActivo = true;

        //Reproducir Audio
        if(mplayAudioDisparo != null){
            mplayAudioDisparo.start();
        }
    }

    //----------------------------------------------------------------------------------------------

    public boolean isCorriendo() {
        return corriendo;
    }

    public void setCorriendo(boolean corriendo) {
        this.corriendo = corriendo;
    }

    //----------------------------------------------------------------------------------------------

    public VistaJuegoThread getVistaJuegoThread() {
        return vistaJuegoThread;
    }

    //----------------------------------------------------------------------------------------------

    public void setAudioDisparo(MediaPlayer mplayAudioDisparo) {
        this.mplayAudioDisparo = mplayAudioDisparo;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Calcular la nueva posicion x, y de la pelota en base a la lectura del acelerometro
        //en los ejes x, y
        x += sensorEvent.values[0] / 7 ;
        y += sensorEvent.values[1] / 7;


        //Invocar invalidate() de este View para invocar al metodo draw()
        invalidate ();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //----------------------------------------------------------------------------------------------
}
