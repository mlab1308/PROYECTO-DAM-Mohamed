package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import iesmm.pmdm.autolabibscan.Models.ApiResponse;
import iesmm.pmdm.autolabibscan.R;
import iesmm.pmdm.autolabibscan.Remote.ApiClient;
import iesmm.pmdm.autolabibscan.Remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DashboardFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Uri imageUri;
    private TextInputLayout textInputLayout;
    private TextInputEditText editTextBastidor;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Referencia elementos del layout
        textInputLayout = view.findViewById(R.id.textInputLayout);
        editTextBastidor = view.findViewById(R.id.editTextBastidor);
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);

        // Acción al pulsar el icono de subir imagen
        textInputLayout.setEndIconOnClickListener(v -> openImageChooser());

        // Configurar el OnEditorActionListener para detectar la tecla "Enter"
        editTextBastidor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Aquí manejas el evento cuando se presiona "Enter"
                    String matricula = editTextBastidor.getText().toString();
                    handleMatriculaInput(matricula);
                    return true;
                }
                return false;
            }
        });

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
                uploadImageToApi(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(getView(), "Error al cargar la imagen", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImageToApi(Bitmap bitmap) {
        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Convertir bitmap a archivo
        File file = new File(getContext().getCacheDir(), "image.jpg");
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Preparar la llamada a la API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<ApiResponse> call = apiService.uploadImage(body);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Ocultar ProgressBar
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getError() == null) {
                        handleMatriculaInput(apiResponse.getPlate());
                    } else {
                        Snackbar.make(getView(), "Error: " + apiResponse.getError(), Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(getView(), "Error al procesar la imagen", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Ocultar ProgressBar
                progressBar.setVisibility(View.GONE);

                Snackbar.make(getView(), "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void handleMatriculaInput(String matricula) {
        matricula = matricula.replace(" ", ""); // Elimina cualquier espacio en blanco
        matricula = matricula.toUpperCase();
        if (isValidMatricula(matricula)) {
            // Realiza la consulta a la base de datos con la matrícula
            Toast.makeText(getActivity(), matricula, Toast.LENGTH_SHORT).show();

            consultaDB(matricula);
        } else {
            // Muestra un mensaje de error si el formato no es válido
            Toast.makeText(getActivity(), getString(R.string.matricula_invalida), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidMatricula(String matricula) {
        // Patrón para "0000ABC"
        String pattern = "^[0-9]{4}[A-Z]{3}$";
        return matricula.toUpperCase().matches(pattern);
    }private void consultaDB(String matricula) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cars");

        databaseReference.orderByChild("plateText").equalTo(matricula.toUpperCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot vehicleSnapshot : dataSnapshot.getChildren()) {
                        String carBrand = vehicleSnapshot.child("brand").getValue(String.class);
                        String ownersText = vehicleSnapshot.child("owners").getValue(String.class);
                        String powerText = vehicleSnapshot.child("power").getValue(String.class);
                        String fuelText = vehicleSnapshot.child("fuel").getValue(String.class);
                        String bastidor = vehicleSnapshot.child("info").child("Bastidor").getValue(String.class);
                        String emissionNorm = vehicleSnapshot.child("info").child("EmissionNorm").getValue(String.class);
                        String fuelType = vehicleSnapshot.child("info").child("FuelType").getValue(String.class);
                        String registeringAuthority = vehicleSnapshot.child("info").child("RegisteringAuthority").getValue(String.class);
                        String vehicleAge = vehicleSnapshot.child("info").child("VehicleAge").getValue(String.class);
                        String vehicleClass = vehicleSnapshot.child("info").child("VehicleClass").getValue(String.class);
                        String vehicleStatus = vehicleSnapshot.child("info").child("VehicleStatus").getValue(String.class);

                        // Crear el Bundle con los datos del vehículo
                        Bundle bundle = new Bundle();
                        bundle.putString("carBrand", carBrand);
                        bundle.putString("plateText", matricula.toUpperCase());
                        bundle.putString("ownersText", ownersText);
                        bundle.putString("powerText", powerText);
                        bundle.putString("fuelText", fuelText);
                        bundle.putString("Bastidor", bastidor);
                        bundle.putString("RegisteringAuthority", registeringAuthority);
                        bundle.putString("VehicleClass", vehicleClass);
                        bundle.putString("FuelType", fuelType);
                        bundle.putString("EmissionNorm", emissionNorm);
                        bundle.putString("VehicleAge", vehicleAge);
                        bundle.putString("VehicleStatus", vehicleStatus);

                        // Crear el ResultFragment y pasarle los datos
                        ResultFragment resultFragment = new ResultFragment();
                        resultFragment.setArguments(bundle);

                        // Navegar al ResultFragment
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, resultFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.no_data_found, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), R.string.db_query_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
