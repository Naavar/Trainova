package com.navar.trainova.ui.ia;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.R;


public class IaActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etChatMessage;
    private ImageButton btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ia);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        btnSendMessage.setOnClickListener(v -> {
            String message = etChatMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                Toast.makeText(this, "Enviando a IA: " + message, Toast.LENGTH_SHORT).show();
                etChatMessage.setText("");
            }
        });

    }

}