package iesmm.pmdm.autolabibscan.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import iesmm.pmdm.autolabibscan.R;

public class CreateVehicleFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etBrand, etPlate, etOwners, etPower, etFuel, etBastidor, etEmissionNorm, etRegisteringAuthority, etManufacturingDate;
    private SwitchCompat switchVehicleStatus;
    private AppCompatButton btnCreateVehicle, btnSelectImage;
    private Uri imageUri;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_vehicle, container, false);

        etBrand = view.findViewById(R.id.et_brand);
        etPlate = view.findViewById(R.id.et_plate);
        etOwners = view.findViewById(R.id.et_owners);
        etPower = view.findViewById(R.id.et_power);
        etFuel = view.findViewById(R.id.et_fuel);
        etBastidor = view.findViewById(R.id.et_bastidor);
        etEmissionNorm = view.findViewById(R.id.et_emission_norm);
        etRegisteringAuthority = view.findViewById(R.id.et_registering_authority);
        etManufacturingDate = view.findViewById(R.id.et_manufacturing_date);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        switchVehicleStatus = view.findViewById(R.id.switch_vehicle_status);
        btnCreateVehicle = view.findViewById(R.id.btn_create_vehicle);

        btnSelectImage.setOnClickListener(v -> openFileChooser());
        btnCreateVehicle.setOnClickListener(v -> createVehicle());
        etManufacturingDate.setOnClickListener(v -> showDatePickerDialog());

        calendar = Calendar.getInstance();
        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etManufacturingDate.setText(sdf.format(calendar.getTime()));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            updateSelectImageButtonState(true);
        }
    }

    private void updateSelectImageButtonState(boolean isImageSelected) {
        if (isImageSelected) {
            btnSelectImage.setText(R.string.image_selected);
            btnSelectImage.setBackgroundResource(R.drawable.btn_background_selected);
            btnSelectImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_image_uploaded, 0, 0, 0);
        } else {
            btnSelectImage.setText(R.string.select_image);
            btnSelectImage.setBackgroundResource(R.drawable.btn_background_1);
            btnSelectImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload, 0, 0, 0);
        }
    }

    private void createVehicle() {
        String brand = etBrand.getText().toString().trim();
        String plate = etPlate.getText().toString().trim();
        String owners = etOwners.getText().toString().trim();
        String power = etPower.getText().toString().trim();
        String fuel = etFuel.getText().toString().trim();
        String bastidor = etBastidor.getText().toString().trim();
        String emissionNorm = etEmissionNorm.getText().toString().trim();
        String registeringAuthority = etRegisteringAuthority.getText().toString().trim();
        String manufacturingDate = etManufacturingDate.getText().toString().trim();
        boolean vehicleStatus = switchVehicleStatus.isChecked();

        if (!validateFields(brand, plate, owners, power, fuel, bastidor, emissionNorm, registeringAuthority, manufacturingDate)) {
            return;
        }

        if (imageUri != null) {
            uploadImageAndSaveVehicle(brand, plate, owners, power, fuel, bastidor, emissionNorm, registeringAuthority, manufacturingDate, vehicleStatus);
        } else {
            Toast.makeText(getContext(), R.string.image_url_required, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields(String brand, String plate, String owners, String power, String fuel, String bastidor, String emissionNorm, String registeringAuthority, String manufacturingDate) {
        if (TextUtils.isEmpty(brand)) {
            etBrand.setError(getString(R.string.brand_required));
            return false;
        }
        if (TextUtils.isEmpty(plate)) {
            etPlate.setError(getString(R.string.plate_number_required));
            return false;
        }
        if (TextUtils.isEmpty(owners)) {
            etOwners.setError(getString(R.string.owners_required));
            return false;
        } else {
            try {
                int ownerCount = Integer.parseInt(owners);
                if (ownerCount <= 0) {
                    etOwners.setError(getString(R.string.owners_positive_number));
                    return false;
                }
            } catch (NumberFormatException e) {
                etOwners.setError(getString(R.string.owners_valid_number));
                return false;
            }
        }
        if (TextUtils.isEmpty(power)) {
            etPower.setError(getString(R.string.power_required));
            return false;
        }
        if (TextUtils.isEmpty(fuel)) {
            etFuel.setError(getString(R.string.fuel_required));
            return false;
        }
        if (TextUtils.isEmpty(bastidor)) {
            etBastidor.setError(getString(R.string.bastidor_required));
            return false;
        }
        if (TextUtils.isEmpty(emissionNorm)) {
            etEmissionNorm.setError(getString(R.string.emission_norm_required));
            return false;
        }
        if (TextUtils.isEmpty(registeringAuthority)) {
            etRegisteringAuthority.setError(getString(R.string.registering_authority_required));
            return false;
        }
        if (TextUtils.isEmpty(manufacturingDate)) {
            etManufacturingDate.setError(getString(R.string.manufacturing_date_required));
            return false;
        }
        return true;
    }

    private void uploadImageAndSaveVehicle(String brand, String plate, String owners, String power, String fuel, String bastidor, String emissionNorm, String registeringAuthority, String manufacturingDate, boolean vehicleStatus) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("vehicle_images/" + UUID.randomUUID().toString());
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveVehicle(brand, plate, owners, power, fuel, bastidor, emissionNorm, registeringAuthority, manufacturingDate, vehicleStatus, imageUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show());
    }

    private void saveVehicle(String brand, String plate, String owners, String power, String fuel, String bastidor, String emissionNorm, String registeringAuthority, String manufacturingDate, boolean vehicleStatus, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cars");
        String key = databaseReference.push().getKey();

        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("brand", brand);
        vehicleData.put("plateText", plate);
        vehicleData.put("owners", Integer.parseInt(owners));
        vehicleData.put("power", power);
        vehicleData.put("fuel", fuel);
        vehicleData.put("vehicleStatus", vehicleStatus);
        vehicleData.put("imageUrl", imageUrl);

        Map<String, Object> infoData = new HashMap<>();
        infoData.put("Bastidor", bastidor);
        infoData.put("EmissionNorm", emissionNorm);
        infoData.put("RegisteringAuthority", registeringAuthority);
        infoData.put("ManufacturingDate", manufacturingDate);

        Map<String, Object> finalData = new HashMap<>();
        finalData.put("brand", brand);
        finalData.put("plateText", plate);
        finalData.put("owners", Integer.parseInt(owners));
        finalData.put("power", power);
        finalData.put("fuel", fuel);
        finalData.put("vehicleStatus", vehicleStatus);
        finalData.put("imageUrl", imageUrl);
        finalData.put("info", infoData);

        if (key != null) {
            databaseReference.child(key).setValue(finalData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), R.string.vehicle_created_successfully, Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.failed_to_create_vehicle, Toast.LENGTH_SHORT).show());
        }
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_fragment_container, new DashboardFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
