package iesmm.pmdm.autolabibscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import iesmm.pmdm.autolabibscan.R;

public class registerActivity extends AppCompatActivity {
    // Declaración de variables
    private EditText edtFirstName, edtLastName, edtEmail, edtPassword;
    private Button btnRegister;
    private TextView txtLoginRedirect;

    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        dataBase = FirebaseDatabase.getInstance();

        // Referencia de los elementos del layout
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect);

        // Manejo del clic en el texto de redirección al login
        txtLoginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirección a la actividad de inicio de sesión
                Intent intent = new Intent(registerActivity.this, loginActivity.class);
                startActivity(intent);
                finish(); // Finaliza esta actividad para evitar que el usuario pueda volver atrás al registro
            }
        });

        // Manejo del clic en el botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtención de los datos ingresados por el usuario
                String firstName = edtFirstName.getText().toString().trim();
                String lastName = edtLastName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                // Validar todos los campos
                if (firstName.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.first_name_required), Snackbar.LENGTH_SHORT).show();
                } else if (lastName.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.last_name_required), Snackbar.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.email_required), Snackbar.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_email_format), Snackbar.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.password_required), Snackbar.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.password_length_error), Snackbar.LENGTH_SHORT).show();
                } else {
                    // Método para registrar al usuario en Firebase Auth
                    registerUser(firstName, lastName, email, password);
                }
            }
        });
    }

    // Método para registrar al usuario en Firebase Auth
    private void registerUser(final String firstName, final String lastName, final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            String userId = mAuth.getCurrentUser().getUid();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("firstName", firstName);
                            map.put("lastName", lastName);
                            map.put("email", email);
                            map.put("role", "user");

                            dataBase.getReference().child("users").child(userId).setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.register_success), Snackbar.LENGTH_SHORT).show();
                                                // Redirección a la actividad de inicio de sesión después del registro exitoso
                                                startActivity(new Intent(registerActivity.this, loginActivity.class));
                                                finish(); // Finaliza esta actividad para evitar que el usuario pueda volver atrás al registro
                                            } else {
                                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.register_db_error), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Registro fallido
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.register_error), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
