package com.navar.trainova.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.navar.trainova.R;
import com.navar.trainova.ui.home.HomeActivity;

/**
 * Actividad de autenticación que maneja el registro, inicio de sesión
 * y autenticación con Google para la aplicación Trainova.
 * Esta actividad permite a los usuarios crear nuevas cuentas, iniciar sesión
 * con credenciales existentes o utilizar su cuenta de Google.
 */
public class AuthActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signUpButton, logInButton;
    private ImageView googleSignInButton;
    private FirebaseAuth mAuth; // Instancia de Firebase Authentication
    private GoogleSignInClient mGoogleSignInClient; // Cliente para Google Sign-In
    private ActivityResultLauncher<Intent> signInLauncher; // Lanzador para el flujo de Google Sign-In

    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Aquí se inicializan las vistas, se configura Firebase Auth y Google Sign-In,
     * y se establecen los listeners para los botones.
     * @param savedInstanceState Si la actividad se está recreando después de un
     * cambio de configuración, este Bundle contiene los datos más recientes de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilita el modo de pantalla completa (EdgeToEdge)
        setContentView(R.layout.activity_auth); // Establece el layout de la actividad

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Enlazar vistas del layout con las variables de la actividad
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        logInButton = findViewById(R.id.logInButton);
        googleSignInButton = findViewById(R.id.imageView2);

        // Configurar el padding para manejar los insets del sistema (barras de estado/navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar las opciones y el cliente para Google Sign-In
        configureGoogleSignIn();

        // Registrar el ActivityResultLauncher para manejar el resultado del inicio de sesión con Google
        signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In fue exitoso, autenticar con Firebase usando el token de Google
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Google Sign In falló, muestra un mensaje de error
                        Toast.makeText(AuthActivity.this, "Error en el inicio de sesión con Google: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });

        // Establecer listeners para los botones de registro, acceso y Google
        signUpButton.setOnClickListener(v -> registerUser());
        logInButton.setOnClickListener(v -> loginUser());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Configura las opciones de Google Sign-In, incluyendo la solicitud del token de ID
     * y el correo electrónico, y luego inicializa el cliente de Google Sign-In.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Solicita el token de ID para Firebase
            .requestEmail() // Solicita el correo electrónico del usuario
            .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Inicia el flujo de autenticación con Google.
     * Lanza la intención de inicio de sesión de Google a través del {@link ActivityResultLauncher}.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Autentica al usuario en Firebase utilizando un token de ID de Google.
     * @param idToken El token de ID de Google obtenido después de un inicio de sesión exitoso con Google.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null); // Crea credencial de Firebase con el token de Google
        mAuth.signInWithCredential(credential) // Intenta iniciar sesión en Firebase con la credencial
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete( Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Si la autenticación en Firebase fue exitosa
                        FirebaseUser user = mAuth.getCurrentUser(); // Obtiene el usuario actual
                        Toast.makeText(AuthActivity.this, "Autenticación con Google exitosa!",
                            Toast.LENGTH_SHORT).show();
                        goToHomeActivity(); // Navega a la actividad principal
                    } else {
                        // Si la autenticación en Firebase falló
                        Toast.makeText(AuthActivity.this, "Error en la autenticación con Google: " +
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /**
     * Registra un nuevo usuario en Firebase con el correo electrónico y la contraseña proporcionados.
     * Realiza validaciones básicas de los campos de entrada.
     */
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validaciones de entrada
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingresa un email válido");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Intenta crear un nuevo usuario en Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Registro exitoso
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(AuthActivity.this, "Registro exitoso!", Toast.LENGTH_SHORT).show();
                    goToHomeActivity(); // Navega a la actividad principal
                } else {
                    // Error en el registro, muestra el mensaje de excepción
                    Toast.makeText(AuthActivity.this, "Error en el registro: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Inicia sesión de un usuario existente en Firebase con el correo electrónico y la contraseña proporcionados.
     * Realiza validaciones básicas de los campos de entrada.
     */
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validaciones de entrada
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingresa un email válido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ingresa tu contraseña");
            return;
        }

        // Intenta iniciar sesión en Firebase
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Inicio de sesión exitoso
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(AuthActivity.this, "Inicio de sesión exitoso!",
                        Toast.LENGTH_SHORT).show();
                    goToHomeActivity(); // Navega a la actividad principal
                } else {
                    // Error en el inicio de sesión, muestra el mensaje de excepción
                    Toast.makeText(AuthActivity.this, "Error en el inicio de sesión: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Navega a la {@link HomeActivity} y finaliza la actividad actual.
     */
    private void goToHomeActivity() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Finaliza esta actividad para que el usuario no pueda volver a ella con el botón de atrás
    }

    /**
     * Se llama cuando la actividad está a punto de hacerse visible para el usuario.
     * En este método, se verifica si un usuario ya ha iniciado sesión en Firebase.
     * Si es así, se redirige directamente a la {@link HomeActivity}.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya ha iniciado sesión
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHomeActivity(); // Si ya hay un usuario logueado, ir a la pantalla principal
        }
    }
}