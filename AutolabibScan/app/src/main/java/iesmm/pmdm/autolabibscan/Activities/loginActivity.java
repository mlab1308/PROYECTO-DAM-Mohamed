package iesmm.pmdm.autolabibscan.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.android.material.snackbar.Snackbar;
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
    private ProgressDialog progressDialog;

    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView txtRegisterRedirect, forgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            // Inicialización de FirebaseAuth
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            // Inicialización del ProgressDialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            // Referencia elementos del layout
            btnLogin = findViewById(R.id.btnLogin);
            btnGoogle = findViewById(R.id.btnGoogle);
            edtEmail = findViewById(R.id.edtEmail);
            edtPassword = findViewById(R.id.edtPassword);
            txtRegisterRedirect = findViewById(R.id.txtRegisterRedirect);
            forgotPass = findViewById(R.id.forgotpass);

            // Acción onClick para redirigir al registro
            txtRegisterRedirect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirecciona a la pantalla de registro
                    Intent intent = new Intent(loginActivity.this, registerActivity.class);
                    startActivity(intent);
                }
            });

            // Configuración del Listener para el enlace de "Forgot Password"
            forgotPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtiene el email introducido
                    String email = edtEmail.getText().toString().trim();
                    if (email.isEmpty()) {
                        // Muestra un mensaje si el campo de email está vacío
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.email_required), Snackbar.LENGTH_SHORT).show();
                    } else {
                        // Llama al método para resetear la contraseña
                        resetPassword(email);
                    }
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
                    // Mostrar el ProgressDialog
                    progressDialog.show();
                }
            });

            // Configuración del Listener para el botón de inicio de sesión
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtención de los valores de los campos de correo electrónico y contraseña
                    String emailUser = edtEmail.getText().toString().trim();
                    String passwordUser = edtPassword.getText().toString().trim();

                    // Verificación de si los campos están vacíos o el correo no es válido
                    if (emailUser.isEmpty() || passwordUser.isEmpty()) {
                        // Mostrar un mensaje si los campos están vacíos
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_fields_empty), Snackbar.LENGTH_SHORT).show();
                    } else if (!isValidEmail(emailUser)) {
                        // Mostrar un mensaje si el formato del correo no es válido
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_email_format), Snackbar.LENGTH_SHORT).show();
                    } else {
                        // Mostrar el ProgressDialog
                        progressDialog.show();
                        // Llamada al método para iniciar sesión del usuario
                        loginUser(emailUser, passwordUser);
                    }

                }
            });

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

        // Verifica si el requestCode corresponde a Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            // Obtiene el resultado del intento de inicio de sesión con Google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Intenta obtener la cuenta de Google desde el resultado del intento de inicio de sesión
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Si la cuenta se obtiene correctamente, se autentica con Firebase usando el token de ID de Google
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Si ocurre una excepción  registra una advertencia en el log
                Log.w("loginActivity", getString(R.string.google_signin_failed), e);
                // Oculta el ProgressDialog
                progressDialog.dismiss();
                // Muestra un mensaje de error usando Snackbar
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.google_signin_failed) + ": " + e.getStatusCode(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    // Método para autenticar con Firebase usando Google
    private void firebaseAuthWithGoogle(String idToken) {
        try {
            // Crear una credencial de autenticación con el token de ID de Google
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

            // Iniciar sesión en Firebase con la credencial de Google
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Oculta el ProgressDialog
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                // Si la autenticación es exitosa, obtener el usuario actual de Firebase
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Obtener los datos del usuario autenticado con Google
                                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(loginActivity.this);
                                    String firstName = account != null ? account.getGivenName() : "";
                                    String lastName = account != null ? account.getFamilyName() : "";

                                    // Verificar si el usuario ya existe en la base de datos
                                    DatabaseReference userRef = database.getReference().child("users").child(user.getUid());
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                // Si el usuario no existe en la base de datos, crearlo
                                                userRef.setValue(new User(firstName, lastName, user.getEmail(), "user"));
                                            } else {
                                                // Verificar y establecer el rol si está vacío
                                                String role = snapshot.child("role").getValue(String.class);
                                                if (role == null || role.isEmpty()) {
                                                    userRef.child("role").setValue("user");
                                                }
                                            }
                                            // Registrar acceso del usuario
                                            logAccess(user.getEmail());
                                            // Actualizar la UI para reflejar el estado del usuario autenticado
                                            updateUI(user);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Manejar errores de la base de datos
                                            Log.w("loginActivity", "Error al verificar la existencia del usuario", error.toException());
                                        }
                                    });
                                }
                            } else {
                                // Si la autenticación falla, registrar una advertencia y mostrar un mensaje de error
                                Log.w("loginActivity", "signInWithCredential:failure", task.getException());
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.authentication_failed), Snackbar.LENGTH_SHORT).show();
                                // Actualizar la UI para reflejar que el usuario no está autenticado
                                updateUI(null);
                            }
                        }
                    });
        } catch (Exception e) {
            // Manejar cualquier excepción que ocurra durante el proceso de autenticación
            Log.e("loginActivity", "Error in firebaseAuthWithGoogle: ", e);
            // Oculta el ProgressDialog
            progressDialog.dismiss();
        }
    }



    // Método para resetear la contraseña
    private void resetPassword(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showAlertDialog("Success", getString(R.string.reset_password_email_sent));
                        } else {
                            showAlertDialog("Error", getString(R.string.reset_password_email_failed));
                        }
                    }
                });
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
                            // Oculta el ProgressDialog
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    logAccess(emailUser); // Registrar acceso
                                    fetchUserRoleAndRedirect(user.getUid(), emailUser);
                                }
                            } else {
                                // Mostrar un mensaje de error si la operación falla
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_failed), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Oculta el ProgressDialog
                            progressDialog.dismiss();
                            // Mostrar un mensaje de error si ocurre una excepción durante el inicio de sesión
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_error), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("loginActivity", "Error in loginUser: ", e);
            // Oculta el ProgressDialog
            progressDialog.dismiss();
        }
    }
    // Método para obtener el rol del usuario y redirigir
    private void fetchUserRoleAndRedirect(String userId, String emailUser) {
        try {
            // Referencia alusuario en la base de datos de Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);


            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Si el usuario existe, obtener el valor del rol
                        String role = snapshot.child("role").getValue(String.class);
                        if (role != null) {
                            // Si el rol no es nulo, redirigir al dashboard correspondiente
                            redirectToDashboard(role);
                        } else {
                            // Si el rol es nulo, establecer el rol como "user" y redirigir al dashboard de usuario
                            userRef.child("role").setValue("user");
                            redirectToDashboard("user");
                        }
                    } else {
                        // Si el usuario no existe en la base de datos, crear un nuevo usuario con el rol "user"
                        userRef.setValue(new User("","", emailUser, "user"));
                        redirectToDashboard("user");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar errores al obtener el rol del usuario
                    Log.w("loginActivity", "Error al obtener el rol del usuario", error.toException());
                }
            });
        } catch (Exception e) {
            // Manejar cualquier excepción que ocurra durante el proceso
            Log.e("loginActivity", "Error in fetchUserRoleAndRedirect: ", e);
        }
    }


    // Método para redirigir al dashboard correspondiente
    private void redirectToDashboard(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, UserDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    // Método para registrar el acceso del usuario
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

    // Método para mostrar un AlertDialog
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    // Método para validar el formato del correo electrónico
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
