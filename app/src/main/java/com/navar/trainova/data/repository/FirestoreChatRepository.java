package com.navar.trainova.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.navar.trainova.ui.ia.IaActivity.Message;
import java.util.List;

/**
 * Implementación de ChatRepository que utiliza Cloud Firestore como fuente de datos.
 * Gestiona la comunicación con la subcolección "mensajes" de cada usuario.
 */
public class FirestoreChatRepository implements ChatRepository {

    private static final String TAG = "FirestoreChatRepo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Message>> chatHistoryLiveData = new MutableLiveData<>();
    private ListenerRegistration chatListener;

    /**
     * {@inheritDoc}
     * Se conecta a la subcolección 'mensajes' del usuario en Firestore y escucha
     * cambios en tiempo real. Los mensajes se ordenan por su marca de tiempo.
     */
    @Override
    public LiveData<List<Message>> getChatHistory(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "El ID de usuario es nulo o vacío. No se puede obtener el historial.");
            return chatHistoryLiveData;
        }

        // Si ya hay un listener, lo quitamos antes de crear uno nuevo.
        removeListener();

        chatListener = db.collection("Usuario").document(userId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al escuchar el historial del chat.", error);
                    return;
                }
                if (snapshots != null) {
                    List<Message> messages = snapshots.toObjects(Message.class);
                    chatHistoryLiveData.setValue(messages);
                }
            });
        return chatHistoryLiveData;
    }

    /**
     * {@inheritDoc}
     * Añade un nuevo documento a la subcolección 'mensajes' del usuario.
     * La marca de tiempo es añadida automáticamente por Firestore gracias a la anotación @ServerTimestamp.
     */
    @Override
    public void saveMessage(String userId, Message message) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "El ID de usuario es nulo o vacío. No se puede guardar el mensaje.");
            return;
        }

        db.collection("Usuario").document(userId).collection("mensajes")
            .add(message)
            .addOnSuccessListener(documentReference -> Log.d(TAG, "Mensaje guardado con ID: " +
                documentReference.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Error al guardar el mensaje.", e));
    }

    /**
     * {@inheritDoc}
     * Quita el listener de Firestore para prevenir memory leaks.
     */
    @Override
    public void removeListener() {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }
    }
}