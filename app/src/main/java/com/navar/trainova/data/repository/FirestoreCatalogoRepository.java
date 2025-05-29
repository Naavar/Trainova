package com.navar.trainova.data.repository;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.ArrayList;
import java.util.List;

public class FirestoreCatalogoRepository implements CatalogoRepository {
    private static final String TAG = "FirestoreCatalogoRepo";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final MutableLiveData<List<CatalogoEvento>> combinedCatalogLiveData = new MutableLiveData<>();

    private ListenerRegistration generalCatalogListener;
    private ListenerRegistration personalCatalogListener;

    private List<CatalogoEvento> generalCatalogList = new ArrayList<>();
    private List<CatalogoEvento> personalCatalogList = new ArrayList<>();

    public FirestoreCatalogoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadAndObserveCombinedCatalog(@Nullable String uid) {
        removeListeners();

        generalCatalogListener = db.collection("catalogoGeneral")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error escuchando catalogoGeneral", e);
                    return;
                }
                if (snapshots != null) {
                    List<CatalogoEvento> tempList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        CatalogoEvento evento = doc.toObject(CatalogoEvento.class);
                        if (evento != null) {
                            evento.setId(doc.getId());
                            tempList.add(evento);
                        }
                    }
                    generalCatalogList = tempList;
                    Log.d(TAG, "CatalogoGeneral actualizado con " + generalCatalogList.size() + " plantillas.");
                    combineAndPostResults();
                }
            });

        if (uid != null && !uid.isEmpty()) {
            personalCatalogListener = db.collection("Usuario").document(uid)
                .collection("catalogoEvento")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando catalogoEvento", e);
                        return;
                    }
                    if (snapshots != null) {
                        List<CatalogoEvento> tempList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            CatalogoEvento evento = doc.toObject(CatalogoEvento.class);
                            if (evento != null) {
                                evento.setId(doc.getId());
                                tempList.add(evento);
                            }
                        }
                        personalCatalogList = tempList;
                        Log.d(TAG, "CatalogoEvento actualizado para " + uid + " con " + personalCatalogList.size() + " plantillas.");
                        combineAndPostResults();
                    }
                });
        } else {
            personalCatalogList.clear();
            combineAndPostResults();
        }
    }

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

        newTemplate.setUid(uid);

        db.collection("Usuario").document(uid).collection("catalogoEvento")
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
    public void updatePersonalTemplate(CatalogoEvento template, SimpleCallback callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onResult(false, "Usuario no autenticado.");
            return;
        }
        if (template.getId() == null || template.getId().isEmpty()) {
            callback.onResult(false, "ID de plantilla inválido para actualizar.");
            return;
        }

        db.collection("Usuario").document(uid)
            .collection("catalogoEvento").document(template.getId())
            .set(template)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Plantilla personal actualizada: " + template.getId());
                callback.onResult(true, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error actualizando plantilla personal", e);
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

        db.collection("Usuario").document(uid).collection("catalogoEvento").document(templateId)
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