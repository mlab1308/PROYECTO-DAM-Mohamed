package iesmm.pmdm.autolabibscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import iesmm.pmdm.autolabibscan.Activities.AdminDashboardActivity;
import iesmm.pmdm.autolabibscan.Activities.UserDashboardActivity;
import iesmm.pmdm.autolabibscan.Activities.loginActivity;
import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.Models.User;

public class welcomeActivity extends AppCompatActivity {
    // Variables
    private static int SPLASH_DURACION = 3000;
    private Animation topAnim, bottomAnim;
    private ImageView imgLogo;
    private TextView txtTitulo, txtAutor;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Muestra la pantalla de bienvenida en vista completa al eliminar la barra superior
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        // Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        FirebaseApp.initializeApp(this);

        // Referencia elementos del layout
        imgLogo = findViewById(R.id.imgLogo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtAutor = findViewById(R.id.txtAutor);

        // Aplicar animaciones a los elementos del layout
        imgLogo.setAnimation(topAnim);
        txtAutor.setAnimation(bottomAnim);
        txtTitulo.setAnimation(bottomAnim);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Registrar el acceso
                    logAccess(currentUser.getEmail());
                    fetchUserRoleAndRedirect(currentUser.getUid(), currentUser.getEmail());
                } else {
                    redirectToLogin();
                }
            }
        }, SPLASH_DURACION);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(welcomeActivity.this, loginActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchUserRoleAndRedirect(String userId, String emailUser) {
        try {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if (role != null) {
                            redirectToDashboard(role);
                        } else {
                            userRef.child("role").setValue("user");
                            redirectToDashboard("user");
                        }
                    } else {
                        userRef.setValue(new User("", emailUser, "user"));
                        redirectToDashboard("user");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("welcomeActivity", "Error al obtener el rol del usuario", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e("welcomeActivity", "Error in fetchUserRoleAndRedirect: ", e);
        }
    }

    private void redirectToDashboard(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, UserDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void logAccess(String email) {
        try {
            DatabaseReference accessRef = FirebaseDatabase.getInstance().getReference().child("access_logs");
            String key = accessRef.push().getKey();
            AccessItem accessItem = new AccessItem(email, new Date());
            accessRef.child(key).setValue(accessItem);
        } catch (Exception e) {
            Log.e("loginActivity", "Error in logAccess: ", e);
        }
    }
}
