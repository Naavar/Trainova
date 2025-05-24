package com.navar.trainova.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.ArrayList;
import java.util.List;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.CatalogoViewHolder> {

    private List<CatalogoEvento> catalogoList = new ArrayList<>();
    private final OnCatalogoActionsListener listener;

    public interface OnCatalogoActionsListener {
        void onAddItemClick(CatalogoEvento plantilla);
        void onItemClick(CatalogoEvento plantilla);
    }

    public CatalogoAdapter(@NonNull OnCatalogoActionsListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_catalogo_evento, parent, false);
        return new CatalogoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogoViewHolder holder, int position) {
        CatalogoEvento plantilla = catalogoList.get(position);
        holder.bind(plantilla, listener);
    }

    @Override
    public int getItemCount() {
        return catalogoList.size();
    }

    public void submitList(List<CatalogoEvento> newList) {
        this.catalogoList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class CatalogoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombre;
        private final TextView tvTipo;
        private final View colorIndicator;
        private final ImageButton btnAdd;

        public CatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreActividadCatalogo);
            tvTipo = itemView.findViewById(R.id.tvTipoActividadCatalogo);
            colorIndicator = itemView.findViewById(R.id.viewColorIndicatorCatalogo);
            btnAdd = itemView.findViewById(R.id.btnAddDesdeCatalogo);
        }

        public void bind(final CatalogoEvento plantilla, final OnCatalogoActionsListener listener) {
            tvNombre.setText(plantilla.getNombreEvento());
            tvTipo.setText(plantilla.getTipoEvento());
            colorIndicator.setBackgroundColor(plantilla.getColorEvento());

            btnAdd.setOnClickListener(v -> listener.onAddItemClick(plantilla));
            itemView.setOnClickListener(v -> listener.onItemClick(plantilla));
        }
    }
}