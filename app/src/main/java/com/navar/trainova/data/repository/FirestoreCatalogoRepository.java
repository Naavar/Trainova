package com.navar.trainova.data.repository;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.ArrayList;
import java.util.List;
// TODO cuando edito realmente no edita, crea una con los datos nuevos que he puesto duplicandose
public class FirestoreCatalogoRepository implements CatalogoRepository {
    private static final String TAG = "FirestoreCatalogoRepo";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // LiveData que expondremos a la UI
    private final MutableLiveData<List<CatalogoEvento>> combinedCatalogLiveData = new MutableLiveData<>();

    // Listeners para poder removerlos después
    private ListenerRegistration generalCatalogListener;
    private ListenerRegistration personalCatalogListener;

    // Listas para almacenar los resultados de cada listener por separado
    private List<CatalogoEvento> generalCatalogList = new ArrayList<>();
    private List<CatalogoEvento> personalCatalogList = new ArrayList<>();

    public FirestoreCatalogoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadAndObserveCombinedCatalog(@Nullable String uid) {
        // Limpiamos listeners anteriores para evitar duplicados
        removeListeners();

        generalCatalogListener = db.collection("catalogoGeneral")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error escuchando catalogoGeneral", e);
                    return;
                }
                if (snapshots != null) {
                    generalCatalogList = snapshots.toObjects(CatalogoEvento.class);
                    Log.d(TAG, "CatalogoGeneral actualizado con " + generalCatalogList.size() + " plantillas.");
                    combineAndPostResults(); // Combinar y notificar
                }
            });

        if (uid != null && !uid.isEmpty()) {
            personalCatalogListener = db.collection("Usuario").document(uid)
                .collection("catalogoPersonal")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando catalogoPersonal", e);
                        return;
                    }
                    if (snapshots != null) {
                        personalCatalogList = snapshots.toObjects(CatalogoEvento.class);
                        Log.d(TAG, "CatalogoPersonal actualizado para " + uid + " con " + personalCatalogList.size() + " plantillas.");
                        combineAndPostResults(); // Combinar y notificar
                    }
                });
        } else {
            // Si no hay usuario, la lista personal está vacía
            personalCatalogList.clear();
            combineAndPostResults();
        }
    }

    /**
     * Método auxiliar que une las dos listas y actualiza el LiveData.
     * Se llama cada vez que cualquiera de los dos listeners recibe datos nuevos.
     */
    private void combineAndPostResults() {
        List<CatalogoEvento> combinedList = new ArrayList<>();
        combinedList.addAll(generalCatalogList);
        combinedList.addAll(personalCatalogList);
        combinedCatalogLiveData.postValue(combinedList);
    }

    @Override
    public LiveData<List<CatalogoEvento>> getCatalogLiveData() {
        return combinedCatalogLiveData;
    }

    @Override
    public void createPersonalTemplate(CatalogoEvento newTemplate, SimpleCallback callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onResult(false, "Usuario no autenticado.");
            return;
        }

        newTemplate.setUidCreador(uid); // Asignar el dueño

        db.collection("Usuario").document(uid).collection("catalogoPersonal")
            .add(newTemplate)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "Plantilla personal creada con ID: " + docRef.getId());
                callback.onResult(true, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creando plantilla personal", e);
                callback.onResult(false, e.getMessage());
            });
    }

    @Override
    public void deletePersonalTemplate(String templateId, SimpleCallback callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onResult(false, "Usuario no autenticado.");
            return;
        }
        if (templateId == null || templateId.isEmpty()) {
            callback.onResult(false, "ID de plantilla inválido.");
            return;
        }

        db.collection("usuarios").document(uid).collection("catalogoPersonal").document(templateId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Plantilla personal borrada: " + templateId);
                callback.onResult(true, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error borrando plantilla personal", e);
                callback.onResult(false, e.getMessage());
            });
    }

    @Override
    public void removeListeners() {
        if (generalCatalogListener != null) {
            generalCatalogListener.remove();
            generalCatalogListener = null;
        }
        if (personalCatalogListener != null) {
            personalCatalogListener.remove();
            personalCatalogListener = null;
        }
        Log.d(TAG, "Listeners de catálogo removidos.");
    }
}