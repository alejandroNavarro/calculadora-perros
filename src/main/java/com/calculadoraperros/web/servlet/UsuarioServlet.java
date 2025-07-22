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
import com.google.api.client.json.jackson2.JacksonFactory; // O JacksonFactory.getDefaultInstance() si usas otra versión
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

    // REEMPLAZA ESTO con el ID de Cliente de Google REAL que obtuviste de Google Cloud Console
    // Debe ser el mismo que usas en tu registro.jsp y login.jsp
    private static final String GOOGLE_CLIENT_ID = "TU_CLIENT_ID_DE_GOOGLE.apps.googleusercontent.com"; // <-- ¡IMPORTANTE!

    private GoogleIdTokenVerifier verifier; // Declarado aquí para ser accesible en toda la clase

    /**
     * Inicializa el Servlet y crea una instancia de UsuarioDAO y el verificador de Google.
     */
    public void init() {
        this.usuarioDAO = new UsuarioDAO();
        // Inicializar el verificador de tokens de Google
        // Asegúrate de que JacksonFactory.getDefaultInstance() sea compatible con tu versión de Jackson
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
        } else if ("registro".equals(action)) {
            registro(request, response);
        } else if ("googleRegister".equals(action)) { // ¡NUEVA ACCIÓN PARA REGISTRO CON GOOGLE!
            handleGoogleRegister(request, response);
        } else if ("googleLogin".equals(action)) { // Si también lo usas para login.jsp
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
        String contextPath = request.getContextPath();

        if (nombre == null || nombre.trim().isEmpty() || email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("message", "Todos los campos son obligatorios.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            System.out.println("UsuarioServlet - registro: Campos vacíos.");
            return;
        }

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
            // CAMBIO CLAVE AQUÍ: Se cambió 'registrarUsuario' a 'insertarUsuario'
            if (usuarioDAO.insertarUsuario(nuevoUsuario)) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", nuevoUsuario); // Guarda el objeto Usuario completo
                session.setAttribute("message", "¡Registro exitoso! Bienvenido, " + nuevoUsuario.getNombre() + ".");
                session.setAttribute("messageType", "success");
                
                response.sendRedirect(contextPath + "/MascotaServlet"); // Redirigir a MascotaServlet o tu panel principal
                System.out.println("UsuarioServlet - registro: Registro exitoso para usuario ID: " + nuevoUsuario.getIdUsuario() + ". Redirigiendo a /MascotaServlet.");
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

        if (idTokenString == null || idTokenString.isEmpty()) {
            responseMap.put("success", "false");
            responseMap.put("message", "No se recibió el token de Google.");
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
                    responseMap.put("redirectUrl", contextPath + "/MascotaServlet"); // Redirige al panel principal
                    System.out.println("UsuarioServlet - handleGoogleRegister: Nuevo usuario Google registrado y logueado: " + email);

                } else {
                    // El usuario YA existe, simplemente lo logueamos
                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioExistente);
                    session.setAttribute("message", "¡Ya tienes una cuenta! Hemos iniciado sesión con Google.");
                    session.setAttribute("messageType", "info"); // Mensaje informativo
                    responseMap.put("success", "true");
                    responseMap.put("redirectUrl", contextPath + "/MascotaServlet"); // Redirige al panel principal
                    System.out.println("UsuarioServlet - handleGoogleRegister: Usuario Google existente logueado: " + email);
                }

            } else {
                responseMap.put("success", "false");
                responseMap.put("message", "Token de Google no válido.");
                System.err.println("UsuarioServlet - handleGoogleRegister: Token de Google no válido.");
            }
        } catch (SQLException e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error de base de datos al procesar el registro con Google: " + e.getMessage());
            System.err.println("UsuarioServlet - handleGoogleRegister: Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            responseMap.put("success", "false");
            responseMap.put("message", "Error interno del servidor al procesar el registro con Google: " + e.getMessage());
            System.err.println("UsuarioServlet - handleGoogleRegister: Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(responseMap));
            out.flush();
        }
    }

    // Si tienes un método handleGoogleLogin para login.jsp, aquí estaría
    private void handleGoogleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lógica similar a handleGoogleRegister, pero quizás con mensajes ligeramente diferentes
        // y sin intentar "registrar" si el usuario no existe (solo loguear si ya está)
        // O simplemente puedes hacer que handleGoogleRegister sea el único punto de entrada para ambos casos.
        // Por simplicidad, si solo usas el botón en registro.jsp, este método podría no ser necesario.
        // Si lo necesitas, su implementación sería casi idéntica a handleGoogleRegister, pero quizás
        // la redirección inicial si el usuario no existe podría ser a registro.jsp en lugar de loguearlo directamente.
        // Para este ejercicio, asumiremos que handleGoogleRegister es el principal.
        
        // Si no lo usas, puedes eliminar esta declaración o dejarla vacía.
        // Si lo usas, asegúrate de que esté implementado.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("success", "false");
        responseMap.put("message", "handleGoogleLogin no implementado o no es el punto de entrada principal.");
        out.print(gson.toJson(responseMap));
        out.flush();
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