package com.example.residenciapptec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificacionDiariaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alumnos").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AlarmasHelper helper = new AlarmasHelper(context);

                        verificarYProgramar(helper, documentSnapshot.getString("fecha_entrega_1"), "fecha1");
                        verificarYProgramar(helper, documentSnapshot.getString("fecha_entrega_2"), "fecha2");
                        verificarYProgramar(helper, documentSnapshot.getString("fecha_entrega_3"), "fecha3");
                    }
                })
                .addOnFailureListener(e -> Log.e("Notificacion", "Error Firebase", e));
    }

    private void verificarYProgramar(AlarmasHelper helper, String fechaTexto, String clave) {
        // Validación estricta
        if (fechaTexto == null || fechaTexto.isEmpty() || !fechaTexto.contains("/")) return;

        try {
            // Limpieza robusta
            String fechaLimpia = fechaTexto.replace("Fecha:", "").replace("Entrega:", "").trim();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateEntrega = sdf.parse(fechaLimpia);

            if (dateEntrega == null) return;

            // 1. Configurar la fecha de entrega (Base)
            Calendar fechaBase = Calendar.getInstance();
            fechaBase.setTime(dateEntrega);
            fechaBase.set(Calendar.HOUR_OF_DAY, 0);
            fechaBase.set(Calendar.MINUTE, 0);
            fechaBase.set(Calendar.SECOND, 0);
            fechaBase.set(Calendar.MILLISECOND, 0);

            // 2. Configurar el día de hoy
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            String nombreDoc = obtenerNombreDocumento(clave);

            // Definir qué es "Día Anterior" (Ayer respecto a la entrega)
            Calendar diaAnterior = (Calendar) fechaBase.clone();
            diaAnterior.add(Calendar.DAY_OF_MONTH, -1);

            // Definir qué es "Día Siguiente" (Mañana respecto a la entrega)
            Calendar diaSiguiente = (Calendar) fechaBase.clone();
            diaSiguiente.add(Calendar.DAY_OF_MONTH, 1);

            // A. UN DÍA ANTES (Avisar preventivamente)
            if (hoy.equals(diaAnterior)) {
                helper.agendarNotificacionInmediata("⚠️ Mañana vence: " + nombreDoc, generarId(clave, 1));
            }

            // B. EL DÍA EXACTO (Avisar urgencia)
            if (hoy.equals(fechaBase)) {
                helper.agendarNotificacionInmediata("🚨 ¡HOY se entrega " + nombreDoc + "!", generarId(clave, 2));
            }

            // C. UN DÍA DESPUÉS (Avisar fallo)
            if (hoy.equals(diaSiguiente)) {
                helper.agendarNotificacionInmediata("❌ Se pasó la fecha de: " + nombreDoc, generarId(clave, 3));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarEnHistorial(Context context, String mensaje) {
        SharedPreferences prefs = context.getSharedPreferences("NotificacionesPrefs", Context.MODE_PRIVATE);
        String historialPrevio = prefs.getString("historial", "");
        long tiempo = System.currentTimeMillis();

        String nuevaLinea = mensaje + "|" + tiempo + "\n";

        if (!historialPrevio.contains(mensaje + "|" + tiempo)) {
            prefs.edit().putString("historial", nuevaLinea + historialPrevio).apply();
        }
    }

    private String obtenerNombreDocumento(String clave) {
        switch (clave) {
            case "fecha1": return "Reporte Asesoría 1";
            case "fecha2": return "Reporte Asesoría 2";
            case "fecha3": return "Reporte Asesoría 3";
            default: return "Documento";
        }
    }

    private int generarId(String clave, int tipo) {
        return (clave.hashCode() + tipo) & Integer.MAX_VALUE;
    }
}
