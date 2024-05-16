package iesmm.pmdm.autolabibscan.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import iesmm.pmdm.autolabibscan.R;

public class loginActivity extends AppCompatActivity {
    // Declaración de variables
    private Button btnLogin;
    private ImageButton btnGoogle;
    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 20;
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

        //Accion onClick en redirigir al registro
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
        dataBase = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

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

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            }catch (Exception e){
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", e.getMessage());
            }

        }
    }

    private void firebaseAuth(String idToken) {

        AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();

                            HashMap<String,Object> map = new HashMap<>();
                            map.put("id",user.getUid());
                            map.put("name",user.getDisplayName());
                            map.put("profile",user.getPhotoUrl().toString());

                            dataBase.getReference().child("users").child(user.getUid()).setValue(map);

                            Intent intent = new Intent(loginActivity.this, userDashboardActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(loginActivity.this,"error, task auth fallida",Toast.LENGTH_SHORT).show();
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
                            startActivity(new Intent(loginActivity.this, userDashboardActivity.class));
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
