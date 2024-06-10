package iesmm.pmdm.autolabibscan.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import iesmm.pmdm.autolabibscan.Models.Vehicle;
import iesmm.pmdm.autolabibscan.R;

public class CreateVehicleFragment extends Fragment {

    private EditText etBrand, etPlate, etOwners, etPower, etFuel, etBastidor, etEmissionNorm, etRegisteringAuthority, etManufacturingDate, etImageUrl;
    private Switch switchVehicleStatus;
    private Button btnCreateVehicle;

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
        etImageUrl = view.findViewById(R.id.et_image_url);
        switchVehicleStatus = view.findViewById(R.id.switch_vehicle_status);
        btnCreateVehicle = view.findViewById(R.id.btn_create_vehicle);

        btnCreateVehicle.setOnClickListener(v -> createVehicle());

        return view;
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
        String imageUrl = etImageUrl.getText().toString().trim();
        boolean vehicleStatus = switchVehicleStatus.isChecked();

        if (!validateFields(brand, plate, owners, power, fuel, bastidor, emissionNorm, registeringAuthority, manufacturingDate, imageUrl)) {
            return;
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(brand);
        vehicle.setPlateText(plate);
        vehicle.setOwners(Integer.parseInt(owners));
        vehicle.setPower(power);
        vehicle.setFuel(fuel);
        vehicle.setBastidor(bastidor);
        vehicle.setEmissionNorm(emissionNorm);
        vehicle.setRegisteringAuthority(registeringAuthority);
        vehicle.setManufacturingDate(manufacturingDate);
        vehicle.setImageUrl(imageUrl);
        vehicle.setVehicleStatus(vehicleStatus);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cars");
        databaseReference.push().setValue(vehicle)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), R.string.vehicle_created_successfully, Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), R.string.failed_to_create_vehicle, Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateFields(String brand, String plate, String owners, String power, String fuel, String bastidor, String emissionNorm, String registeringAuthority, String manufacturingDate, String imageUrl) {
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
        if (TextUtils.isEmpty(imageUrl)) {
            etImageUrl.setError(getString(R.string.image_url_required));
            return false;
        }
        return true;
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
