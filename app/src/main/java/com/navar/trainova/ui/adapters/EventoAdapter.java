package com.navar.trainova.ui.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.R;
import com.navar.trainova.data.model.Evento;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar una lista de objetos Evento en un RecyclerView.
 * Este adaptador se encarga de inflar las vistas de los elementos de la lista y de vincular
 * los datos de cada Evento con su correspondiente vista.
 * También gestiona los clics en los elementos de la lista a través de un listener.
 */
public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.ViewHolder> {

    private List<Evento> eventosList;
    private final OnEventoClickListener listener;

    /**
     * Interfaz para el callback de clic en un evento.
     * Permite que otras clases reaccionen cuando un usuario hace clic en un elemento de la lista.
     */
    public interface OnEventoClickListener {
        /**
         * Se invoca cuando se hace clic en un elemento de la lista.
         * @param evento El objeto Evento que fue clicado.
         */
        void onEventoClick(Evento evento);
    }

    /**
     * Constructor del adaptador.
     * @param eventos La lista de objetos Evento a mostrar.
     * @param listener El listener para manejar los clics en los eventos.
     */
    public EventoAdapter(@NonNull List<Evento> eventos, @NonNull OnEventoClickListener listener) {
        this.eventosList = new ArrayList<>(eventos);
        this.listener = listener;
    }

    /**
     * Crea nuevas vistas (invocado por el layout manager).
     * @param parent El ViewGroup en el que se añadirá la nueva View una vez esté adjunta a una
     * posición de adaptador.
     * @param viewType El tipo de vista de la nueva View.
     * @return Un nuevo ViewHolder que contiene la View para el elemento de la lista.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_evento_bottomsheet, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Reemplaza el contenido de una vista (invocado por el layout manager).
     * Este método asocia los datos de un Evento específico con la vista de un elemento.
     * @param holder El ViewHolder que debe ser actualizado para representar el contenido
     * del elemento en la posición dada.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Evento currentEvento = eventosList.get(position);

        String nombre = currentEvento.getNombreMostrado();
        int colorDelEventoOriginal = currentEvento.getColor();

        Log.d("EventoAdapterDebug", "Binding evento: " + nombre +
            ", Color Original (int): " + colorDelEventoOriginal + ", Color Original (hex): #" +
            Integer.toHexString(colorDelEventoOriginal));

        holder.tvNombreEvento.setText(nombre != null ? nombre : "Evento sin nombre");

        holder.viewColorIndicator.setBackgroundColor(colorDelEventoOriginal);
        holder.tvNombreEvento.setTextColor(Color.WHITE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventoClick(currentEvento);
            }
        });
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos que el adaptador representa.
     * @return El número total de eventos en la lista.
     */
    @Override
    public int getItemCount() {
        return eventosList != null ? eventosList.size() : 0;
    }

    /**
     * Actualiza la lista de eventos que el adaptador está mostrando y notifica al RecyclerView
     * que los datos han cambiado.
     * @param nuevosEventos La nueva lista de eventos a mostrar.
     */
    public void submitList(List<Evento> nuevosEventos) {
        this.eventosList = new ArrayList<>(nuevosEventos);
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado.
    }

    /**
     * Proporciona una referencia a las vistas para cada elemento de datos.
     * Un ViewHolder describe la vista de un elemento y los metadatos sobre su lugar
     * dentro del RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View viewColorIndicator;
        public TextView tvNombreEvento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorIndicator = itemView.findViewById(R.id.viewEventoColorIndicatorItem);
            tvNombreEvento = itemView.findViewById(R.id.tvNombreEventoItem);
        }
    }
}