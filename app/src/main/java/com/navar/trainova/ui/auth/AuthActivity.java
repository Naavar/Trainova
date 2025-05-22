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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.navar.trainova.R;
import com.navar.trainova.ui.home.HomeActivity;
import com.navar.trainova.ui.survey.SurveyActivity;

/**
 * Actividad de autenticación que maneja el registro, inicio de sesión
 * y autenticación con Google para la aplicación Trainova.
 * Esta actividad permite a los usuarios crear nuevas cuentas, iniciar sesión
 * con credenciales existentes o utilizar su cuenta de Google.
 */
public class AuthActivity extends AppCompatActivity {

    /** Campo de texto para el correo electrónico del usuario. */
    private EditText emailEditText;
    /** Campo de texto para la contraseña del usuario. */
    private EditText passwordEditText;
    /** Botón para iniciar el proceso de registro de un nuevo usuario. */
    private Button signUpButton;
    /** Botón para iniciar el proceso de inicio de sesión de un usuario existente. */
    private Button logInButton;
    /** Botón (ImageView) para iniciar el proceso de autenticación con Google. */
    private ImageView googleSignInButton;
    /** Instancia de Firebase Authentication. */
    private FirebaseAuth mAuth;
    /** Cliente para Google Sign-In. */
    private GoogleSignInClient mGoogleSignInClient;
    /** Lanzador para el flujo de Google Sign-In que maneja el resultado de la actividad de inicio de sesión. */
    private ActivityResultLauncher<Intent> signInLauncher;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

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
                    com.google.android.gms.tasks.Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In fue exitoso, autenticar con Firebase usando el token de Google
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            Toast.makeText(AuthActivity.this, "No se pudo obtener la cuenta de Google.", Toast.LENGTH_SHORT).show();
                        }
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
     * Lanza la intención de inicio de sesión de Google a través del ActivityResultLauncher.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Autentica al usuario en Firebase utilizando un token de ID de Google.
     * @param idToken El token de ID de Google obtenido después de un inicio de sesión exitoso con Google. Puede ser nulo.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(AuthActivity.this, "Token de Google no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(AuthActivity.this, "Autenticación con Google exitosa!",
                        Toast.LENGTH_SHORT).show();

                    if (user != null) {
                        checkIfSurveyCompletedAndNavigate(user.getUid());
                    } else {
                        Toast.makeText(AuthActivity.this, "Error: No se pudo obtener el " +
                                "usuario tras el inicio de sesión.",
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Error en la autenticación con Google en Firebase";
                    if (task.getException() != null && task.getException().getMessage() != null) {
                        errorMessage += ": " + task.getException().getMessage();
                    } else if (task.getException() != null) {
                        errorMessage += ": " + task.getException().getClass().getSimpleName();
                    }
                    Toast.makeText(AuthActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Verifica si el usuario ha completado la encuesta inicial consultando Firestore.
     * Navega a HomeActivity si la encuesta está completada, o a SurveyActivity en caso contrario.
     * @param userId El ID del usuario a verificar.
     */
    private void checkIfSurveyCompletedAndNavigate(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Usuario").document(userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    goToHomeActivity();
                } else {
                    goToSurveyActivity();
                }
            });
    }

    /**
     * Registra un nuevo usuario en Firebase con el correo electrónico y la contraseña proporcionados.
     * Realiza validaciones básicas de los campos de entrada.
     * Si el registro es exitoso, navega a la SurveyActivity.
     */
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingresa un email válido");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AuthActivity.this, "Registro exitoso!",
                        Toast.LENGTH_SHORT).show();
                    goToSurveyActivity();
                } else {
                    Toast.makeText(AuthActivity.this, "Error en el registro: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Inicia sesión de un usuario existente en Firebase con el correo electrónico y
     * la contraseña proporcionados. Realiza validaciones básicas de los campos de entrada.
     * Tras un inicio de sesión exitoso, verifica si la encuesta fue completada para la navegación.
     */
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingresa un email válido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ingresa tu contraseña");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(AuthActivity.this, "Inicio de sesión exitoso!",
                        Toast.LENGTH_SHORT).show();
                    if (user != null) {
                        checkIfSurveyCompletedAndNavigate(user.getUid());
                    } else {
                        Toast.makeText(AuthActivity.this, "Error: No se pudo obtener " +
                            "el usuario.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AuthActivity.this, "Error en el inicio de sesión: " +
                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Navega a la SurveyActivity y finaliza la actividad actual.
     * Se utiliza después de un registro exitoso o si un usuario existente no ha completado la encuesta.
     */
    private void goToSurveyActivity() {
        Intent intent = new Intent(AuthActivity.this, SurveyActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navega a la HomeActivity y finaliza la actividad actual, limpiando la pila de actividades.
     * Se utiliza cuando el usuario ha completado la autenticación y la encuesta.
     */
    private void goToHomeActivity() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Se llama cuando la actividad está a punto de hacerse visible para el usuario.
     * Verifica si hay un usuario de Firebase ya autenticado. Si es así, comprueba
     * si ha completado la encuesta para dirigirlo a la pantalla correspondiente.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Verifica si la encuesta está completada antes de decidir a dónde navegar.
            checkIfSurveyCompletedAndNavigate(currentUser.getUid());
        }
        // Si currentUser es null, la actividad de Auth se muestra normalmente para el login/registro.
    }
}