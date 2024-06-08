package iesmm.pmdm.autolabibscan.Activities;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.Models.User;
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

        try {
            // Inicialización de FirebaseAuth
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();

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

            // Configuración de Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client))
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
                    if (emailUser.isEmpty() || passwordUser.isEmpty()) {
                        // Mostrar un mensaje si los campos están vacíos
                        Toast.makeText(loginActivity.this, getString(R.string.login_fields_empty), Toast.LENGTH_SHORT).show();
                    } else {
                        // Llamada al método para iniciar sesión del usuario
                        loginUser(emailUser, passwordUser);
                    }
                }
            });

            // Verificar si el usuario ya está autenticado
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Registrar el acceso
                logAccess(currentUser.getEmail());
                // Redirigir a la actividad correspondiente
                redirectToDashboard(currentUser.getUid());
            }
        } catch (Exception e) {
            Log.e("loginActivity", "Error in onCreate: ", e);
        }
    }

    // Método para iniciar el proceso de Google Sign-In
    private void googleSignIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Log.e("loginActivity", "Error in googleSignIn: ", e);
        }
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
                Log.w("loginActivity", getString(R.string.google_signin_failed), e);
                Toast.makeText(this, getString(R.string.google_signin_failed) + ": " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para autenticar con Firebase usando Google
    private void firebaseAuthWithGoogle(String idToken) {
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Verificar si el usuario ya existe en la base de datos
                                    DatabaseReference userRef = database.getReference().child("users").child(user.getUid());
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                // Si el usuario no existe en la base de datos, crearlo
                                                userRef.setValue(new User(user.getDisplayName(), user.getEmail(), "user"));
                                            } else {
                                                // Verificar y establecer el rol si está vacío
                                                String role = snapshot.child("role").getValue(String.class);
                                                if (role == null || role.isEmpty()) {
                                                    userRef.child("role").setValue("user");
                                                }
                                            }
                                            logAccess(user.getEmail()); // Registrar acceso
                                            updateUI(user);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.w("loginActivity", "Error al verificar la existencia del usuario", error.toException());
                                        }
                                    });
                                }
                            } else {
                                Log.w("loginActivity", "signInWithCredential:failure", task.getException());
                                Toast.makeText(loginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("loginActivity", "Error in firebaseAuthWithGoogle: ", e);
        }
    }

    // Actualiza la UI después de la autenticación
    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                // Redirige al usuario a la actividad correspondiente
                redirectToDashboard(user.getUid());
            }
        } catch (Exception e) {
            Log.e("loginActivity", "Error in updateUI: ", e);
        }
    }

    // Método para iniciar sesión de usuario con correo electrónico y contraseña
    private void loginUser(String emailUser, String passwordUser) {
        try {
            mAuth.signInWithEmailAndPassword(emailUser, passwordUser)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    logAccess(emailUser); // Registrar acceso
                                    fetchUserRoleAndRedirect(user.getUid(), emailUser);
                                }
                            } else {
                                // Mostrar un mensaje de error si la operación falla
                                Toast.makeText(loginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Mostrar un mensaje de error si ocurre una excepción durante el inicio de sesión
                            Toast.makeText(loginActivity.this, getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("loginActivity", "Error in loginUser: ", e);
        }
    }

    private void fetchUserRoleAndRedirect(String userId, String emailUser) {
        try {
            // Obtener la referencia del usuario en la base de datos
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        // Verificar el rol del usuario y redirigir a la actividad correspondiente
                        if (role != null) {
                            redirectToDashboard(userId);
                        } else {
                            // Si el rol no está definido, establecer un rol por defecto
                            userRef.child("role").setValue("user");
                            redirectToDashboard(userId);
                        }
                    } else {
                        // Si el usuario no existe en la base de datos, crear una entrada para el usuario
                        userRef.setValue(new User("", emailUser, "user"));
                        redirectToDashboard(userId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("loginActivity", "Error al obtener el rol del usuario", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e("loginActivity", "Error in fetchUserRoleAndRedirect: ", e);
        }
    }

    // Método para redirigir al usuario al dashboard
    private void redirectToDashboard(String userId) {
        try {
            Intent intent = new Intent(loginActivity.this, dashboardActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("loginActivity", "Error in redirectToDashboard: ", e);
        }
    }

    private void logAccess(String email) {
        try {
            DatabaseReference accessRef = FirebaseDatabase.getInstance().getReference().child("access_logs");
            String key = accessRef.push().getKey();
            AccessItem accessItem = new AccessItem(email, new Date());
            accessRef.child(key).setValue(accessItem);
        } catch (Exception e) {
            Log.e("loginActivity", "Error in logAccess: ", e);
        }
    }
}
