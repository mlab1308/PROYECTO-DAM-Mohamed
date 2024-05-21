package iesmm.pmdm.autolabibscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

import iesmm.pmdm.autolabibscan.Activities.loginActivity;

public class welcomeActivity extends AppCompatActivity {
    //Variables
    private static int SPLASH_DURACION = 3000;
    private Animation topAnim, bottomAnim;
    private ImageView imgLogo;
    private TextView txtTitulo, txtAutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Muestra la pantalla de bienvenida en vista completa alijminando la barra superior
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        FirebaseApp.initializeApp(this);

        //Referencia elementos del layout
        imgLogo=findViewById(R.id.imgLogo);
        txtTitulo=findViewById(R.id.txtTitulo);
        txtAutor=findViewById(R.id.txtAutor);


        //Apicar animaciones a los elemntos del layout
        imgLogo.setAnimation(topAnim);
        txtAutor.setAnimation(bottomAnim);
        txtTitulo.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(welcomeActivity.this, loginActivity.class);
                startActivity(intent); // Iniciar la actividad de inicio de sesión
                finish(); // Finalizar la actividad de bienvenida
            }
        },SPLASH_DURACION);

    }
}