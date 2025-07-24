package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.UsuarioDAO;
import com.calculadoraperros.web.modelo.Usuario;

// Importaciones de Jakarta Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Importaciones para Google Sign-In
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson; // Para manejar respuestas JSON
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet para manejar las operaciones de usuario (login, registro y social login).
 */
@WebServlet({"/UsuarioServlet", "/registro", "/login"})
public class UsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    // ¡IMPORTANTE! REEMPLAZA ESTO con el ID de Cliente de Google REAL que obtuviste de Google Cloud Console
    // Debe ser el mismo que usas en tu registro.jsp y login.jsp
    private static final String GOOGLE_CLIENT_ID = "595405886937-tf6nba4e60c0dvu6t9n8u5a5cd4nruju.apps.googleusercontent.com"; 

    private GoogleIdTokenVerifier verifier; // Declarado aquí para ser accesible en toda la clase

    /**
     * Inicializa el Servlet y crea una instancia de UsuarioDAO y el verificador de Google.
     */
    public void init() {
        this.usuarioDAO = new UsuarioDAO();
        // Inicializar el verificador de tokens de Google
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
        System.out.println("UsuarioServlet inicializado. Instancia de UsuarioDAO y Google Verifier creados.");
    }

    /**
     * Maneja las solicitudes POST para login, registro y social login.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        System.out.println("UsuarioServlet - doPost: Acción recibida = " + action);

        if ("login".equals(action)) {
            login(request, response);
        } else if ("register".equals(action)) {
            registro(request, response);
        } else if ("googleRegister".equals(action)) {
            handleGoogleRegister(request, response);
        } else if ("googleLogin".equals(action)) {
            handleGoogleLogin(request, response);
        }
        else {
            // Acción no válida, redirigir a la página de inicio de sesión
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("UsuarioServlet - doPost: Acción no válida, redirigiendo a login.jsp");
        }
    }

    /**
     * Maneja la lógica de inicio de sesión tradicional.
     * @param request Objeto HttpServletRequest.
     * @param response Objeto HttpServletResponse.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath();

        // Almacenar los valores para precargar el formulario en caso de error
        request.setAttribute("oldEmail", email); // Guardar email para precargar

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("message", "Por favor, introduce tu email y contraseña.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            System.out.println("UsuarioServlet - login: Email o contraseña vacíos.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.validarUsuario(email, password); // Asume que validarUsuario maneja el hashing
            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario); // Guarda el objeto Usuario completo
                session.setAttribute("message", "¡Bienvenido, " + usuario.getNombre() + "!");
                session.setAttribute("messageType", "success");
                
                response.sendRedirect(contextPath + "/MascotaServlet"); // Redirigir a MascotaServlet o tu panel principal
                System.out.println("UsuarioServlet - login: Login exitoso para usuario ID: " + usuario.getIdUsuario() + ". Redirigiendo a /MascotaServlet.");
            } else {
                request.setAttribute("message", "Credenciales inválidas. Inténtalo de nuevo.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                System.out.println("UsuarioServlet - login: Credenciales inválidas para email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos durante el login: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            System.err.println("UsuarioServlet - login: Error SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado durante el login: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            System.err.println("UsuarioServlet - login: Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Maneja la lógica de registro de nuevo usuario tradicional.
     * @param request Objeto HttpServletRequest.
     * @param response Objeto HttpServletResponse.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    private void registro(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword"); // Capturar confirmación de contraseña
        String contextPath = request.getContextPath();

        // Almacenar los valores para precargar el formulario en caso de error
        request.setAttribute("oldNombre", nombre);
        request.setAttribute("oldEmail", email);

        // --- Validación del lado del servidor ---
        if (nombre == null || nombre.trim().isEmpty()) {
            request.setAttribute("message", "El nombre es obligatorio.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Nombre vacío.");
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("message", "El correo electrónico es obligatorio.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Email vacío.");
            return;
        } else if (!email.matches("\\S+@\\S+\\.\\S+")) { // Validación básica de formato de email
            request.setAttribute("message", "Introduce un formato de correo electrónico válido.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Formato de email inválido: " + email);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("message", "La contraseña es obligatoria.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Contraseña vacía.");
            return;
        } else if (password.length() < 8) { // Validación de longitud mínima de contraseña
            request.setAttribute("message", "La contraseña debe tener al menos 8 caracteres.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Contraseña demasiado corta.");
            return;
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) { // Validación de coincidencia de contraseñas
            request.setAttribute("message", "Las contraseñas no coinciden.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Las contraseñas no coinciden.");
            return;
        }
        // --- Fin Validación del lado del servidor ---

        try {
            // Antes de insertar, verifica si el email ya existe para evitar duplicados
            if (usuarioDAO.obtenerUsuarioPorEmail(email) != null) {
                request.setAttribute("message", "Error al registrar. El email ya está en uso.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/registro.jsp").forward(request, response);
                System.out.println("UsuarioServlet - registro: Email ya en uso: " + email);
                return;
            }

            Usuario nuevoUsuario = new Usuario(nombre, email, password); // Asume que el constructor o DAO maneja el hashing
            if (usuarioDAO.insertarUsuario(nuevoUsuario)) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", nuevoUsuario); // Guarda el objeto Usuario completo
                session.setAttribute("message", "¡Registro exitoso! Bienvenido, " + nuevoUsuario.getNombre() + ".");
                session.setAttribute("messageType", "success");
                
                // REDIRIGIR A LA PÁGINA DE BIENVENIDA DESPUÉS DEL REGISTRO TRADICIONAL
                response.sendRedirect(contextPath + "/welcome.jsp"); 
                System.out.println("UsuarioServlet - registro: Registro exitoso para usuario ID: " + nuevoUsuario.getIdUsuario() + ". Redirigiendo a /welcome.jsp.");
            } else {
                request.setAttribute("message", "Error al registrar. Inténtalo de nuevo más tarde.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/registro.jsp").forward(request, response);
                System.out.println("UsuarioServlet - registro: Error al insertar usuario con email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos durante el registro: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.err.println("UsuarioServlet - registro: Error SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado durante el registro: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.err.println("UsuarioServlet - registro: Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Maneja la lógica de registro/login con Google.
     * Este método es llamado desde registro.jsp cuando el usuario usa Google Sign-In.
     * @param request Objeto HttpServletRequest.
     * @param response Objeto HttpServletResponse.
     * @throws IOException Si ocurre un error de E/S al escribir la respuesta JSON.
     */
    private void handleGoogleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idTokenString = request.getParameter("id_token");
        Gson gson = new Gson();
        Map<String, String> responseMap = new HashMap<>();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath(); // Para redirecciones

        System.out.println("handleGoogleRegister: Recibido id_token: " + (idTokenString != null ? idTokenString.substring(0, Math.min(idTokenString.length(), 50)) + "..." : "null")); // Log truncado

        if (idTokenString == null || idTokenString.isEmpty()) {
            responseMap.put("success", "false");
            responseMap.put("message", "No se recibió el token de Google.");
            System.err.println("handleGoogleRegister: Error: Token de Google es nulo o vacío.");
            out.print(gson.toJson(responseMap));
            out.flush();
            return;
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                // String pictureUrl = (String) payload.get("picture"); // Puedes obtener la URL de la imagen si la necesitas

                System.out.println("handleGoogleRegister: Token verificado. Email: " + email + ", Nombre: " + name);

                Usuario usuarioExistente = usuarioDAO.obtenerUsuarioPorEmail(email); // Asume este método en tu DAO

                if (usuarioExistente == null) {
                    // El usuario NO existe, procedemos a REGISTRARLO
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(email);
                    nuevoUsuario.setNombre(name != null ? name : "Usuario Google");
                    nuevoUsuario.setPassword(""); // Deja la contraseña vacía o un marcador especial para social login
                    nuevoUsuario.setRol("user"); // Asigna un rol por defecto

                    usuarioDAO.insertarUsuario(nuevoUsuario); // Registra en tu DB
                    
                    // Obtener el usuario completo con el ID generado después de la inserción
                    Usuario usuarioLogueado = usuarioDAO.obtenerUsuarioPorEmail(email); 

                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioLogueado);
                    session.setAttribute("message", "¡Registro exitoso con Google! Bienvenido, " + usuarioLogueado.getNombre() + ".");
                    session.setAttribute("messageType", "success");
                    responseMap.put("success", "true");
                    // REDIRIGIR A LA PÁGINA DE BIENVENIDA DESPUÉS DEL REGISTRO CON GOOGLE
                    responseMap.put("redirectUrl", contextPath + "/welcome.jsp"); 
                    System.out.println("UsuarioServlet - handleGoogleRegister: Nuevo usuario Google registrado y logueado: " + email + ". Redirigiendo a /welcome.jsp.");

                } else {
                    // El usuario YA existe, simplemente lo logueamos
                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioExistente);
                    session.setAttribute("message", "¡Ya tienes una cuenta! Hemos iniciado sesión con Google.");
                    session.setAttribute("messageType", "info"); // Mensaje informativo
                    responseMap.put("success", "true");
                    responseMap.put("redirectUrl", contextPath + "/MascotaServlet"); // Redirige al panel principal
                    System.out.println("UsuarioServlet - handleGoogleRegister: Usuario Google existente logueado: " + email + ". Redirigiendo a /MascotaServlet.");
                }

            } else {
                responseMap.put("success", "false");
                responseMap.put("message", "Token de Google no válido o verificado.");
                System.err.println("handleGoogleRegister: Error: Token de Google no válido o verificación fallida.");
            }
        } catch (SQLException e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error de base de datos al procesar el registro con Google: " + e.getMessage());
            System.err.println("handleGoogleRegister: Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error interno del servidor al procesar el registro con Google: " + e.getMessage());
            System.err.println("handleGoogleRegister: Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(responseMap));
            out.flush();
        }
    }

    /**
     * Maneja la lógica de inicio de sesión con Google.
     * Este método es un placeholder, ya que handleGoogleRegister puede manejar ambos casos.
     * Si no hay una necesidad específica de un flujo de login de Google separado, puede ser eliminado.
     * @param request Objeto HttpServletRequest.
     * @param response Objeto HttpServletResponse.
     * @throws IOException Si ocurre un error de E/S al escribir la respuesta JSON.
     */
    private void handleGoogleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idTokenString = request.getParameter("id_token");
        Gson gson = new Gson();
        Map<String, String> responseMap = new HashMap<>();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();

        System.out.println("handleGoogleLogin: Recibido id_token: " + (idTokenString != null ? idTokenString.substring(0, Math.min(idTokenString.length(), 50)) + "..." : "null")); // Log truncado

        if (idTokenString == null || idTokenString.isEmpty()) {
            responseMap.put("success", "false");
            responseMap.put("message", "No se recibió el token de Google.");
            System.err.println("handleGoogleLogin: Error: Token de Google es nulo o vacío.");
            out.print(gson.toJson(responseMap));
            out.flush();
            return;
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                System.out.println("handleGoogleLogin: Token verificado. Email: " + email + ", Nombre: " + name);

                Usuario usuarioExistente = usuarioDAO.obtenerUsuarioPorEmail(email);

                if (usuarioExistente == null) {
                    // Si el usuario no existe, lo registramos y redirigimos a la página de bienvenida
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(email);
                    nuevoUsuario.setNombre(name != null ? name : "Usuario Google");
                    nuevoUsuario.setPassword(""); // Sin contraseña para social login
                    nuevoUsuario.setRol("user");

                    usuarioDAO.insertarUsuario(nuevoUsuario);
                    usuarioExistente = usuarioDAO.obtenerUsuarioPorEmail(email); // Obtener con ID generado
                    
                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioExistente);
                    session.setAttribute("message", "¡Registro exitoso con Google! Bienvenido, " + usuarioExistente.getNombre() + ".");
                    session.setAttribute("messageType", "success");
                    responseMap.put("success", "true");
                    // REDIRIGIR A LA PÁGINA DE BIENVENIDA DESPUÉS DEL REGISTRO CON GOOGLE (si no existe)
                    responseMap.put("redirectUrl", contextPath + "/welcome.jsp");
                    System.out.println("UsuarioServlet - handleGoogleLogin: Nuevo usuario Google registrado y logueado: " + email + ". Redirigiendo a /welcome.jsp.");

                } else {
                    // El usuario ya existe, simplemente lo logueamos y redirigimos a MascotaServlet
                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioExistente);
                    session.setAttribute("message", "¡Bienvenido de nuevo, " + usuarioExistente.getNombre() + "!");
                    session.setAttribute("messageType", "success");
                    responseMap.put("success", "true");
                    responseMap.put("redirectUrl", contextPath + "/MascotaServlet");
                    System.out.println("UsuarioServlet - handleGoogleLogin: Usuario Google existente logueado: " + email + ". Redirigiendo a /MascotaServlet.");
                }

            } else {
                responseMap.put("success", "false");
                responseMap.put("message", "Token de Google no válido o verificado.");
                System.err.println("handleGoogleLogin: Error: Token de Google no válido o verificación fallida.");
            }
        } catch (SQLException e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error de base de datos al procesar el login con Google: " + e.getMessage());
            System.err.println("handleGoogleLogin: Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error interno del servidor al procesar el login con Google: " + e.getMessage());
            System.err.println("handleGoogleLogin: Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(responseMap));
            out.flush();
        }
    }


    /**
     * Maneja las solicitudes GET (no se usan para login/registro en este diseño).
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/login.jsp"); // Redirigir a login si se accede directamente por GET
        System.out.println("UsuarioServlet - doGet: Acceso por GET, redirigiendo a login.jsp");
    }
}
