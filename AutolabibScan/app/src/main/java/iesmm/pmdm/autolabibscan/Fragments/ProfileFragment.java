package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.Activities.loginActivity;
import iesmm.pmdm.autolabibscan.R;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private Button btnEditProfile;
    private Button btnLogout;
    private String userRole;
    private String userName;
    private String userEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicialización de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Referencia elementos del layout
        imgProfile = view.findViewById(R.id.imgProfile);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Obtener la información del usuario desde Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            fetchUserInfo();
        }

        // Agregar funcionalidad al botón de editar perfil
        btnEditProfile.setOnClickListener(v -> {
            //Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            //startActivity(intent);
        });

        // Agregar funcionalidad al botón de cerrar sesión
        btnLogout.setOnClickListener(v -> {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut();
            // Redirigir a la pantalla de inicio de sesión
            Intent intent = new Intent(getActivity(), loginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void fetchUserInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("role").getValue(String.class);
                    userName = snapshot.child("name").getValue(String.class);
                    userEmail = snapshot.child("email").getValue(String.class);

                    // Configurar la información del perfil
                    txtProfileName.setText(userName != null ? userName : "Nombre del Usuario");
                    txtProfileEmail.setText(userEmail != null ? userEmail : "usuario@correo.com");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // errores
            }
        });
    }
}
