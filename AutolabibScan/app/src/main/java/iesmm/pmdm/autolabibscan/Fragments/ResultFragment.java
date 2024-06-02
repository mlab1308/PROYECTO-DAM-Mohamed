package iesmm.pmdm.autolabibscan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import iesmm.pmdm.autolabibscan.R;

public class ResultFragment extends Fragment {

    private ImageView carLogo, plateImage, ownersIcon, powerIcon, fuelIcon;
    private TextView carBrand, plateText, ownersText, powerText, fuelText, vehicleInfo;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        backButton = view.findViewById(R.id.backButton);
        carLogo = view.findViewById(R.id.carLogo);
        plateImage = view.findViewById(R.id.plateImage);
        ownersIcon = view.findViewById(R.id.ownersIcon);
        powerIcon = view.findViewById(R.id.powerIcon);
        fuelIcon = view.findViewById(R.id.fuelIcon);

        carBrand = view.findViewById(R.id.carBrand);
        plateText = view.findViewById(R.id.plateText);
        ownersText = view.findViewById(R.id.ownersText);
        powerText = view.findViewById(R.id.powerText);
        fuelText = view.findViewById(R.id.fuelText);
        vehicleInfo = view.findViewById(R.id.vehicleInfo);

        // Obtener los datos del Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            carBrand.setText(bundle.getString("carBrand"));
            plateText.setText(bundle.getString("plateText"));
            ownersText.setText(bundle.getString("ownersText"));
            powerText.setText(bundle.getString("powerText"));
            fuelText.setText(bundle.getString("fuelText"));
            vehicleInfo.setText(bundle.getString("vehicleInfo"));
        }
        // Manejar el botón de retroceso
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
