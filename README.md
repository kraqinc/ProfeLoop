# 🎓 TareaApp — I.E. Del Pinar

> **Sistema de Gestión de Tareas Escolares** para la Institución Educativa Del Pinar  
> Android · Kotlin · Firebase · Material Design 3

---

## ✨ Características

| Función | Descripción |
|---|---|
| 🔐 **Autenticación** | Email/contraseña con Firebase Auth, roles Estudiante/Profesor |
| 📚 **Grados 1°–9°** | Agrupados en Primaria (1-5) y Bachillerato (6-9) |
| 📋 **17 Asignaturas** | Todas las materias del colegio |
| 📅 **4 Períodos** | Organización por período académico |
| 📤 **Publicar Tareas** | Profesores suben PDF/Word/Excel con descripción y fecha límite |
| 📥 **Entregar Tareas** | Estudiantes suben su trabajo |
| 🔔 **Notificaciones FCM** | Push notifications en tiempo real |
| ⭐ **Calificaciones** | Profesores califican y envían comentarios |

---

## 🚀 Configuración Paso a Paso

### 1. Crear Proyecto Firebase

1. Ve a **[console.firebase.google.com](https://console.firebase.google.com)**
2. Clic en **"Agregar proyecto"**
3. Nombre del proyecto: `TareaApp-DelPinar`
4. Desactiva Google Analytics (opcional)
5. Clic en **"Crear proyecto"**

### 2. Registrar la App Android

1. En tu proyecto Firebase, clic en el ícono **Android** (</> Android)
2. **Package name:** `com.tareaapp`
3. **Nombre:** TareaApp
4. Clic en **"Registrar app"**
5. **Descarga el archivo `google-services.json`** — ¡no lo pierdas!
6. Salta los pasos de SDK (ya están en el código)

### 3. Activar Servicios Firebase

#### 🔐 Authentication
1. Firebase Console → **Authentication** → **Comenzar**
2. Clic en **"Correo electrónico/contraseña"**
3. **Habilitar** → Guardar

#### 🗄️ Firestore Database
1. Firebase Console → **Firestore Database** → **Crear base de datos**
2. Modo: **Producción** (luego ajustamos reglas)
3. Región: `us-central1` (o la más cercana)

**Reglas de Firestore** (Firebase Console → Firestore → Reglas):
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Usuarios - solo el propio usuario puede leer/escribir su perfil
    match /usuarios/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Tareas - profesores crean, todos los autenticados leen
    match /tareas/{tareaId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        resource.data.profesorUid == request.auth.uid;
    }
    
    // Entregas - estudiantes crean las suyas, profesores leen todas
    match /entregas/{entregaId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
    }
    
    // Notificaciones - el destinatario las lee y actualiza
    match /notificaciones/{notifId} {
      allow read, write: if request.auth != null && 
        resource.data.destinatarioUid == request.auth.uid;
      allow create: if request.auth != null;
    }
  }
}
```

#### 📦 Storage
1. Firebase Console → **Storage** → **Comenzar**
2. Modo producción → siguiente
3. Región: `us-central1`

**Reglas de Storage:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### 🔔 Cloud Messaging (FCM)
- Se activa automáticamente. Solo necesitas configurar las **Cloud Functions** para enviar notificaciones server-side (ver sección avanzada abajo).

### 4. Configurar GitHub Actions

1. Ve a tu repositorio en GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Clic en **"New repository secret"**
4. Nombre: `GOOGLE_SERVICES_JSON`
5. Valor: **copia todo el contenido del archivo `google-services.json`** que descargaste
6. Clic en **"Add secret"**

### 5. Subir el Proyecto a GitHub

```bash
# 1. Ve a github.com y crea un nuevo repositorio (ej: TareaApp)

# 2. Clona este proyecto o inicializa git
git init
git add .
git commit -m "feat: Initial TareaApp commit 🎓"

# 3. Conecta con tu repositorio
git remote add origin https://github.com/TU_USUARIO/TareaApp.git
git branch -M main
git push -u origin main
```

4. ¡El build se dispara automáticamente!
5. Ve a **GitHub → Actions** para ver el progreso
6. Cuando termine, descarga el APK desde **Artifacts → TareaApp-debug**

---

## 🗂️ Estructura de Firestore

```
/usuarios/{uid}
  ├── uid: String
  ├── email: String
  ├── nombre: String
  ├── apellido: String
  ├── rol: "estudiante" | "profesor"
  ├── nivel: "primaria" | "bachillerato"
  ├── grado: Int (1-9)
  ├── asignaturas: List<String>
  ├── fcmToken: String
  └── createdAt: Long

/tareas/{tareaId}
  ├── id: String
  ├── titulo: String
  ├── descripcion: String
  ├── asignatura: String
  ├── grado: Int
  ├── periodo: Int (1-4)
  ├── profesorUid: String
  ├── profesorNombre: String
  ├── archivoUrl: String
  ├── archivoNombre: String
  ├── archivoTipo: "pdf" | "docx" | "xlsx"
  ├── fechaLimite: Long (timestamp)
  ├── createdAt: Long
  └── activa: Boolean

/entregas/{entregaId}
  ├── id: String
  ├── tareaId: String
  ├── estudianteUid: String
  ├── estudianteNombre: String
  ├── profesorUid: String
  ├── asignatura: String
  ├── grado: Int
  ├── periodo: Int
  ├── archivoUrl: String
  ├── archivoNombre: String
  ├── estado: "enviada" | "revisando" | "calificada"
  ├── calificacion: Double
  ├── comentarioProfesor: String
  ├── createdAt: Long
  └── revisadoAt: Long

/notificaciones/{notifId}
  ├── id: String
  ├── destinatarioUid: String
  ├── titulo: String
  ├── mensaje: String
  ├── tipo: "tarea_enviada" | "revisando" | "calificada" | "mensaje"
  ├── tareaId: String
  ├── submissionId: String
  ├── leida: Boolean
  └── createdAt: Long
```

---

## 🔔 Notificaciones Push (FCM Avanzado)

Para enviar notificaciones push reales (no solo in-app), necesitas **Firebase Cloud Functions**. Aquí el código Node.js:

```javascript
// functions/index.js
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.onNewEntrega = functions.firestore
  .document("entregas/{entregaId}")
  .onCreate(async (snap) => {
    const entrega = snap.data();
    const profesorDoc = await admin.firestore()
      .collection("usuarios").doc(entrega.profesorUid).get();
    const profesor = profesorDoc.data();
    if (!profesor?.fcmToken) return;

    await admin.messaging().send({
      token: profesor.fcmToken,
      notification: {
        title: "Nueva entrega recibida 📥",
        body: `${entrega.estudianteNombre} envió su tarea de ${entrega.asignatura}`,
      },
      data: { tareaId: entrega.tareaId, submissionId: snap.id },
    });
  });
```

```bash
# Instalar Firebase CLI y desplegar
npm install -g firebase-tools
firebase login
firebase init functions
firebase deploy --only functions
```

---

## 📱 Uso de la App

### Como Estudiante:
1. Regístrate con tu correo del colegio
2. Selecciona: Estudiante → Bachillerato → 8°
3. En el inicio verás solo tu grado (8°)
4. Toca el grado → selecciona una materia → elige el período
5. Ve las tareas publicadas por el profesor
6. Toca una tarea → sube tu archivo de entrega

### Como Profesor:
1. Regístrate con tu correo institucional
2. Selecciona: Profesor → tu nivel y grado principal
3. En el inicio verás todos los grados (1°-9°)
4. Navega a una asignatura → período
5. Toca el botón **"Publicar Tarea"** (FAB)
6. Llena el formulario y sube el archivo
7. Revisa las entregas desde el detalle de la tarea

---

## 🏫 Asignaturas Incluidas

| # | Asignatura | Emoji |
|---|---|---|
| 1 | Química | ⚗️ |
| 2 | Biología | 🧬 |
| 3 | Comprensión Lectora | 📖 |
| 4 | Matemáticas | 🔢 |
| 5 | Inglés | 🇬🇧 |
| 6 | Estadística | 📊 |
| 7 | Emprendimiento | 💡 |
| 8 | Informática | 💻 |
| 9 | Física | ⚡ |
| 10 | Ética | ⚖️ |
| 11 | Religión | ✝️ |
| 12 | Geometría | 📐 |
| 13 | Competencias Ciudadanas | 🏛️ |
| 14 | Lenguaje | ✏️ |
| 15 | Sociales | 🌎 |
| 16 | Artística | 🎨 |
| 17 | Educación Física | ⚽ |

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** XML + Material Design 3
- **Arquitectura:** Single Activity + Fragments + Navigation Component
- **Backend:** Firebase (Auth, Firestore, Storage, FCM)
- **Async:** Kotlin Coroutines
- **Build:** GitHub Actions → APK Debug

---

## 📋 Licencia

Desarrollado para I.E. Del Pinar — uso educativo interno.

---

*Hecho con ❤️ por un estudiante de 8° grado — I.E. Del Pinar, Colombia*
