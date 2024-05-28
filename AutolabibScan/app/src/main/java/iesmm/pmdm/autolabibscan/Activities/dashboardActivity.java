package iesmm.pmdm.autolabibscan.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import iesmm.pmdm.autolabibscan.Fragments.AccessListFragment;
import iesmm.pmdm.autolabibscan.Fragments.DashboardFragment;
import iesmm.pmdm.autolabibscan.Fragments.FavoritesFragment;
import iesmm.pmdm.autolabibscan.Fragments.ProfileFragment;
import iesmm.pmdm.autolabibscan.R;
import iesmm.pmdm.autolabibscan.Utils.OCRProcessor;
import iesmm.pmdm.autolabibscan.Utils.TessDataManager;

public class dashboardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Uri imageUri;
    private TextView txtMatriculaleida;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        // Copia los datos de Tesseract al directorio de archivos de la aplicación
        TessDataManager.copyTessDataFiles(this);
        //Referencia elementos del layout
        /*Button btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        txtMatriculaleida=findViewById(R.id.txtMatricula);*/
        bottomNavigationView =  findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        //btnSelectImage.setOnClickListener(v -> openImageChooser());
    }
    // Configura el BottomNavigationView
    private void setupBottomNavigation() {
        // Obtener el rol del usuario de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userRole = preferences.getString("user_role", "");

        // Cargar el menú correspondiente según el rol
        if (userRole.equals("admin")) {
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
            } else {

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


    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);


                //llamada a la función para leer la matrícula
                OCRProcessor ocrProcessor = new OCRProcessor(this);
                String recognizedText = ocrProcessor.getTextFromBitmap(bitmap);
                ocrProcessor.close();

                // Muestra el texto reconocido
                Toast.makeText(this, recognizedText, Toast.LENGTH_LONG).show();
                txtMatriculaleida.setText(recognizedText);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
