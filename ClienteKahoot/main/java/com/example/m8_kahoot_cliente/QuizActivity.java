package com.example.m8_kahoot_cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuizActivity extends AppCompatActivity {

    private TextView questionTextView;  // Para mostrar el número de la pregunta
    private DatabaseReference databaseReference;
    private String codigoSala;
    private String nombreUsuario;  // Nombre del usuario
    private Integer numeroPregunta;  // Número de la pregunta actual
    private boolean respuestaSeleccionada = false;  // Bandera para verificar si ya se seleccionó una respuesta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Obtener el código de la sala y nombre del usuario de la Intent
        codigoSala = getIntent().getStringExtra("codigoSala");
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        if (nombreUsuario == null) {
            System.out.println("Error: nombreUsuario no recibido.");
        }

        // Inicializar la referencia a Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child(codigoSala);

        // Referencias de los elementos de la UI
        questionTextView = findViewById(R.id.questionText);

        // Cargar el número de la pregunta
        cargarNumeroPregunta();

        // Configurar botones de respuesta
        Button buttonA = findViewById(R.id.option1);
        Button buttonB = findViewById(R.id.option2);
        Button buttonC = findViewById(R.id.option3);
        Button buttonD = findViewById(R.id.option4);

        // Establecer los escuchadores de clic para cada botón
        buttonA.setOnClickListener(view -> {
            if (!respuestaSeleccionada) {
                guardarRespuesta("A");
                deshabilitarBotones(buttonA, buttonB, buttonC, buttonD);
            }
        });

        buttonB.setOnClickListener(view -> {
            if (!respuestaSeleccionada) {
                guardarRespuesta("B");
                deshabilitarBotones(buttonA, buttonB, buttonC, buttonD);
            }
        });

        buttonC.setOnClickListener(view -> {
            if (!respuestaSeleccionada) {
                guardarRespuesta("C");
                deshabilitarBotones(buttonA, buttonB, buttonC, buttonD);
            }
        });

        buttonD.setOnClickListener(view -> {
            if (!respuestaSeleccionada) {
                guardarRespuesta("D");
                deshabilitarBotones(buttonA, buttonB, buttonC, buttonD);
            }
        });
    }

    private void cargarNumeroPregunta() {
        // Escuchar cambios en la clave 'quiz' para obtener el número de la pregunta actual
        DatabaseReference preguntaRef = databaseReference.child("quiz");

        preguntaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer nuevoNumeroPregunta = dataSnapshot.getValue(Integer.class);
                    if (nuevoNumeroPregunta != null && nuevoNumeroPregunta.equals(-1)) {
                        // Si el valor de 'quiz' es -1, ir a ResultadoActivity
                        Intent intent = new Intent(QuizActivity.this, ResultadoActivity.class);
                        startActivity(intent);
                        finish();  // Finalizar la actividad actual
                    } else if (nuevoNumeroPregunta != null && !nuevoNumeroPregunta.equals(numeroPregunta)) {
                        numeroPregunta = nuevoNumeroPregunta;
                        // Actualizar el número de la pregunta en la UI
                        questionTextView.setText("Pregunta " + numeroPregunta);

                        // Reiniciar la lógica de selección de respuesta
                        respuestaSeleccionada = false;

                        // Habilitar nuevamente los botones
                        Button buttonA = findViewById(R.id.option1);
                        Button buttonB = findViewById(R.id.option2);
                        Button buttonC = findViewById(R.id.option3);
                        Button buttonD = findViewById(R.id.option4);
                        habilitarBotones(buttonA, buttonB, buttonC, buttonD);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores de la base de datos
            }
        });
    }

    private void guardarRespuesta(String respuesta) {
        // Verificar que numeroPregunta y nombreUsuario no son nulos
        if (numeroPregunta == null) {
            System.out.println("Error: numeroPregunta aún no cargado.");
            return;  // Detener si no se ha cargado el número de pregunta
        }

        if (nombreUsuario == null) {
            System.out.println("Error: nombreUsuario es nulo.");
            return;  // Detener si no se ha recibido el nombre de usuario
        }

        // Crear la referencia para el nombre del usuario en la base de datos
        DatabaseReference usuarioRef = databaseReference.child("usuarios").child(nombreUsuario);

        // Insertar la respuesta bajo el número de pregunta correspondiente
        usuarioRef.child(String.valueOf(numeroPregunta)).setValue(respuesta);

        // Marcar que ya se seleccionó una respuesta
        respuestaSeleccionada = true;
    }

    private void deshabilitarBotones(Button... botones) {
        // Deshabilitar todos los botones después de una selección
        for (Button boton : botones) {
            boton.setEnabled(false);
        }
    }

    private void habilitarBotones(Button... botones) {
        // Habilitar todos los botones cuando se cambia la pregunta
        for (Button boton : botones) {
            boton.setEnabled(true);
        }
    }
}
