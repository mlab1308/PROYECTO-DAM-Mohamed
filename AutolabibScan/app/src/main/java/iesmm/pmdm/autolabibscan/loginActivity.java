package iesmm.pmdm.autolabibscan;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class loginActivity extends AppCompatActivity {
    // Declaración de variables
    private Button btnLogin;
    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private TextView txtRegisterRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialización de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Referencia elementos del layout
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        txtRegisterRedirect = findViewById(R.id.txtRegisterRedirect);

        //Accion onClick en redirigir al registro
        txtRegisterRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirecciona a la pantalla de registro
                Intent intent = new Intent(loginActivity.this, registerActivity.class);
                startActivity(intent);
            }
        });

        // Configuración del Listener para el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtención de los valores de los campos de correo electrónico y contraseña
                String emailUser = edtEmail.getText().toString().trim();
                String passwordUser = edtPassword.getText().toString().trim();

                // Verificación de si los campos están vacíos
                if (emailUser.isEmpty() && passwordUser.isEmpty()) {
                    // Mostrar un mensaje si los campos están vacíos
                    Toast.makeText(loginActivity.this, "Por favor, ingrese correo electrónico y contraseña.", Toast.LENGTH_SHORT).show();
                } else {
                    // Llamada al método para iniciar sesión del usuario
                    loginUser(emailUser, passwordUser);
                }
            }
        });
    }

    // Método para iniciar sesión de usuario con correo electrónico y contraseña
    private void loginUser(String emailUser, String passwordUser) {
        // Utilizar el método signInWithEmailAndPassword de FirebaseAuth
        mAuth.signInWithEmailAndPassword(emailUser, passwordUser)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Manejar el resultado de la operación de inicio de sesión
                        if (task.isSuccessful()) {
                            // Si la operación es exitosa, redirigir a la actividad principal
                            finish();
                            startActivity(new Intent(loginActivity.this, MainActivity.class));
                        } else {
                            // Mostrar un mensaje de error si la operación falla
                            Toast.makeText(loginActivity.this, "No se pudo iniciar sesión. Verifique sus credenciales e inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Mostrar un mensaje de error si ocurre una excepción durante el inicio de sesión
                        Toast.makeText(loginActivity.this, "Se produjo un error al iniciar sesión. Por favor, inténtelo de nuevo más tarde.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
