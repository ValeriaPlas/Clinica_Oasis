package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView infoText = findViewById(R.id.infoText);
        infoText.setText(
                "🏥 Clínica Oasis (Salud y Bienestar) 🏥\n\n" +
                        "📱 Aplicación: Clínica Oasis\n" +
                        "📌 Versión: 1.0\n\n" +
                        "🌐 Descripción: \n" +
                        "Aplicación móvil diseñada para la gestión eficiente de citas médicas en la Clínica Oasis. " +
                        "Permite a los pacientes:\n" +
                        " - Agendar citas con doctores especializados 👩‍⚕️👨‍⚕️\n" +
                        " - Consultar disponibilidad de horarios 📅\n" +
                        " - Recibir notificaciones y recordatorios en tiempo real 🔔\n" +
                        " - Mejorar la comunicación con el consultorio 💬\n\n" +
                        "🔧 Tecnologías usadas:\n" +
                        "- Firebase (Google Cloud) para base de datos y autenticación 🔐\n" +
                        "- Android Studio para la app 📲\n" +
                        "👩‍💻 Equipo de Desarrollo:\n" +
                        " - Coordinadora del Proyecto: \n" +
                        " - Desarrollador Backend & Base de Datos: \n" +
                        " - Desarrollador Frontend UX/UI: \n\n" +
                        "📬 Contacto:\n" +
                        "Correo: contacto@clinicaoasis.com\n" +
                        "Teléfono: +52 477-123-45-67\n\n" +
                        "⚙️ Funcionalidades principales:\n" +
                        "  - Gestión de doctores, pacientes y consultorios 🗂️\n" +
                        "  - Recordatorios automáticos de citas ⏰\n" +
                        "  - Acceso remoto y seguro 🔒\n" +
                        "  - Escalabilidad para futuros módulos como: expedientes médicos digitales 📑, farmacia 💊 y terapias 💆‍♀️.\n\n" +
                        "🔒 Privacidad y Seguridad:\n" +
                        "    Clínica Oasis garantiza la protección de tus datos personales y médicos, " +
                        "siguiendo buenas prácticas de seguridad y confidencialidad."
        );
    }
}
