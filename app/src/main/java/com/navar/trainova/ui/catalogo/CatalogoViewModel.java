package com.navar.trainova.ui.catalogo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.repository.CatalogoRepository;
import com.navar.trainova.data.repository.FirestoreCatalogoRepository;

import java.util.List;

public class CatalogoViewModel extends ViewModel {

    private final CatalogoRepository catalogoRepository;
    private final LiveData<List<CatalogoEvento>> catalogoLiveData;

    public CatalogoViewModel() {
        catalogoRepository = new FirestoreCatalogoRepository();

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // Inicia la carga de datos del catálogo (general + personal)
        catalogoRepository.loadAndObserveCombinedCatalog(uid);
        catalogoLiveData = catalogoRepository.getCatalogLiveData();
    }

    public LiveData<List<CatalogoEvento>> getCatalogLiveData() {
        return catalogoLiveData;
    }

    public void createPersonalTemplate(CatalogoEvento plantilla) {
        catalogoRepository.createPersonalTemplate(plantilla, (success, message) -> {
        });
    }
    public void updatePersonalTemplate(CatalogoEvento plantilla) {
        catalogoRepository.updatePersonalTemplate(plantilla, (success, message) -> {
        });
    }

    public void deletePersonalTemplate(String templateId) {
        catalogoRepository.deletePersonalTemplate(templateId, (success, message) -> {
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        catalogoRepository.removeListeners();
    }
}