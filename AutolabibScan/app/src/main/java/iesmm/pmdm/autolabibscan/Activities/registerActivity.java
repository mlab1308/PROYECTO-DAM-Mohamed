package iesmm.pmdm.autolabibscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import iesmm.pmdm.autolabibscan.R;

public class registerActivity extends AppCompatActivity {
    // Declaración de variables
    private EditText edtEmail, edtPassword;
    private Button btnRegister;
    private TextView txtLoginRedirect;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Referencia de los elementos del layout
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
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                // Método para registrar al usuario en Firebase Auth
                registerUser(email, password);
            }
        });
    }

    // Método para registrar al usuario en Firebase Auth
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            Toast.makeText(registerActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            // Redirección a la actividad de inicio de sesión después del registro exitoso
                            startActivity(new Intent(registerActivity.this, loginActivity.class));
                            finish(); // Finaliza esta actividad para evitar que el usuario pueda volver atrás al registro
                        } else {
                            // Registro fallido
                            Toast.makeText(registerActivity.this, "Error al registrar. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
