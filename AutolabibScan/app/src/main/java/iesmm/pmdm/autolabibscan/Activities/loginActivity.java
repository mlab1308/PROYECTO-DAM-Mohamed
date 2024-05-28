package iesmm.pmdm.autolabibscan.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iesmm.pmdm.autolabibscan.R;

public class loginActivity extends AppCompatActivity {
    private final int RC_SIGN_IN = 20;
    // Declaración de variables
    private Button btnLogin;
    private ImageButton btnGoogle;
    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView txtRegisterRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Referencia elementos del layout
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        txtRegisterRedirect = findViewById(R.id.txtRegisterRedirect);

        // Acción onClick para redirigir al registro
        txtRegisterRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirecciona a la pantalla de registro
                Intent intent = new Intent(loginActivity.this, registerActivity.class);
                startActivity(intent);
            }
        });

        // Inicialización de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("388872684081-9sgjsvqisrjvrju9uoqb6baa7rdkpf1t.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configuración del Listener para el botón de Google Sign-In
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
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

    // Método para iniciar el proceso de Google Sign-In
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("DEBUG", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para autenticar con Firebase usando Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w("DEBUG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(loginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // Actualiza la UI después de la autenticación
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Redirige al usuario a la actividad correspondiente
            startActivity(new Intent(loginActivity.this, dashboardActivity.class));
        }
    }

    // Método para iniciar sesión de usuario con correo electrónico y contraseña
    private void loginUser(String emailUser, String passwordUser) {
        mAuth.signInWithEmailAndPassword(emailUser, passwordUser)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            // Obtener la referencia del usuario en la base de datos
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        // Verificar el rol del usuario y redirigir a la actividad correspondiente
                                        if (role != null) {
                                            // Almacenar el rol y el correo electrónico del usuario en SharedPreferences
                                            SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("user_role", role);
                                            editor.putString("user_email", emailUser);
                                            editor.apply();
                                            if (role.equals("admin")) {
                                                startActivity(new Intent(loginActivity.this, accessListActivity.class));
                                                finish();
                                            } else if (role.equals("user")) {
                                                startActivity(new Intent(loginActivity.this, dashboardActivity.class));
                                                finish();
                                            } else {
                                                // Si el rol no está definido correctamente
                                                Toast.makeText(loginActivity.this, "Rol de usuario no válido.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Si no se encuentra el rol del usuario
                                            Toast.makeText(loginActivity.this, "Rol de usuario no encontrado.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Si no se encuentra la información del usuario
                                        Toast.makeText(loginActivity.this, "Información de usuario no encontrada.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(loginActivity.this, "Error de base de datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
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
