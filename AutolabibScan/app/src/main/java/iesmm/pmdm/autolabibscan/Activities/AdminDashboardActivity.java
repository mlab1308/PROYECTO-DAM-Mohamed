package iesmm.pmdm.autolabibscan.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.Fragments.AccessListFragment;
import iesmm.pmdm.autolabibscan.Fragments.CreateVehicleFragment;
import iesmm.pmdm.autolabibscan.Fragments.DashboardFragment;
import iesmm.pmdm.autolabibscan.Fragments.FavoritesFragment;
import iesmm.pmdm.autolabibscan.Fragments.ProfileFragment;
import iesmm.pmdm.autolabibscan.Models.User;
import iesmm.pmdm.autolabibscan.R;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private TextView textViewAdminName, textViewAdminEmail;
    private ImageView imageViewAdminProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Configurar la barra de herramientas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Inflar la vista del encabezado y obtener referencias a las vistas
        View headerView = navigationView.getHeaderView(0);
        textViewAdminName = headerView.findViewById(R.id.textViewAdminName);
        textViewAdminEmail = headerView.findViewById(R.id.textViewAdminEmail);
        imageViewAdminProfile = headerView.findViewById(R.id.imageViewAdminProfile);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            loadAdminData();
        } else {
            // Manejar el caso donde currentUser es null
        }

        // Cargar el fragmento por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.admin_fragment_container, new DashboardFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new DashboardFragment();
        } else if (item.getItemId() == R.id.nav_favorites) {
            selectedFragment = new FavoritesFragment();
        } else if (item.getItemId() == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (item.getItemId() == R.id.nav_access_list) {
            selectedFragment = new AccessListFragment();
        } else if (item.getItemId() == R.id.nav_create_vehicle) {
            selectedFragment = new CreateVehicleFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método para cargar los datos del administrador desde Firebase
    private void loadAdminData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        textViewAdminName.setText(user.getName());
                        textViewAdminEmail.setText(user.getEmail());

                        // Cargar la imagen de perfil si existe, de lo contrario, cargar la imagen por defecto
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            Glide.with(AdminDashboardActivity.this)
                                    .load(user.getProfileImageUrl())
                                    .transform(new CircleCrop())
                                    .into(imageViewAdminProfile);
                        } else {
                            Glide.with(AdminDashboardActivity.this)
                                    .load(R.drawable.ic_profile_default)
                                    .transform(new CircleCrop())
                                    .into(imageViewAdminProfile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load admin data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
