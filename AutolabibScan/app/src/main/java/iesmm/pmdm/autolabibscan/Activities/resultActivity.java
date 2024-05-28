package iesmm.pmdm.autolabibscan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import iesmm.pmdm.autolabibscan.R;

public class resultActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();
    }


    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            // Obtener el rol del usuario de SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
            String userRole = preferences.getString("user_role", "");

            if (userRole.equals("admin")) {
                if (id == R.id.navigation_home) {
                    // Ya estamos en home, no hacer nada
                    //return true;
                } else if (id == R.id.navigation_favorites) {
                    intent = new Intent(this, favoritesActivity.class);
                } else if (id == R.id.navigation_profile) {
                    intent = new Intent(this, profileActivity.class);
                } else if (id == R.id.navigation_access_list) {
                    intent = new Intent(this, accessListActivity.class);
                }
            } else if (userRole.equals("user")) {
                if (id == R.id.navigation_home) {
                    // Ya estamos en home, no hacer nada
                    //return true;
                } else if (id == R.id.navigation_favorites) {
                    intent = new Intent(this, favoritesActivity.class);
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