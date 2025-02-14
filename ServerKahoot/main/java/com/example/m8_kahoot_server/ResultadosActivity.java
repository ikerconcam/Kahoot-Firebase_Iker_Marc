package com.example.m8_kahoot_server;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadosActivity extends AppCompatActivity {
    private LinearLayout layoutUsuarios, layoutCorrectas, layoutGanador;
    private DatabaseReference dbRef;
    private String numeroSala;
    private Map<String, String> respuestasCorrectas = new HashMap<>();
    private Map<String, Integer> puntuaciones = new HashMap<>();
    private List<String> ganadores = new ArrayList<>();
    private CheckBox checkBoxMostrarRespuestas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        layoutUsuarios = findViewById(R.id.layoutUsuarios);
        layoutCorrectas = findViewById(R.id.layoutCorrectas);
        layoutGanador = findViewById(R.id.layoutGanador);
        checkBoxMostrarRespuestas = findViewById(R.id.checkBoxMostrarRespuestas);

        numeroSala = getIntent().getStringExtra("numeroSalaaa"); // Recibe el número de sala

        if (numeroSala == null) {
            Toast.makeText(this, "Error: No se recibió el número de sala", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        cargarRespuestasCorrectas();

        // Establecer el listener para la CheckBox
        checkBoxMostrarRespuestas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Mostrar las respuestas correctas y los usuarios con sus respuestas
                mostrarRespuestasCorrectas();
                cargarUsuariosYComparar();
            } else {
                // Solo mostrar los ganadores sin respuestas
                layoutCorrectas.setVisibility(View.GONE);
                layoutUsuarios.setVisibility(View.GONE);
                mostrarGanadores();
            }
        });

        // Cargar solo los ganadores al inicio si la CheckBox no está activada
        if (!checkBoxMostrarRespuestas.isChecked()) {
            cargarGanadoresSolo();
        }
    }

    private void cargarRespuestasCorrectas() {
        dbRef.child("Correctas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                respuestasCorrectas.clear();
                layoutCorrectas.removeAllViews();

                for (DataSnapshot preguntaSnapshot : snapshot.getChildren()) {
                    respuestasCorrectas.put(preguntaSnapshot.getKey(), preguntaSnapshot.getValue(String.class));
                }

                // Si el checkbox está marcado, mostrar las respuestas correctas y los usuarios
                if (checkBoxMostrarRespuestas.isChecked()) {
                    mostrarRespuestasCorrectas();
                    cargarUsuariosYComparar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Error al leer respuestas correctas: " + error.getMessage());
            }
        });
    }

    private void cargarUsuariosYComparar() {
        dbRef.child(numeroSala).child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutUsuarios.removeAllViews();
                puntuaciones.clear();

                for (DataSnapshot usuarioSnapshot : snapshot.getChildren()) {
                    String nombreUsuario = usuarioSnapshot.getKey();
                    if (nombreUsuario != null) {
                        int puntaje = compararRespuestas(usuarioSnapshot);
                        puntuaciones.put(nombreUsuario, puntaje);
                        mostrarUsuario(nombreUsuario, usuarioSnapshot);
                    }
                }

                determinarGanadores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Error al leer usuarios: " + error.getMessage());
            }
        });
    }

    private int compararRespuestas(DataSnapshot usuarioSnapshot) {
        int aciertos = 0;
        for (DataSnapshot respuestaSnapshot : usuarioSnapshot.getChildren()) {
            String pregunta = respuestaSnapshot.getKey();
            String respuestaUsuario = respuestaSnapshot.getValue(String.class);

            if (pregunta != null && respuestaUsuario != null && respuestaUsuario.equals(respuestasCorrectas.get(pregunta))) {
                aciertos++;
            }
        }
        return aciertos;
    }

    private void mostrarUsuario(String nombre, DataSnapshot usuarioSnapshot) {
        TextView textView = new TextView(this);
        textView.setText("Usuario: " + nombre);
        textView.setTextSize(18);
        textView.setPadding(10, 10, 10, 10);
        layoutUsuarios.addView(textView);

        for (DataSnapshot datoSnapshot : usuarioSnapshot.getChildren()) {
            String key = datoSnapshot.getKey();
            String value = datoSnapshot.getValue(String.class);

            if (key != null && value != null) {
                TextView textDato = new TextView(this);
                textDato.setText(" - " + key + ": " + value);
                textDato.setTextSize(16);
                textDato.setPadding(20, 5, 10, 5);
                layoutUsuarios.addView(textDato);
            }
        }
    }

    private void mostrarRespuestasCorrectas() {
        layoutCorrectas.setVisibility(View.VISIBLE);  // Asegura que las respuestas se muestren solo si está seleccionado

        TextView titulo = new TextView(this);
        titulo.setText("Correctas:");
        titulo.setTextSize(18);
        titulo.setPadding(10, 10, 10, 10);
        layoutCorrectas.addView(titulo);

        for (Map.Entry<String, String> entry : respuestasCorrectas.entrySet()) {
            TextView textView = new TextView(this);
            textView.setText(" - " + entry.getKey() + ": " + entry.getValue());
            textView.setTextSize(16);
            textView.setPadding(20, 5, 10, 5);
            layoutCorrectas.addView(textView);
        }
    }

    private void determinarGanadores() {
        int maxPuntos = -1;
        ganadores.clear();

        // Determinamos el puntaje máximo
        for (Map.Entry<String, Integer> entry : puntuaciones.entrySet()) {
            int puntos = entry.getValue();
            String nombre = entry.getKey();

            if (puntos > maxPuntos) {
                maxPuntos = puntos;
                ganadores.clear(); // Limpiamos la lista si encontramos un nuevo máximo
                ganadores.add(nombre); // Agregamos al nuevo ganador
            } else if (puntos == maxPuntos) {
                ganadores.add(nombre); // Agregamos al empate
            }
        }

        mostrarGanadores();
        actualizarGanadoresEnFirebase();
    }


    private void mostrarGanadores() {
        layoutGanador.removeAllViews();
        TextView textView = new TextView(this);

        if (ganadores.size() == 1) {
            textView.setText("Ganador: " + ganadores.get(0));
        } else {
            StringBuilder ganadoresTexto = new StringBuilder("Jugadores: ");
            for (String ganador : ganadores) {
                ganadoresTexto.append(ganador).append(", ");
            }
            ganadoresTexto.setLength(ganadoresTexto.length() - 2); // Eliminar la última coma y espacio
            textView.setText(ganadoresTexto.toString());
        }

        textView.setTextSize(20);
        textView.setPadding(10, 10, 10, 10);
        layoutGanador.addView(textView);
    }

    // Función para actualizar los ganadores en Firebase
    private void actualizarGanadoresEnFirebase() {
        if (!ganadores.isEmpty()) {
            dbRef.child(numeroSala).child("ganadores").setValue(ganadores)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseData", "Ganadores actualizados en Firebase"))
                    .addOnFailureListener(e -> Log.e("FirebaseData", "Error al actualizar ganadores: " + e.getMessage()));
        }
    }

    // Cargar solo los ganadores sin mostrar usuarios si la CheckBox no está activada
    private void cargarGanadoresSolo() {
        dbRef.child(numeroSala).child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                puntuaciones.clear();

                // Solo calculamos los ganadores sin mostrar usuarios ni respuestas
                for (DataSnapshot usuarioSnapshot : snapshot.getChildren()) {
                    String nombreUsuario = usuarioSnapshot.getKey();
                    if (nombreUsuario != null) {
                        int puntaje = compararRespuestas(usuarioSnapshot);
                        puntuaciones.put(nombreUsuario, puntaje);
                    }
                }

                determinarGanadores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Error al leer usuarios: " + error.getMessage());
            }
        });
    }
}
