
# 🐾 Calculadora Perros – Plataforma de Alimentación y Salud Canina

## 📌 Descripción general

Esta es una aplicación web en desarrollo que permite calcular la cantidad diaria de comida adecuada para perros, registrar usuarios, sus mascotas y las vacunas o tratamientos aplicados. Diseñada para escalar hacia una plataforma personalizada, con funcionalidades futuras como avisos automáticos, planes premium, y seguimiento nutricional profesional.

---

## 🛠️ Tecnologías utilizadas

- **Backend**: Java (Jakarta EE), Servlets, JDBC
- **Frontend**: JSP + Bootstrap 5
- **Base de datos**: MySQL (Docker, puerto 3308)
- **Servidor**: Tomcat (puerto 8080)
- **Build**: Maven (`pom.xml`)
- **Estructura WAR**: `src/main/java` y `src/main/webapp`

---

## 📂 Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/calculadoraperros/web/
│   │       ├── dao/                # DAOs para acceso a BD
│   │       ├── modelo/             # Clases modelo (Usuario, Mascota)
│   │       ├── servlet/            # Lógica de los Servlets
│   │       └── util/               # ConexionDB.java
│   └── webapp/
│       ├── css/                    # Estilos Bootstrap y propios
│       ├── jsp/                    # Páginas JSP (si las organizas)
│       ├── index.jsp              # Página principal con la calculadora
│       ├── login.jsp, registro.jsp
│       ├── panel.jsp, editarMascota.jsp
│       ├── registroMascota.jsp
│       └── WEB-INF/web.xml        # Configuración de servlets
pom.xml                            # Archivo de construcción con Maven
```

---

## 🚀 Instrucciones para levantar el proyecto localmente

### 1. Requisitos previos

- Java JDK 11+
- Maven
- Tomcat (instalado o integrado)
- Docker (para la base de datos) o una instancia local de MySQL

### 2. Base de datos

- Puerto: `3308`
- Base de datos: `calculadora_perros`
- Usuario: `root`
- Contraseña: `tu_contraseña` (modificar en `ConexionDB.java`)

### 3. Levantar la base de datos (si usas Docker)

```bash
docker run --name mysql-calculadora -e MYSQL_ROOT_PASSWORD=tu_contraseña -e MYSQL_DATABASE=calculadora_perros -p 3308:3306 -d mysql:8
```

### 4. Importar estructura SQL

Asegúrate de tener un script con las tablas `usuarios`, `mascotas` y `dosis`. (¿Quieres que te ayude a generarlo?).

### 5. Compilar y ejecutar

```bash
mvn clean package
```

Despliega el `.war` generado en Tomcat o usa un IDE como IntelliJ/Eclipse con integración de Tomcat.

---

## ✅ Funcionalidades actuales

- Registro de usuarios y validación de correo único
- Registro de mascotas por usuario
- Registro de vacunas/dosis
- Calculadora básica de ración diaria
- Diseño adaptado a móvil con Bootstrap 5

---

## 🧭 Funcionalidades previstas (próximas fases)

- Login real con sesiones activas
- Lógica avanzada de nutrición canina (peso ideal, edad, raza, esterilización)
- Historial de peso y ajustes dinámicos
- Panel de usuario con alertas y dosis
- Soporte PWA (modo app desde el móvil)
- Sistema de notificaciones internas
- Subida de imagen y edición de perfil
- Emails reales mediante SMTP externo
- Monetización: PDFs, comunidad, recomendaciones personalizadas

---

## 📸 Capturas (opcional)
_Incluye aquí capturas de pantalla si las tienes, para mostrar el funcionamiento visual._

---

## 👤 Autor

Desarrollado por [Tu nombre o alias].  
Contacto: [Tu email o redes si deseas incluirlo].

---

## 📝 Licencia
Proyecto personal en desarrollo – todos los derechos reservados.
