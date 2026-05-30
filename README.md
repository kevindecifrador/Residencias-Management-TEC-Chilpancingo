# ResidenciAPP TEC - Sistema Móvil para la Gestión de Residencias Profesionales 📱📄

Aplicación móvil nativa en Android desarrollada en equipo como solución integral para la administración, seguimiento temporal y generación de reportes oficiales de residencias profesionales en el Instituto Tecnológico de Chilpancingo.

## 📌 El Problema
Los estudiantes y docentes que participan en el programa de residencias profesionales a menudo carecen de un canal unificado para la carga de datos de proyectos, lo que ralentiza las revisiones de expedientes. Además, la ausencia de recordatorios automáticos provoca que los alumnos olviden las fechas límite de entrega de reportes parciales, generando retrasos administrativos y pérdida de sincronía con sus asesores.

## 💡 La Solución
ResidenciAPP TEC resuelve estas problemáticas centralizando los flujos de trabajo de alumnos y docentes en una sola aplicación nativa. La plataforma destaca por implementar una estrategia de persistencia híbrida local-nube, un motor gráfico de generación de documentos en segundo plano y un sistema asíncrono de notificaciones temporales para mitigar las entregas tardías.

---

## 🛠️ Stack Tecnológico & Arquitectura Nativa

### Desarrollo Móvil Core (Android Nativo)
* **Android SDK & Java Nativo:** Desarrollo estructurado bajo el ciclo de vida nativo de actividades y componentes del sistema operativo Android.
* **Persistencia Híbrida (Offline-First):** Arquitectura de almacenamiento tolerante a fallos de red. Captura y concatena flujos de datos en archivos locales de texto plano mediante `FileOutputStream` (`MODE_APPEND`) y sincroniza asíncronamente mapas de datos estructurados hacia colecciones de **Firebase Firestore**.
* **Carga Asíncrona de Archivos Binarios (Blobs):** Integración con la API de **Firebase Storage** mediante selectores nativos (`ActivityResultContracts.GetContent`) para transferir de manera segura archivos PDF cifrados en la ruta física `uploads/{uid}/{campo_bd}.pdf`, mapeando dinámicamente las URLs de descarga en la base de datos.

### Motor de Documentación & Seguridad
* **API Gráfica Nativa (PdfDocument):** Renderizado de reportes en formato estándar A4 utilizando primitivas de dibujo técnico (`Canvas` y `Paint`), tipografías dinámicas y paletas de color institucionales (`rgb(27, 57, 106)`).
* **Compartición Segura (FileProvider):** Cumplimiento estricto de directivas de aislamiento de almacenamiento mediante la generación de URIs seguras y encapsuladas, otorgando permisos de lectura bajo demanda (`FLAG_GRANT_READ_URI_PERMISSION`) a visores externos de PDF.

### Tareas en Segundo Plano & Nube
* **Firebase Infrastructure:** Autenticación bi-rol gestionada mediante `FirebaseAuth` y persistencia en la nube a través de **Firebase Firestore**.
* **Sistema de Alertas Temporales (BroadcastReceiver):** Monitorización asíncrona mediante receptores de difusión que calculan deltas de tiempo lógicos con la API `Calendar`, agendando alertas automáticas en tres ventanas críticas: Preventiva (24 hrs antes), Urgencia (Día exacto) y Fallo (24 hrs después del vencimiento).

---

## ⚙️ Configuración e Integración de Servicios Cloud (Firebase)

El ecosistema está integrado a nivel de compilación utilizando el motor de dependencias de Gradle (`build.gradle.kts`), asegurando un aislamiento estricto de librerías y compatibilidad transaccional mediante el uso del compilador para Java 11 (`JavaVersion.VERSION_11`):

1. **Inyección del Plugin de Google Services (Top-Level):** Se añade el motor de mapeo de infraestructura mediante la ruta de clases `com.google.gms:google-services:4.4.1` dentro del script de construcción del proyecto.
2. **Aplicación del Plugin de Enrutamiento (App-Level):** Se aplica el identificador `com.google.gms.google-services` para automatizar la lectura y parseo del archivo confidencial de llaves institucionales en tiempo de compilación.
3. **Control de Versiones unificado mediante Firebase BoM:** Se implementa la plataforma **Firebase Bill of Materials (`firebase-bom:32.7.0`)**, delegando la sincronización de versiones internas y mitigando de forma nativa cualquier conflicto de librerías en los subcomponentes lógicos:
   * `firebase-auth`: Gestión síncrona y cifrada de sesiones de usuarios.
   * `firebase-firestore`: Persistencia NoSQL e integridad transaccional de expedientes de residencia.
   * `firebase-storage`: Repositorio binario en la nube para la auditoría de reportes académicos en formato PDF.
   * `play-services-auth:21.0.0`: Soporte complementario federado y encapsulado para credenciales seguras.

---

## ⚙️ Estructura del Proyecto (Módulos Críticos)

El proyecto está diseñado segmentando las responsabilidades de vistas, receptores del sistema operativo y proveedores de datos seguros:

app/src/main/

├── java/edu/tecnm/residencias/

│   ├── LoginActivity.java            # Autenticación nativa y manejo de SharedPreferences

│   ├── LoginDocenteActivity.java     # Panel de acceso seguro para el cuerpo docente

│   ├── RegistrarProyectoActivity.java # Formulario validado con persistencia dual local-nube

│   ├── GestionDocumentosActivity.java # Orquestador de reportes y exportación documental

│   ├── NotificacionDiariaReceiver.java # Receptor de difusión para cálculo temporal de alertas

│   └── AlarmaReceiver.java           # Manejador interno para el disparo de notificaciones push

└── resources/

├── xml/

│   ├── file_paths.xml            # Rutas del FileProvider permitidas para exportar PDFs

│   └── backup_rules.xml          # Reglas lógicas de respaldo seguro de la aplicación

└── layout/                       # Diseños XML responsivos de las interfaces de usuario


## 🏗️ Arquitectura de Componentes del Sistema

    [ Formulario de Datos ] ✏️
              │
              ├──► [ Guardado Local ] 💾 ──► Flujo binario appending en archivo .txt
              │
              └──► [ Firebase Cloud ] 🔥 ──► Actualización asíncrona por UID del alumno
                         │
                         ├──► [ Firebase Storage ] ☁️ (Carga de PDFs / URL Dinámica)
                         │
                         ▼ (Sincronización de Fechas Límite)
           [ NotificacionDiariaReceiver ] 🛰️
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
         (T-1 Día)    (Día D)   (T+1 Día)
        Preventiva   Urgente     Fallo
                        │
            ┌──────────┼──────────┐
            ▼          ▼          ▼
        (T-1 Día)    (Día D)   (T+1 Día)
        Preventiva   Urgente     Fallo


## 🛡️ Integridad de Entornos
Este desarrollo sigue las pautas profesionales de seguridad al desvincular cualquier credencial de base de datos directa del archivo de manifiesto (`AndroidManifest.xml`). La comunicación y el intercambio de archivos con aplicaciones externas del sistema operativo están restringidos mediante reglas explícitas de visibilidad y filtros de intents validados.
