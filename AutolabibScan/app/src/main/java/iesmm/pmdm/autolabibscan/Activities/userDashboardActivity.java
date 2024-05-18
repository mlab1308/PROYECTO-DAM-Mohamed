package iesmm.pmdm.autolabibscan.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import iesmm.pmdm.autolabibscan.R;
import iesmm.pmdm.autolabibscan.Utils.OCRProcessor;
import iesmm.pmdm.autolabibscan.Utils.TessDataManager;

public class userDashboardActivity extends AppCompatActivity {

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
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        txtMatriculaleida=findViewById(R.id.txtMatricula);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();
        btnSelectImage.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.navigation_home) {
                // Ya estamos en home, no hacer nada
                //return true;
            } else if (id == R.id.navigation_favorites) {
                intent = new Intent(this, favoritesActivity.class);
            } else if (id == R.id.navigation_profile) {
                intent = new Intent(this, profileActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);


                // Aquí puedes llamar a la función para leer la matrícula
                OCRProcessor ocrProcessor = new OCRProcessor(this);
                String recognizedText = ocrProcessor.getTextFromBitmap(bitmap);
                ocrProcessor.close();

                // Muestra el texto reconocido (puedes mostrarlo en un TextView o en un Toast)
                Toast.makeText(this, recognizedText, Toast.LENGTH_LONG).show();
                txtMatriculaleida.setText(recognizedText);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
