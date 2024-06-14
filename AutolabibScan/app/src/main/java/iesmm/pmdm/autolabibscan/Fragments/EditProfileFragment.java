package iesmm.pmdm.autolabibscan.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import iesmm.pmdm.autolabibscan.Models.User;
import iesmm.pmdm.autolabibscan.R;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private EditText editTextName, editTextLastName, editTextEmail;
    private ImageButton backButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private StorageReference storageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        editTextName = view.findViewById(R.id.editTextName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        backButton = view.findViewById(R.id.backButton);
        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = database.getReference().child("users").child(currentUser.getUid());
            loadUserData();
        }

        backButton.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        profileImageView.setOnClickListener(v -> openImageChooser());
        view.findViewById(R.id.buttonSave).setOnClickListener(v -> saveUserData());

        return view;
    }

    // Método para cargar los datos del usuario desde la base de datos
    private void loadUserData() {
        // Mostrar el progreso y ocultar el contenido del perfil mientras se cargan los datos
        progressBar.setVisibility(View.VISIBLE);


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si los datos existen en la base de datos
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Configurar los valores de las vistas con los datos del usuario
                        editTextName.setText(user.getName().toString() );
                        editTextLastName.setText(user.getLastName().toString());
                        editTextEmail.setText(user.getEmail().toString());
                        // Cargar la imagen de perfil si existe, de lo contrario, cargar una imagen por defecto
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            // Verificar que el fragmento esté adjunto a una actividad antes de usar Glide
                            if (getActivity() != null) {
                                Glide.with(getContext())
                                        .load(user.getProfileImageUrl())
                                        .transform(new CircleCrop())
                                        .into(profileImageView);
                            }
                        } else {
                            // Verificar que el fragmento esté adjunto a una actividad antes de usar Glide
                            if (getActivity() != null) {
                                Glide.with(getContext())
                                        .load(R.drawable.ic_profile_default)
                                        .transform(new CircleCrop())
                                        .into(profileImageView);
                            }
                        }
                    }
                }
                // Ocultar el progreso y mostrar el contenido del perfil después de cargar los datos
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error y ocultar el progreso en caso de fallo
                progressBar.setVisibility(View.GONE);

            }
        });
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
                profileImageView.setImageBitmap(bitmap);
                uploadImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);

            StorageReference fileReference = storageRef.child("profile_images/" + currentUser.getUid() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("profileImageUrl").setValue(uri.toString());
                        Toast.makeText(getContext(), "Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }

    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("name").setValue(name);
        userRef.child("lastName").setValue(lastName);
        userRef.child("email").setValue(email);

        Toast.makeText(getContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}
