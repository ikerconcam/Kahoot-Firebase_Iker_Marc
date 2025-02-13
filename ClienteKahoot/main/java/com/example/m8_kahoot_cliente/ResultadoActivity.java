package com.example.m8_kahoot_cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultadoActivity extends AppCompatActivity {

    private TextView primerLugarTextView;
    private TextView nombreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);  // Asegúrate de que este es el layout correcto

        // Inicializar las vistas de los resultados
        primerLugarTextView = findViewById(R.id.primerLugar);
        nombreTextView = findViewById(R.id.nombre);

        // Obtener los datos del ganador pasados en el Intent
        String ganador = getIntent().getStringExtra("ganador");
        String nombreGanador = getIntent().getStringExtra("nombreGanador");


        // Botón para volver a jugar
        Button volverAJugarButton = findViewById(R.id.volverAJugarButton);
        volverAJugarButton.setOnClickListener(view -> {
            // Redirigir a la MainActivity (pantalla principal)
            Intent intent = new Intent(ResultadoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Finaliza esta actividad
        });
    }
}