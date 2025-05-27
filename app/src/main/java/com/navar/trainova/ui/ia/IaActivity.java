package com.navar.trainova.ui.ia;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.Gson;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.model.EjercicioPlantilla;
import com.navar.trainova.data.repository.CatalogoRepository;
import com.navar.trainova.data.repository.ChatRepository;
import com.navar.trainova.data.repository.FirestoreCatalogoRepository;
import com.navar.trainova.data.repository.FirestoreChatRepository;
import com.navar.trainova.ui.adapters.ChatAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Gestiona la interfaz de chat con el asistente de IA.
 * Esta actividad se encarga de:
 * 1. Mostrar el historial de chat persistente desde Firestore.
 * 2. Enviar las preguntas del usuario a un servicio de IA.
 * 3. Obtener datos del usuario desde Firebase para dar contexto a la IA.
 * 4. Procesar respuestas de texto y sugerencias interactivas de la IA.
 * 5. Guardar la conversación completa en Firestore.
 * 6. Guardar las rutinas aceptadas en el catálogo del usuario.
 */
public class IaActivity extends AppCompatActivity implements ChatAdapter.SuggestionInteractionListener {

    private static final String TAG_AI_DEBUG = "AI_DEBUG";
    private static final String TAG_API_ERROR = "API_ERROR";
    private static final String TAG_API_FAILURE = "API_FAILURE";
    private static final String TAG_COLOR_DEBUG = "ColorDebug";

    private RecyclerView recyclerViewChat;
    private EditText etChatMessage;
    private ImageButton btnSendMessage;
    private ChatAdapter chatAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private OpenAiApiService apiService;
    private ChatRepository chatRepository;
    private CatalogoRepository catalogoRepository;

    private String currentUserId;
    private static final String BASE_URL = "https://openai-proxy.trainova.workers.dev/";
    private String AUTH_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ia);

        AUTH_KEY = getResources().getString(R.string.auth_key_secret);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        initRepositoriesAndAuth();
        setupRecyclerView();
        setupRetrofit();
        setupSendButton();

        if (currentUserId != null) {
            observeChatHistory();
        }
    }

    private void initRepositoriesAndAuth() {
        chatRepository = new FirestoreChatRepository();
        catalogoRepository = new FirestoreCatalogoRepository();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, getString(R.string.usuario_no_autenticado), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>(), this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiService = retrofit.create(OpenAiApiService.class);
    }

    private void setupSendButton() {
        btnSendMessage.setOnClickListener(v -> {
            String userQuestion = etChatMessage.getText().toString().trim();
            if (!userQuestion.isEmpty() && currentUserId != null) {
                chatRepository.saveMessage(currentUserId, new Message("user", userQuestion));
                etChatMessage.setText("");
                fetchUserDataAndAskAI(userQuestion);
            }
        });
    }

    private void observeChatHistory() {
        chatRepository.getChatHistory(currentUserId).observe(this, messages -> {
            chatAdapter.updateMessages(messages);
            if (messages != null && !messages.isEmpty()) {
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });
    }

    /**
     * Orquesta el proceso de obtener los datos del usuario y realizar la consulta a la IA.
     * @param userQuestion La pregunta que el usuario ha escrito en el chat.
     */
    private void fetchUserDataAndAskAI(String userQuestion) {
        addMessageToChat("assistant", getString(R.string.ia_pensando));
        DocumentReference userDocRef = db.collection("Usuario").document(currentUserId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> userData = documentSnapshot.getData();
                String prompt = buildPrompt(userData, userQuestion);

                List<Message> promptMessages = new ArrayList<>();
                promptMessages.add(new Message("system", prompt));

                ChatRequest request = new ChatRequest("gpt-3.5-turbo", promptMessages);
                callOpenAiApi(request);
            } else {
                removeThinkingMessage();
                Toast.makeText(this, getString(R.string.error_datos_usuario_no_encontrados),
                    Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            removeThinkingMessage();
            Toast.makeText(this, getString(R.string.error_obtener_datos_usuario, e.getMessage()),
                Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Construye el prompt del sistema para la IA, pidiéndole que genere un JSON estructurado
     * que coincida con el modelo CatalogoEvento, incluyendo un nombre de color de una lista predefinida.
     * @param userData Mapa con los datos del usuario.
     * @param question La pregunta específica del usuario.
     * @return Un String con el prompt completo para enviar como mensaje de sistema.
     */
    private String buildPrompt(Map<String, Object> userData, String question) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Eres un entrenador personal experto llamado Trainova. Tu objetivo es crear plantillas de rutinas de ejercicios detalladas para el usuario.");
        promptBuilder.append("Analiza los datos del usuario y su petición. Luego, debes responder ÚNICAMENTE con un objeto JSON. No añadas explicaciones fuera del JSON.");
        promptBuilder.append("El JSON debe tener la siguiente estructura y tipos de dato:\n");
        promptBuilder.append("{\"justificacionIA\": \"Un texto breve y motivador explicando la sugerencia de la rutina.\", ");
        promptBuilder.append("\"plantillaSugerida\": {");
        promptBuilder.append("\"nombre\": \"string (nombre de la rutina)\", ");
        promptBuilder.append("\"descripcion\": \"string (descripción de la rutina)\", ");
        promptBuilder.append("\"duracion\": \"string (ej: 45 minutos, 1 hora)\", ");
        promptBuilder.append("\"tipo\": \"string (ej: Entrenamiento de Fuerza, Cardio, Flexibilidad)\", ");
        promptBuilder.append("\"nombreColor\": \"string (elige UNO de los siguientes nombres de color EXACTAMENTE: Naranja, Rojo, Verde, Azul, Lila, Amarillo, Cian, Rosa, Coral, Lima)\", ");
        promptBuilder.append("\"ejercicios\": [");
        promptBuilder.append("{\"nombreEjercicio\": \"string\", \"series\": integer, \"repeticiones\": \"string (ej: 10-12, Al fallo)\", \"descanso\": \"string (ej: 60s, 90s)\", \"notas\": \"string (opcional, consejos adicionales)\"}");
        promptBuilder.append("]}}");
        promptBuilder.append("\n\n--- DATOS DEL USUARIO (si son relevantes) ---\n");
        for (Map.Entry<String, Object> entry : Objects.requireNonNull(userData).entrySet()) {
            promptBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }
        promptBuilder.append("\n--- PETICIÓN DEL USUARIO ---\n'");
        promptBuilder.append(question).append("'\n");
        promptBuilder.append("--- FIN --- \nAhora, genera el JSON detallado incluyendo al menos 2-3 ejercicios si es apropiado para la petición.");
        return promptBuilder.toString();
    }

    /**
     * Realiza la llamada asíncrona a la API de OpenAI a través del proxy de Cloudflare.
     * Intenta interpretar la respuesta como un JSON de sugerencia; si falla, la trata como texto normal.
     * Convierte el nombre de color recibido de la IA a un entero de color de Android.
     * @param request El objeto ChatRequest que contiene el modelo y los mensajes.
     */
    private void callOpenAiApi(ChatRequest request) {
        apiService.getChatCompletion(AUTH_KEY, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call,
                                   @NonNull Response<ChatResponse> response) {
                removeThinkingMessage();
                if (response.isSuccessful() && response.body() != null && !response.body()
                    .choices.isEmpty()) {
                    String jsonResponse = response.body().choices.get(0).message.content;
                    Log.d(TAG_AI_DEBUG, "JSON crudo recibido de la IA: " + jsonResponse);
                    try {
                        Gson gson = new Gson();
                        RecomendacionIA recomendacion = gson.fromJson(jsonResponse, RecomendacionIA.class);

                        if (recomendacion != null && recomendacion.plantillaSugerida != null) {
                            Log.d(TAG_AI_DEBUG, "Parseo Gson - Nombre Plantilla: "
                                + recomendacion.plantillaSugerida.getNombreEvento());
                            Log.d(TAG_AI_DEBUG, "Parseo Gson - NombreColor de IA: "
                                + recomendacion.plantillaSugerida.getNombreColor());

                            int colorResuelto = convertirNombreColorAEntero(recomendacion
                                .plantillaSugerida.getNombreColor());
                            recomendacion.plantillaSugerida.setColorEvento(colorResuelto);
                            Log.d(TAG_COLOR_DEBUG, "Color resuelto para plantilla (int): "
                                + colorResuelto + ", (hex): #" + Integer.toHexString(colorResuelto));

                            if (recomendacion.plantillaSugerida.getEjercicios() != null) {
                                Log.d(TAG_AI_DEBUG, "Parseo Gson - Número de ejercicios parseados: "
                                    + recomendacion.plantillaSugerida.getEjercicios().size());
                                for (EjercicioPlantilla ej : recomendacion.plantillaSugerida.getEjercicios()) {
                                    Log.d(TAG_AI_DEBUG, "Parseo Gson - Ejercicio: " +
                                        (ej != null ? ej.getNombreEjercicio() : "ejercicio nulo en lista"));
                                }
                            } else {
                                Log.d(TAG_AI_DEBUG, "Parseo Gson - La lista de ejercicios en plantillaSugerida es NULL.");
                            }
                            chatRepository.saveMessage(currentUserId, new Message("assistant", recomendacion));
                        } else {
                            Log.e(TAG_AI_DEBUG, "Recomendacion o plantillaSugerida es null después del parseo, guardando JSON crudo como texto.");
                            chatRepository.saveMessage(currentUserId, new Message("assistant", jsonResponse));
                        }
                    } catch (Exception e) {
                        Log.e(TAG_AI_DEBUG, "Error al parsear JSON o JSON incompleto. Guardando como texto. Error: "
                            + e.getMessage(), e);
                        chatRepository.saveMessage(currentUserId, new Message("assistant", jsonResponse));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e(TAG_API_ERROR, "Code: " + response.code() + " Body: " + errorBody);
                        chatRepository.saveMessage(currentUserId, new Message("assistant", getString(R.string.ia_error_respuesta_generica, response.code())));
                    } catch (IOException e) {
                        Log.e(TAG_API_ERROR, "Error al parsear el cuerpo del error", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG_API_FAILURE, "Error en la llamada a la API", t);
                removeThinkingMessage();
                chatRepository.saveMessage(currentUserId, new Message("assistant", getString(R.string.ia_error_conexion)));
            }
        });
    }

    /**
     * Convierte un nombre de color (String) a su valor entero ARGB de Android.
     * Utiliza los colores definidos en R.color.
     * @param nombreColor El nombre del color (ej. "Naranja", "Rojo").
     * @return El valor entero del color ARGB, o un color por defecto si el nombre no se reconoce.
     */
    private int convertirNombreColorAEntero(String nombreColor) {
        if (nombreColor == null) {
            Log.w(TAG_COLOR_DEBUG, "Nombre de color es null. Usando color por defecto Azul.");
            return ContextCompat.getColor(this, R.color.Azul);
        }
        switch (nombreColor.toLowerCase().trim()) {
            case "naranja": return ContextCompat.getColor(this, R.color.Naranja);
            case "rojo": return ContextCompat.getColor(this, R.color.Rojo);
            case "verde": return ContextCompat.getColor(this, R.color.Verde);
            case "azul": return ContextCompat.getColor(this, R.color.Azul);
            case "lila": return ContextCompat.getColor(this, R.color.Lila);
            case "amarillo": return ContextCompat.getColor(this, R.color.Amarillo);
            case "cian": return ContextCompat.getColor(this, R.color.Cian);
            case "rosa": return ContextCompat.getColor(this, R.color.Rosa);
            case "coral": return ContextCompat.getColor(this, R.color.Coral);
            case "lima": return ContextCompat.getColor(this, R.color.Lima);
            default:
                Log.w(TAG_COLOR_DEBUG, "Nombre de color no reconocido de la IA: '" +
                    nombreColor + "'. Usando color por defecto Azul.");
                return ContextCompat.getColor(this, R.color.Azul);
        }
    }

    @Override
    public void onAcceptSuggestion(CatalogoEvento plantilla) {
        Log.d(TAG_AI_DEBUG, "onAcceptSuggestion - Plantilla recibida: " +
            (plantilla != null ? plantilla.getNombreEvento() : "plantilla NULL"));
        if (plantilla != null) {
            Log.d(TAG_COLOR_DEBUG, "onAcceptSuggestion - Color de plantilla (int): "
                + plantilla.getColorEvento() + ", (hex): #" + Integer.toHexString(plantilla.getColorEvento()));
            if (plantilla.getEjercicios() != null) {
                Log.d(TAG_AI_DEBUG, "onAcceptSuggestion - Número de ejercicios a guardar: "
                    + plantilla.getEjercicios().size());
                for (EjercicioPlantilla ej : plantilla.getEjercicios()) {
                    Log.d(TAG_AI_DEBUG, "onAcceptSuggestion - Ejercicio: "
                        + (ej != null ? ej.getNombreEjercicio() : "ejercicio nulo en lista"));
                }
            } else {
                Log.d(TAG_AI_DEBUG, "onAcceptSuggestion - La lista de ejercicios en " +
                    "la plantilla es NULL antes de llamar al repositorio.");
            }
        } else {
            Log.e(TAG_AI_DEBUG, "onAcceptSuggestion - La plantilla entera es NULL." +
                " No se puede guardar.");
            Toast.makeText(this, getString(R.string.error_procesar_sugerencia),
                Toast.LENGTH_SHORT).show();
            return;
        }

        catalogoRepository.createPersonalTemplate(plantilla, (success, message) -> {
            if (success) {
                Toast.makeText(this, getString(R.string.rutina_guardada_catalogo),
                    Toast.LENGTH_SHORT).show();
                chatRepository.saveMessage(currentUserId, new Message("assistant",
                    getString(R.string.ia_confirmacion_rutina_guardada, plantilla.getNombreEvento())));
            } else {
                Toast.makeText(this, getString(R.string.error_guardar_plantilla, message), Toast.LENGTH_SHORT).show();
                Log.e(TAG_AI_DEBUG, "Error al guardar plantilla en Firestore: " + message);
            }
        });
    }

    @Override
    public void onDeclineSuggestion() {
        Toast.makeText(this, getString(R.string.sugerencia_descartada), Toast.LENGTH_SHORT).show();
        chatRepository.saveMessage(currentUserId, new Message("assistant",
            getString(R.string.ia_confirmacion_sugerencia_descartada)));
    }

    private void addMessageToChat(String role, String content) {
        if (currentUserId != null) {
            chatRepository.saveMessage(currentUserId, new Message(role, content));
        }
    }

    private void removeThinkingMessage() {
        if (chatAdapter != null) {
            chatAdapter.removeThinkingMessage();
        }
    }

    public interface OpenAiApiService {
        @POST(".")
        Call<ChatResponse> getChatCompletion(
            @Header("x-auth-key") String authKey,
            @Body ChatRequest requestBody
        );
    }

    public static class ChatRequest {
        public String model;
        public List<Message> messages;
        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    public static class Message {
        public String role;
        public String content;
        public RecomendacionIA sugerencia;
        @ServerTimestamp
        public Date timestamp;

        public Message() {}
        public Message(String role, String content) { this.role = role; this.content = content; }
        public Message(String role, RecomendacionIA sugerencia) { this.role = role;
            this.sugerencia = sugerencia; }
    }

    public static class RecomendacionIA {
        public String justificacionIA;
        public CatalogoEvento plantillaSugerida;
    }

    public static class ChatResponse {
        public List<Choice> choices;
        public static class Choice {
            public Message message;
        }
    }
}