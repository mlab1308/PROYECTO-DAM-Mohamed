package iesmm.pmdm.autolabibscan.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

import iesmm.pmdm.autolabibscan.Adapters.FavoritesAdapter;
import iesmm.pmdm.autolabibscan.R;

public class favoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavorites;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        //Referencia elemntos del layout
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupRecyclerView();
        setupBottomNavigation();
    }


    private void setupRecyclerView() {
        List<String> favoritePlates = Arrays.asList("1234-FJK", "5678-ABC", "9012-XYZ"); // Ejemplo de datos
        FavoritesAdapter adapter = new FavoritesAdapter(favoritePlates);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFavorites.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        // Obtener el rol del usuario de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userRole = preferences.getString("user_role", "");

        // Cargar el menú correspondiente según el rol
        if (userRole.equals("admin")) {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_admin);
        } else  {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;


            if (userRole.equals("admin")) {
                if (id == R.id.navigation_home) {
                    intent = new Intent(this, dashboardActivity.class);
                } else if (id == R.id.navigation_profile) {
                    intent = new Intent(this, profileActivity.class);
                } else if (id == R.id.navigation_access_list) {
                    intent = new Intent(this, accessListActivity.class);
                }
            } else  {
                if (id == R.id.navigation_home) {
                    intent = new Intent(this, dashboardActivity.class);
                } else if (id == R.id.navigation_profile) {
                    intent = new Intent(this, profileActivity.class);
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
