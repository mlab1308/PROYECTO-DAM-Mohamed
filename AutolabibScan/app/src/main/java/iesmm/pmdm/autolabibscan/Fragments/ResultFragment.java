package iesmm.pmdm.autolabibscan.Fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.R;

public class ResultFragment extends Fragment {

    private ImageView carLogo;
    private TextView carBrand, plateText, ownersText, powerText, fuelText;
    private ImageButton backButton, favoriteButton;
    private TextView bastidor, registeringAuthority, emissionNorm, manufacturingDate, vehicleStatus;

    private String[] carBrands;
    private int[] carBrandLogos;
    private boolean isFavorite = false;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        backButton = view.findViewById(R.id.backButton);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        carLogo = view.findViewById(R.id.carLogo);

        carBrand = view.findViewById(R.id.carBrand);
        plateText = view.findViewById(R.id.plateText);
        ownersText = view.findViewById(R.id.ownersText);
        powerText = view.findViewById(R.id.powerText);
        fuelText = view.findViewById(R.id.fuelText);

        bastidor = view.findViewById(R.id.bastidor);
        registeringAuthority = view.findViewById(R.id.registeringAuthority);
        emissionNorm = view.findViewById(R.id.emissionNorm);
        manufacturingDate = view.findViewById(R.id.manufacturingDate);
        vehicleStatus = view.findViewById(R.id.vehicleStatus);

        // Cargar los recursos de marcas y logotipos
        carBrands = getResources().getStringArray(R.array.car_brands);
        carBrandLogos = loadCarBrandLogos();

        // Obtener los datos del Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String carBrandStr = bundle.getString("carBrand");
            String plateStr = bundle.getString("plateText");
            String ownersStr = bundle.getString("ownersText");
            String powerStr = bundle.getString("powerText");
            String fuelStr = bundle.getString("fuelText");
            String bastidorStr = bundle.getString("Bastidor");
            String registeringAuthorityStr = bundle.getString("RegisteringAuthority");
            String emissionNormStr = bundle.getString("EmissionNorm");
            String manufacturingDateStr = bundle.getString("ManufacturingDate");
            boolean vehicleStatusBool = bundle.getBoolean("VehicleStatus");

            carBrand.setText(carBrandStr);
            plateText.setText(plateStr);
            ownersText.setText(ownersStr);
            powerText.setText(powerStr);
            fuelText.setText(fuelStr);

            bastidor.setText(getString(R.string.bastidor) + ": " + bastidorStr);
            registeringAuthority.setText(getString(R.string.registering_authority) + ": " + registeringAuthorityStr);
            emissionNorm.setText(getString(R.string.emission_norm) + ": " + emissionNormStr);
            manufacturingDate.setText(getString(R.string.manufacturing_date) + ": " + manufacturingDateStr);
            vehicleStatus.setText(getString(R.string.vehicle_status) + ": " + (vehicleStatusBool ? getString(R.string.active) : getString(R.string.inactive)));

            // Buscar el logotipo de la marca del coche y establecerlo
            boolean logoFound = false;
            for (int i = 0; i < carBrands.length; i++) {
                if (carBrands[i].equalsIgnoreCase(carBrandStr)) {
                    carLogo.setImageResource(carBrandLogos[i]);
                    logoFound = true;
                    break;
                }
            }

            // Si no se encuentra el logotipo, establecer el logotipo por defecto
            if (!logoFound) {
                carLogo.setImageResource(R.drawable.default_logo_car);
            }

            // Obtener el ID del usuario actual
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
                checkFavoriteStatus(plateStr);
            }

            // Manejar el botón de favoritos
            favoriteButton.setOnClickListener(v -> {
                isFavorite = !isFavorite;
                updateFavoriteButton();
                saveFavoriteStatus(plateStr, isFavorite);
            });
        }

        // Manejar el botón de retroceso
        backButton.setOnClickListener(v -> getFragmentManager().popBackStack());

        return view;
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_star_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_star_empty);
        }
    }

    private void saveFavoriteStatus(String plateText, boolean isFavorite) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(plateText);

        if (isFavorite) {
            databaseReference.setValue(true);
            Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.removeValue();
            Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFavoriteStatus(String plateText) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(plateText);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isFavorite = true;
                } else {
                    isFavorite = false;
                }
                updateFavoriteButton();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error checking favorite status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int[] loadCarBrandLogos() {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.car_brand_logos);
        int[] logos = new int[typedArray.length()];
        for (int i = 0; i < typedArray.length(); i++) {
            logos[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return logos;
    }
}
