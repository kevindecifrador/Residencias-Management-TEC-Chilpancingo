package com.example.residenciapptec;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmasHelper {
    private Context context;

    public AlarmasHelper(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void agendarNotificacionInmediata(String mensaje, int idNotificacion) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 3);

        Intent intent = new Intent(context, AlarmaReceiver.class);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("id", idNotificacion);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, idNotificacion, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }
}
