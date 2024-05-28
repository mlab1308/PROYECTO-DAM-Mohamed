package iesmm.pmdm.autolabibscan.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import iesmm.pmdm.autolabibscan.R;

public class profileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private Button btnEditProfile;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Referencia elemntos del layout
        imgProfile = findViewById(R.id.imgProfile);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        // Puedes configurar la información del perfil aquí
        txtProfileName.setText("Nombre del Usuario");
        txtProfileEmail.setText("usuario@correo.com");

        // Agrega funcionalidad al botón de editar perfil
        btnEditProfile.setOnClickListener(v -> {
            // Aquí podrías abrir una nueva actividad para editar el perfil
        });
    }

    private void setupBottomNavigation() {
        // Obtener el rol del usuario de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userRole = preferences.getString("user_role", "");

        // Cargar el menú correspondiente según el rol
        if (userRole.equals("admin")) {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_admin);
        } else{
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;


            if (userRole.equals("admin")) {
                if (id == R.id.navigation_home) {
                    intent = new Intent(this, dashboardActivity.class);
                } else if (id == R.id.navigation_favorites) {
                    intent = new Intent(this, favoritesActivity.class);
                } else if (id == R.id.navigation_profile) {

                } else if (id == R.id.navigation_access_list) {
                    intent = new Intent(this, accessListActivity.class);
                }
            } else {
                if (id == R.id.navigation_home) {
                    intent = new Intent(this, dashboardActivity.class);
                } else if (id == R.id.navigation_favorites) {
                    intent = new Intent(this, favoritesActivity.class);
                }
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

}
