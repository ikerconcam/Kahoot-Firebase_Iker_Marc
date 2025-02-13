package com.example.m8_kahoot_cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecondActivity extends AppCompatActivity {

    private TextView salaCodigoTextView;
    private TextView esperandoTextView;
    private Button exitButton;
    private String codigoSala;
    private String nombreUsuario;
    private DatabaseReference databaseReference;
String cliente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Obtener los datos de la intent
        codigoSala = getIntent().getStringExtra("codigoSala");
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        // Referencias a los elementos de la UI
        salaCodigoTextView = findViewById(R.id.salaCodigo);
        esperandoTextView = findViewById(R.id.esperandoMensaje);
        exitButton = findViewById(R.id.exitButton);

        // Mostrar el código de la sala
        salaCodigoTextView.setText("Sala: " + codigoSala);

        // Referencia a Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child(codigoSala);

        // Comprobar si la sala ha iniciado
        comprobarSalaIniciada();

        // Lógica para el botón de salir
        exitButton.setOnClickListener(v -> eliminarUsuarioYSalir());

        // Escuchar cambios en la clave 'quiz' para ver si pasa de 0 a 1
        escucharCambioQuiz();
    }

    private void comprobarSalaIniciada() {
        // Verificar si la sala ya tiene un estado de "START"
        DatabaseReference salaRef = databaseReference.child("estadoSala");

        salaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String estado = dataSnapshot.getValue(String.class);
                    if ("START".equals(estado)) {
                        // La sala ha comenzado, pasar a la siguiente actividad (iniciar el Kahoot)
                        iniciarKahoot();
                    } else {
                        // La sala sigue en espera
                        esperandoTextView.setText("Esperando...");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // En caso de error
                esperandoTextView.setText("Error al comprobar el estado de la sala.");
            }
        });
    }

    private void iniciarKahoot() {
        // Aquí podrías iniciar el juego (pasar a la siguiente pantalla, por ejemplo)
        esperandoTextView.setText("¡La sala ha comenzado!");
    }

    private void eliminarUsuarioYSalir() {
        // Eliminar solo el nodo del usuario
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(codigoSala)
                .child("usuarios")
                .child(nombreUsuario);

        usuarioRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Decrementamos el total
                DatabaseReference totalRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child(codigoSala)
                        .child("total");

                totalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Integer total = dataSnapshot.getValue(Integer.class);
                        if (total != null) {
                            // Decrementamos el total
                            totalRef.setValue(total - 1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SecondActivity.this, "Error al actualizar el total", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(SecondActivity.this, "Has salido de la sala", Toast.LENGTH_SHORT).show();
                finish();  // Vuelve a la pantalla anterior
            } else {
                Toast.makeText(SecondActivity.this, "Error al abandonar la sala", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void escucharCambioQuiz() {
        // Escuchar cambios en el valor de 'quiz'
        DatabaseReference quizRef = databaseReference.child("quiz");

        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer quizValue = dataSnapshot.getValue(Integer.class);
                    if (quizValue != null) {
                        if (quizValue == 1) {
                            // Si el valor de 'quiz' pasa a 1, navegamos a la siguiente actividad
                            Intent intent = new Intent(SecondActivity.this, QuizActivity.class);
                            intent.putExtra("codigoSala", codigoSala);  // Pasa el código de la sala
                            intent.putExtra("nombreUsuario", nombreUsuario);
                            startActivity(intent);
                            finish();  // Opcional: cerrar la actividad actual
                        }
                    } else {
                        // Si quizValue es null, no se hace nada
                        Toast.makeText(SecondActivity.this, "Valor de 'quiz' no válido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Si 'quiz' no existe en la base de datos, no se hace nada
                    Toast.makeText(SecondActivity.this, "No se encuentra el valor 'quiz' en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al escuchar el cambio
                Toast.makeText(SecondActivity.this, "Error al escuchar cambios en 'quiz'", Toast.LENGTH_SHORT).show();
            }
        });
    }
}