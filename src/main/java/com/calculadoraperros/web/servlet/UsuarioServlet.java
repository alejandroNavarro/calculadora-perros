package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.UsuarioDAO;
import com.calculadoraperros.web.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp; // Asegúrate de que esta importación esté presente si Usuario la usa

/**
 * Servlet para manejar las operaciones de usuario (login y registro).
 */
@WebServlet({"/UsuarioServlet", "/registro", "/login"})
public class UsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    /**
     * Inicializa el Servlet y crea una instancia de UsuarioDAO.
     */
    public void init() {
        this.usuarioDAO = new UsuarioDAO();
        System.out.println("UsuarioServlet inicializado. Instancia de UsuarioDAO creada.");
    }

    /**
     * Maneja las solicitudes POST para login y registro.
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
        } else {
            // Acción no válida, redirigir a la página de inicio de sesión
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("UsuarioServlet - doPost: Acción no válida, redirigiendo a login.jsp");
        }
    }

    /**
     * Maneja la lógica de inicio de sesión.
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
            Usuario usuario = usuarioDAO.validarUsuario(email, password);
            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("message", "¡Bienvenido, " + usuario.getNombre() + "!");
                session.setAttribute("messageType", "success");
                
                // Redirigir directamente a panel.jsp
                response.sendRedirect(contextPath + "/MascotaServlet"); 
                System.out.println("UsuarioServlet - login: Login exitoso para usuario ID: " + usuario.getIdUsuario() + ". Redirigiendo a /panel.jsp.");
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
     * Maneja la lógica de registro de nuevo usuario.
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
            Usuario nuevoUsuario = new Usuario(nombre, email, password);
            // CAMBIO CLAVE AQUÍ: Se cambió 'registrarUsuario' a 'insertarUsuario'
            if (usuarioDAO.insertarUsuario(nuevoUsuario)) { 
                HttpSession session = request.getSession();
                session.setAttribute("usuario", nuevoUsuario);
                session.setAttribute("message", "¡Registro exitoso! Bienvenido, " + nuevoUsuario.getNombre() + ".");
                session.setAttribute("messageType", "success");
                
                // Redirigir directamente a panel.jsp
                response.sendRedirect(contextPath + "/panel.jsp");
                System.out.println("UsuarioServlet - registro: Registro exitoso para usuario ID: " + nuevoUsuario.getIdUsuario() + ". Redirigiendo a /panel.jsp.");
            } else {
                request.setAttribute("message", "Error al registrar. El email ya podría estar en uso.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/registro.jsp").forward(request, response);
                System.out.println("UsuarioServlet - registro: Error al registrar usuario con email: " + email);
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
