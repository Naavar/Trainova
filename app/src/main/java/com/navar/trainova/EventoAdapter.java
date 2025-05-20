package com.navar.trainova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.activity.HomeActivity;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.ViewHolder> {

    private final List<String> nombreEventos;
    private final List<HomeActivity.Evento> eventos;
    private OnEventoClickListener listener;

    // Interfaz para el callback de clic
    public interface OnEventoClickListener {
        void onEventoClick(HomeActivity.Evento evento, int position);
    }

    // Constructor actualizado para pasar eventos completos, no solo nombres
    public EventoAdapter(List<String> nombreEventos, List<HomeActivity.Evento> eventos) {
        this.nombreEventos = nombreEventos;
        this.eventos = eventos;
    }

    public void setOnEventoClickListener(OnEventoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(nombreEventos.get(position));

        // Color del texto según el color del evento
        if (eventos != null && position < eventos.size()) {
            holder.textView.setTextColor(eventos.get(position).getColor());
        }

        // Configurar clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && eventos != null && position < eventos.size()) {
                listener.onEventoClick(eventos.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nombreEventos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}