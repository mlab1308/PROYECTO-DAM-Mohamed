package iesmm.pmdm.autolabibscan.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import iesmm.pmdm.autolabibscan.R;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private Button btnEditProfile;
    private BottomNavigationView bottomNavigationView;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Referencia elementos del layout
        imgProfile = view.findViewById(R.id.imgProfile);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);

        // Obtener el rol del usuario de SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("user_info", getActivity().MODE_PRIVATE);
        userRole = preferences.getString("user_role", "");

        // Configurar la información del perfil
        txtProfileName.setText("Nombre del Usuario");
        txtProfileEmail.setText("usuario@correo.com");

        // Agregar funcionalidad al botón de editar perfil
        btnEditProfile.setOnClickListener(v -> {

        });

        return view;
    }
}
