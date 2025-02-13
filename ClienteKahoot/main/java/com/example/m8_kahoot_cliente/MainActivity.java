package com.example.m8_kahoot_cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();  // Referencia raíz de Firebase

        EditText numberInput = findViewById(R.id.numberInput);
        EditText textInput = findViewById(R.id.textInput);
        Button confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = numberInput.getText().toString().trim();
                String text = textInput.getText().toString().trim();

                if (number.isEmpty() || text.isEmpty()) {
                    showAlert("Completa los campos");
                } else if (number.length() > 4 || text.length() > 20) {
                    showAlert("Número máximo de caracteres excedido");
                } else {
                    comprobarCodigo(number, text);
                }
            }
        });
    }

    private void comprobarCodigo(String number, String cliente) {
        DatabaseReference salaRef = databaseReference.child(number);

        salaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // La sala existe
                    DatabaseReference usuariosRef = salaRef.child("usuarios").child(cliente);

                    usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (!userSnapshot.exists()) {
                                // Añadimos el usuario con un valor vacío ("")
                                usuariosRef.setValue("").addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Usuario registrado", Toast.LENGTH_SHORT).show();

                                        // Incrementamos el total en la base de datos
                                        DatabaseReference totalRef = databaseReference.child(number).child("total");
                                        totalRef.runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Integer total = mutableData.getValue(Integer.class);
                                                if (total == null) {
                                                    total = 0;
                                                }
                                                mutableData.setValue(total + 1);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData) {
                                                // Pasamos a la siguiente pantalla
                                                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                                intent.putExtra("codigoSala", number);
                                                intent.putExtra("nombreUsuario", cliente);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "El usuario ya existe en esta sala", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Error al comprobar usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // La sala no existe
                    showAlert("No existe esta sala");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al comprobar la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Atención")
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}