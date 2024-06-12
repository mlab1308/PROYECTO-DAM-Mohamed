package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.Activities.loginActivity;
import iesmm.pmdm.autolabibscan.Models.User;
import iesmm.pmdm.autolabibscan.R;

public class ProfileFragment extends Fragment {

    // Declaración de variables para los elementos de la UI y Firebase
    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private ProgressBar progressBar;
    private View profileContent;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar las vistas del layout
        profileImage = view.findViewById(R.id.imageView2);
        profileName = view.findViewById(R.id.textView);
        profileEmail = view.findViewById(R.id.textView2);
        progressBar = view.findViewById(R.id.progressBar);
        profileContent = view.findViewById(R.id.profileContent);

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Si el usuario actual no es nulo, obtener la referencia de su información en la base de datos
        if (currentUser != null) {
            userRef = database.getReference().child("users").child(currentUser.getUid());
            loadUserData();
        }

        // Manejar el botón de editar perfil
        view.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            // Crear y realizar la transacción para cambiar al fragmento de editar perfil con animación
            Fragment editProfileFragment = new EditProfileFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.fragment_container, editProfileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Manejar el botón de mis favoritos
        view.findViewById(R.id.buttonFavorites).setOnClickListener(v -> {
            // Crear y realizar la transacción para cambiar al fragmento de favoritos con animación
            Fragment favoritesFragment = new FavoritesFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.fragment_container, favoritesFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Manejar el botón de cerrar sesión
        view.findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            // Cerrar sesión en Firebase y redirigir al usuario a la pantalla de inicio de sesión
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), loginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    // Método para cargar los datos del usuario desde la base de datos
    private void loadUserData() {
        // Mostrar el progreso y ocultar el contenido del perfil mientras se cargan los datos
        progressBar.setVisibility(View.VISIBLE);
        profileContent.setVisibility(View.GONE);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si los datos existen en la base de datos
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Configurar los valores de las vistas con los datos del usuario
                        profileName.setText(user.getName() + " " + user.getLastName());
                        profileEmail.setText(user.getEmail());
                        // Cargar la imagen de perfil si existe, de lo contrario, cargar una imagen por defecto
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            // Verificar que el fragmento esté adjunto a una actividad antes de usar Glide
                            if (getActivity() != null) {
                                Glide.with(getContext())
                                        .load(user.getProfileImageUrl())
                                        .transform(new CircleCrop())
                                        .into(profileImage);
                            }
                        } else {
                            // Verificar que el fragmento esté adjunto a una actividad antes de usar Glide
                            if (getActivity() != null) {
                                Glide.with(getContext())
                                        .load(R.drawable.ic_profile_default)
                                        .transform(new CircleCrop())
                                        .into(profileImage);
                            }
                        }
                    }
                }
                // Ocultar el progreso y mostrar el contenido del perfil después de cargar los datos
                progressBar.setVisibility(View.GONE);
                profileContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error y ocultar el progreso en caso de fallo
                progressBar.setVisibility(View.GONE);
                profileContent.setVisibility(View.VISIBLE);
            }
        });
    }
}
