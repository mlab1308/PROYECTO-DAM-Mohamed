package iesmm.pmdm.autolabibscan.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import iesmm.pmdm.autolabibscan.Fragments.DashboardFragment;
import iesmm.pmdm.autolabibscan.Fragments.FavoritesFragment;
import iesmm.pmdm.autolabibscan.Fragments.ProfileFragment;
import iesmm.pmdm.autolabibscan.R;

public class UserDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Comprobar si el usuario está autenticado
        if (mAuth.getCurrentUser() == null) {
            redirigirAlLogin();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        configurarNavegaciónInferior();

        // Cargar DashboardFragment por defecto
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
    }

    // Método para redirigir al usuario a la pantalla de inicio de sesión
    private void redirigirAlLogin() {
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
        finish();
    }

    // Método para configurar la navegación inferior
    private void configurarNavegaciónInferior() {
        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.navigation_home) {
                fragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.navigation_favorites) {
                fragment = new FavoritesFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }

            return false;
        });
    }
}
