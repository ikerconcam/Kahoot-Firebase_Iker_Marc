package com.example.m8_kahoot_server;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;

public class ResultadosActivity extends AppCompatActivity {

    private TextView preguntaTextView;
    private RadioGroup respuestaRadioGroup;
    private Button enviarButton;
    private TextView resultadoTextView;

    private HashMap<Integer, String> respuestasCorrectas;
    private int preguntaActual = 1;
    private int respuestasCorrectasUsuario = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        preguntaTextView = findViewById(R.id.preguntaTextView);
        respuestaRadioGroup = findViewById(R.id.respuestaRadioGroup);
        enviarButton = findViewById(R.id.enviarButton);
        resultadoTextView = findViewById(R.id.resultadoTextView);

        // Definir respuestas correctas (simulado)
        respuestasCorrectas = new HashMap<>();
        respuestasCorrectas.put(1, "C");
        respuestasCorrectas.put(2, "A");
        respuestasCorrectas.put(3, "B");

        mostrarPregunta(preguntaActual);

        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int respuestaSeleccionadaId = respuestaRadioGroup.getCheckedRadioButtonId();
                if (respuestaSeleccionadaId != -1) {
                    RadioButton respuestaSeleccionada = findViewById(respuestaSeleccionadaId);
                    String respuestaUsuario = respuestaSeleccionada.getText().toString();

                    if (verificarRespuesta(preguntaActual, respuestaUsuario)) {
                        respuestasCorrectasUsuario++;
                    }

                    preguntaActual++;
                    if (preguntaActual <= respuestasCorrectas.size()) {
                        mostrarPregunta(preguntaActual);
                    } else {
                        mostrarResultado();
                    }
                }
            }
        });
    }

    private void mostrarPregunta(int pregunta) {
        preguntaTextView.setText("Pregunta " + pregunta);
        // Aquí podrías cargar las preguntas desde un archivo o base de datos
    }

    private boolean verificarRespuesta(int pregunta, String respuestaUsuario) {
        return respuestaUsuario.equals(respuestasCorrectas.get(pregunta));
    }

    private void mostrarResultado() {
        resultadoTextView.setText("Respuestas correctas: " + respuestasCorrectasUsuario);
        // Aquí podrías determinar al ganador según tus criterios
    }
}