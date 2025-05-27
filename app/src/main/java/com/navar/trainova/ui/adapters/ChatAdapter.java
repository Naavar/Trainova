package com.navar.trainova.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.ui.ia.IaActivity.Message;
import com.navar.trainova.ui.ia.IaActivity.RecomendacionIA;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para el RecyclerView del chat con la IA.
 * Es capaz de mostrar tres tipos de vistas: mensajes del usuario, mensajes de texto del asistente
 * y tarjetas de sugerencia interactivas con botones.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;
    private static final int VIEW_TYPE_SUGGESTION = 3;

    private List<Message> messageList;
    private final SuggestionInteractionListener listener;

    /**
     * Interfaz para comunicar las acciones del usuario sobre una sugerencia (Aceptar/Rechazar)
     * desde el adapter hacia la Activity o Fragment que lo contiene.
     */
    public interface SuggestionInteractionListener {
        void onAcceptSuggestion(CatalogoEvento plantilla);
        void onDeclineSuggestion();
    }

    public ChatAdapter(List<Message> messageList, SuggestionInteractionListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.sugerencia != null) {
            return VIEW_TYPE_SUGGESTION;
        } else if ("user".equals(message.role)) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ASSISTANT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_USER:
                View userView = inflater.inflate(R.layout.item_chat_user, parent, false);
                return new UserMessageViewHolder(userView);
            case VIEW_TYPE_SUGGESTION:
                View suggestionView = inflater.inflate(R.layout.item_chat_suggestion, parent, false);
                return new SuggestionViewHolder(suggestionView); // Ya no se pasa el listener aquí
            case VIEW_TYPE_ASSISTANT:
            default:
                View assistantView = inflater.inflate(R.layout.item_chat_assistant, parent, false);
                return new AssistantMessageViewHolder(assistantView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER:
                ((UserMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_ASSISTANT:
                ((AssistantMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_SUGGESTION:
                ((SuggestionViewHolder) holder).bind(message.sugerencia);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMessages(List<Message> newMessages) {
        this.messageList = new ArrayList<>(newMessages);
        notifyDataSetChanged();
    }

    public void removeThinkingMessage() {
        if (!messageList.isEmpty() && "Pensando...".equals(messageList.get(messageList.size() - 1).content)) {
            int lastPosition = messageList.size() - 1;
            messageList.remove(lastPosition);
            notifyItemRemoved(lastPosition);
        }
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatMessage;
        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
        }
        void bind(Message message) {
            tvChatMessage.setText(message.content);
        }
    }

    class AssistantMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatMessage;
        AssistantMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
        }
        void bind(Message message) {
            tvChatMessage.setText(message.content);
        }
    }

    // AHORA NO ES ESTÁTICA
    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvJustificacion;
        TextView tvNombreRutinaSugerida;
        Button btnAceptar;
        Button btnRechazar;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJustificacion = itemView.findViewById(R.id.tvJustificacionIa);
            tvNombreRutinaSugerida = itemView.findViewById(R.id.tvNombreRutinaSugerida);
            btnAceptar = itemView.findViewById(R.id.btnAceptarSugerencia);
            btnRechazar = itemView.findViewById(R.id.btnRechazarSugerencia);
        }

        void bind(RecomendacionIA recomendacion) {
            tvJustificacion.setText(recomendacion.justificacionIA);
            tvNombreRutinaSugerida.setText(recomendacion.plantillaSugerida.getNombreEvento());

            btnAceptar.setOnClickListener(v -> {
                listener.onAcceptSuggestion(recomendacion.plantillaSugerida);
                btnAceptar.setEnabled(false);
                btnRechazar.setEnabled(false);
            });

            btnRechazar.setOnClickListener(v -> {
                listener.onDeclineSuggestion();
                btnAceptar.setEnabled(false);
                btnRechazar.setEnabled(false);
            });
        }
    }
}