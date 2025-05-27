package com.navar.trainova.data.repository;

import androidx.lifecycle.LiveData;
import com.navar.trainova.ui.ia.IaActivity.Message;
import java.util.List;

/**
 * Define el contrato para la gestión de datos de los mensajes de un chat.
 * Esta interfaz abstrae la fuente de datos (Firestore, base de datos local, etc.),
 * permitiendo que la lógica de la aplicación no dependa de la implementación específica.
 */
public interface ChatRepository {

    /**
     * Obtiene el historial completo de mensajes para un usuario específico.
     * Devuelve un objeto LiveData que permite a la UI observar los cambios en tiempo real
     * y actualizarse automáticamente cuando se añaden nuevos mensajes.
     *
     * @param userId El ID único del usuario cuyo historial de chat se quiere obtener.
     * @return Un LiveData que contiene la lista de mensajes del chat.
     */
    LiveData<List<Message>> getChatHistory(String userId);

    /**
     * Guarda un nuevo mensaje en el historial del chat del usuario.
     * La implementación se encargará de añadir los datos necesarios, como la marca de tiempo.
     *
     * @param userId El ID único del usuario al que pertenece el mensaje.
     * @param message El objeto Message que se va a guardar.
     */
    void saveMessage(String userId, Message message);

    /**
     * Detiene la escucha de cambios en la base de datos.
     * Es importante llamar a este método cuando la vista asociada se destruye
     * para evitar fugas de memoria (memory leaks).
     */
    void removeListener();
}