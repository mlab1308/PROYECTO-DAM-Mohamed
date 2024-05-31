package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import iesmm.pmdm.autolabibscan.R;
import iesmm.pmdm.autolabibscan.Utils.OCRProcessor;
import iesmm.pmdm.autolabibscan.Utils.TessDataManager;

public class DashboardFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private ImageView imageViewProcessed;
    private Uri imageUri;
    private TextView txtMatriculaleida;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Referencia elementos del layout
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        imageView = view.findViewById(R.id.imageView);
        imageViewProcessed = view.findViewById(R.id.imageViewProcessed);
        txtMatriculaleida = view.findViewById(R.id.txtMatricula);

        // Copia los datos de Tesseract al directorio de archivos de la aplicación
        TessDataManager.initTessData(getContext());

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

                // Convertir Bitmap a Mat
                Mat src = new Mat();
                Utils.bitmapToMat(bitmap, src);

                // Preprocesar la imagen
                Mat processedImage = preProcessImage(src);

                // Extraer la región de la matrícula
                Mat licensePlate = extractLicensePlate(processedImage);

                // Convertir Mat a Bitmap
                Bitmap processedBitmap = Bitmap.createBitmap(licensePlate.cols(), licensePlate.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(licensePlate, processedBitmap);

                // Mostrar la imagen procesada
                imageViewProcessed.setImageBitmap(processedBitmap);

                // Realizar OCR en la imagen procesada
                OCRProcessor ocrProcessor = new OCRProcessor(getContext());
                String text = ocrProcessor.getOCRResult(processedBitmap);

                txtMatriculaleida.setText("Matricula leida: "+text);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Mat preProcessImage(Mat src) {
        Mat gray = new Mat();
        Mat blur = new Mat();
        Mat edged = new Mat();

        // Convertir a escala de grises
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Aplicar filtro bilateral para reducir el ruido
        Imgproc.bilateralFilter(gray, blur, 11, 17, 17);

        // Detectar bordes
        Imgproc.Canny(blur, edged, 30, 200);

        return edged;
    }

    private Mat extractLicensePlate(Mat src) {
        Mat contoursImage = src.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // Encontrar contornos
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Rect largestRect = null;

        // Iterar a través de los contornos y encontrar uno que coincida con el tamaño de una matrícula
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (isLicensePlateShape(rect)) {
                if (largestRect == null || rect.area() > largestRect.area()) {
                    largestRect = rect;
                }
            }
        }

        if (largestRect != null) {
            return new Mat(contoursImage, largestRect);
        }

        return contoursImage;
    }

    private boolean isLicensePlateShape(Rect rect) {
        // Define los criterios para identificar la forma de una matrícula
        float aspectRatio = (float) rect.width / rect.height;
        return aspectRatio > 2 && aspectRatio < 6 && rect.height > 20 && rect.width > 60;
    }
}
