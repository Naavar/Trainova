package com.navar.trainova.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.List;

public interface CatalogoRepository {

    /**
     * Comienza a observar los catálogos (general y personal del usuario)
     * y devuelve un LiveData con la lista combinada.
     * @param uid El ID del usuario actual. Si es nulo, solo se observará el catálogo general.
     */
    void loadAndObserveCombinedCatalog(@Nullable String uid);

    /**
     * Devuelve el LiveData que la UI puede observar para obtener la lista de plantillas.
     */
    LiveData<List<CatalogoEvento>> getCatalogLiveData();

    /**
     * Crea una nueva plantilla en el catálogo personal del usuario.
     * @param newTemplate El objeto de la plantilla a crear.
     * @param callback Para notificar el resultado de la operación.
     */
    void createPersonalTemplate(CatalogoEvento newTemplate, SimpleCallback callback);

    /**
     * Actualiza una plantilla existente en el catálogo personal del usuario.
     * @param template El objeto de la plantilla con los datos actualizados. Debe contener el ID de la plantilla a modificar.
     * @param callback Para notificar el resultado de la operación.
     */
    void updatePersonalTemplate(CatalogoEvento template, SimpleCallback callback);

    /**
     * Borra una plantilla del catálogo personal del usuario.
     * @param templateId El ID de la plantilla a borrar.
     * @param callback Para notificar el resultado de la operación.
     */
    void deletePersonalTemplate(String templateId, SimpleCallback callback);

    /**
     * Detiene todos los listeners para evitar fugas de memoria.
     * Llamar cuando el ViewModel se destruye o el usuario hace logout.
     */
    void removeListeners();

    /** Interfaz para callbacks sencillos */
    interface SimpleCallback {
        void onResult(boolean success, String message);
    }
}