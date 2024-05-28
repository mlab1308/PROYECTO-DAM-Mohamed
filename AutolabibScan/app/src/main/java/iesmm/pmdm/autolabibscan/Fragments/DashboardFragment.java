package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import iesmm.pmdm.autolabibscan.R;
import iesmm.pmdm.autolabibscan.Utils.OCRProcessor;
import iesmm.pmdm.autolabibscan.Utils.TessDataManager;

public class DashboardFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Uri imageUri;
    private TextView txtMatriculaleida;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Referencia elementos del layout
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        imageView = view.findViewById(R.id.imageView);
        txtMatriculaleida = view.findViewById(R.id.txtMatricula);

        // Copia los datos de Tesseract al directorio de archivos de la aplicación
        TessDataManager.copyTessDataFiles(getContext());

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);

                // Llamada a la función para leer la matrícula
                OCRProcessor ocrProcessor = new OCRProcessor(getContext());
                String recognizedText = ocrProcessor.getTextFromBitmap(bitmap);
                ocrProcessor.close();

                // Muestra el texto reconocido
                Toast.makeText(getContext(), recognizedText, Toast.LENGTH_LONG).show();
                txtMatriculaleida.setText(recognizedText);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
