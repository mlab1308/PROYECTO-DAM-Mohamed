package iesmm.pmdm.autolabibscan.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.Fragments.AccessListFragment;
import iesmm.pmdm.autolabibscan.Fragments.DashboardFragment;
import iesmm.pmdm.autolabibscan.Fragments.FavoritesFragment;
import iesmm.pmdm.autolabibscan.Fragments.ProfileFragment;
import iesmm.pmdm.autolabibscan.R;

public class dashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Inicialización de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Referencia elementos del layout
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (currentUser != null) {
            // Obtener la referencia del usuario en la base de datos
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            fetchUserRoleAndSetupBottomNavigation();
        } else {
            // Manejar el caso donde currentUser es null
            Log.e("dashboardActivity", "Rol de usuario no autenticado");
        }

        // Cargar DashboardFragment por defecto
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
    }
    /**
     * Método para obtener el rol del usuario desde Firebase y configurar la navegación inferior.
     */
    private void fetchUserRoleAndSetupBottomNavigation() {
        // Añade un listener de evento único para obtener los datos del usuario desde Firebase Realtime Database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Verifica si los datos del usuario existen en la base de datos
                if (snapshot.exists()) {
                    // Obtiene el valor del rol del usuario desde el snapshot
                    String userRole = snapshot.child("role").getValue(String.class);

                    // Configura el BottomNavigationView basado en el rol del usuario
                    setupBottomNavigation(userRole);
                } else {
                    // Maneja el caso donde los datos del usuario no existen en la base de datos
                    Log.e("dashboardActivity", "Rol de usuario no encontrado");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Maneja los errores en caso de que la consulta a la base de datos falle
                Log.e("dashboardActivity", "Error al obtener el rol del usuario");
            }
        });
    }


    // Configura el BottomNavigationView
    private void setupBottomNavigation(String userRole) {
        // Cargar el menú correspondiente según el rol
        if ("admin".equals(userRole)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_admin);
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.navigation_home) {
                fragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.navigation_favorites) {
                fragment = new FavoritesFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.navigation_access_list) {
                fragment = new AccessListFragment();
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
