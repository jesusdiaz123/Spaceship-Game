/*------------------------------------------------------------------------------------------
:*                         TECNOLOGICO NACIONAL DE MEXICO
:*                       INSTITUTO TECNOLOGICO DE LA LAGUNA
:*                     INGENIERIA EN SISTEMAS COMPUTACIONALES
:*                             DESARROLLO EN ANDROID "A"
:*
:*                   SEMESTRE: AGO-DIC/2021    HORA: 10-11 HRS
:*
:*
:*
:*  Archivo     : U4JuegoAsteroidesApp
:*  Autor       : Jesus Alberto Diaz de Leon     17130778
:*  Fecha       : 23/11/2021
:*  Compilador  : Android Studio Artic Fox 2020.3.1 + JDK 11
:*  Descripci�n : El proyecto es un juego pequeno de unos asteroides y una nave
:*  Ultima modif:
:*  Fecha       Modific�             Motivo
:*==========================================================================================
:*  23/11/2021 Jesus      Se completo el proyecto
:*  10/12/2021 Jesus      Se implemento el acelerometro en el moviento de la nave
:*------------------------------------------------------------------------------------------*/
package mx.edu.itl.c17130778.u4juegoasteroidesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void btnJuegoDeClick (View v){
        Intent intent = new Intent(this, JuegoActivity.class);
        startActivity(intent);
    }
    public void btnAcercaDeClick (View v){
        Intent intent = new Intent(this, AcercaDeActivity.class);
        startActivity(intent);
    }
}