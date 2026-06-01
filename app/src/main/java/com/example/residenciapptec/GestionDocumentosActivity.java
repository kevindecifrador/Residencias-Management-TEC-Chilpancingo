package com.example.residenciapptec;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GestionDocumentosActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CheckBox checkAprobado;

    private EditText etFecha1, etObs1;
    private Button btnVer1, btnGuardarObs1;
    private ImageButton btnBorrarFecha1;

    private EditText etFecha2, etObs2;
    private Button btnVer2, btnGuardarObs2;
    private ImageButton btnBorrarFecha2;

    private EditText etFecha3, etObs3;
    private Button btnVer3, btnGuardarObs3;
    private ImageButton btnBorrarFecha3;

    private DocumentReference alumnoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_documentos);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        String uidAlumno = getIntent().getStringExtra("uid_alumno");

        if (uidAlumno != null) {
            alumnoRef = db.collection("alumnos").document(uidAlumno);
        } else {
            Toast.makeText(this, "Error: ID nulo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAprobado = findViewById(R.id.checkAprobado);

        etFecha1 = findViewById(R.id.etFecha1);
        btnBorrarFecha1 = findViewById(R.id.btnBorrarFecha1);
        btnVer1 = findViewById(R.id.btnVerArchivo1);
        btnGuardarObs1 = findViewById(R.id.btnGuardarObs1);
        etObs1 = findViewById(R.id.etObs1);

        etFecha2 = findViewById(R.id.etFecha2);
        btnBorrarFecha2 = findViewById(R.id.btnBorrarFecha2);
        btnVer2 = findViewById(R.id.btnVerArchivo2);
        btnGuardarObs2 = findViewById(R.id.btnGuardarObs2);
        etObs2 = findViewById(R.id.etObs2);

        etFecha3 = findViewById(R.id.etFecha3);
        btnBorrarFecha3 = findViewById(R.id.btnBorrarFecha3);
        btnVer3 = findViewById(R.id.btnVerArchivo3);
        btnGuardarObs3 = findViewById(R.id.btnGuardarObs3);
        etObs3 = findViewById(R.id.etObs3);

        cargarDatos();

        // --- AHORA GUARDAMOS EN LAS LLAVES NUEVAS ("fecha_entrega_X") ---
        etFecha1.setOnClickListener(v -> mostrarDatePickerYGuardar("fecha_entrega_1", etFecha1));
        etFecha2.setOnClickListener(v -> mostrarDatePickerYGuardar("fecha_entrega_2", etFecha2));
        etFecha3.setOnClickListener(v -> mostrarDatePickerYGuardar("fecha_entrega_3", etFecha3));

        btnBorrarFecha1.setOnClickListener(v -> eliminarFecha("fecha_entrega_1", etFecha1));
        btnBorrarFecha2.setOnClickListener(v -> eliminarFecha("fecha_entrega_2", etFecha2));
        btnBorrarFecha3.setOnClickListener(v -> eliminarFecha("fecha_entrega_3", etFecha3));

        // --- LEEMOS LOS ARCHIVOS NUEVOS ("url_asesoria_X") ---
        btnVer1.setOnClickListener(v -> abrirArchivo("url_asesoria_1"));
        btnVer2.setOnClickListener(v -> abrirArchivo("url_asesoria_2"));
        btnVer3.setOnClickListener(v -> abrirArchivo("url_asesoria_3"));

        // --- GUARDAMOS OBSERVACIONES EN LLAVES NUEVAS ("obs_asesoria_X") ---
        btnGuardarObs1.setOnClickListener(v -> guardarObservacion("obs_asesoria_1", etObs1));
        btnGuardarObs2.setOnClickListener(v -> guardarObservacion("obs_asesoria_2", etObs2));
        btnGuardarObs3.setOnClickListener(v -> guardarObservacion("obs_asesoria_3", etObs3));

        checkAprobado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alumnoRef.update("aprobado", isChecked);
        });
    }

    private void cargarDatos() {
        alumnoRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Boolean aprobado = doc.getBoolean("aprobado");
            checkAprobado.setChecked(aprobado != null && aprobado);

            etFecha1.setText(doc.getString("fecha_entrega_1"));
            etFecha2.setText(doc.getString("fecha_entrega_2"));
            etFecha3.setText(doc.getString("fecha_entrega_3"));

            etObs1.setText(doc.getString("obs_asesoria_1"));
            etObs2.setText(doc.getString("obs_asesoria_2"));
            etObs3.setText(doc.getString("obs_asesoria_3"));

            if (doc.getString("fecha_entrega_1") == null)
                findViewById(R.id.cardSeguimiento2).setVisibility(View.GONE);
            if (doc.getString("fecha_entrega_2") == null)
                findViewById(R.id.cardEvaluacion).setVisibility(View.GONE);
        });
    }

    private void mostrarDatePickerYGuardar(String campoFirestore, EditText campoVisual) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            String fecha = String.format("%02d/%02d/%04d", d, m + 1, y);
            campoVisual.setText(fecha);

            alumnoRef.update(campoFirestore, fecha)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Fecha asignada", Toast.LENGTH_SHORT).show());
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void eliminarFecha(String campo, EditText campoVisual) {
        Map<String, Object> update = new HashMap<>();
        update.put(campo, FieldValue.delete());

        alumnoRef.update(update).addOnSuccessListener(unused -> {
            campoVisual.setText("");
            Toast.makeText(this, "Fecha eliminada", Toast.LENGTH_SHORT).show();
        });
    }

    private void abrirArchivo(String campoURL) {
        alumnoRef.get().addOnSuccessListener(doc -> {
            String url = doc.getString(campoURL);
            if (url != null && !url.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                Toast.makeText(this, "El alumno no ha subido este archivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarObservacion(String campoObs, EditText campoVisual) {
        String texto = campoVisual.getText().toString().trim();
        alumnoRef.update(campoObs, texto).addOnSuccessListener(unused ->
                Toast.makeText(this, "Observación enviada al alumno", Toast.LENGTH_SHORT).show()
        );
    }
}
