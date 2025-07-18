package com.calculadoraperros.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import com.calculadoraperros.web.dao.UsuarioDAO;
import com.calculadoraperros.web.modelo.Usuario;

/**
 * Servlet para manejar el inicio de sesión de usuarios.
 * Este servlet es invocado por login.jsp.
 */
@WebServlet("/LoginServlet") // Mapea este servlet a la URL /LoginServlet
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    /**
     * Inicializa el Servlet y crea una instancia de UsuarioDAO.
     */
    public void init() {
        usuarioDAO = new UsuarioDAO();
        System.out.println("LoginServlet inicializado. Instancia de UsuarioDAO creada.");
    }

    /**
     * Maneja las solicitudes POST del formulario de inicio de sesión.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("LoginServlet - doPost: Intentando login para email: " + email);

        try {
            Usuario usuario = usuarioDAO.validarUsuario(email, password);

            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario); // Guardar el objeto Usuario en la sesión
                session.setAttribute("message", "¡Bienvenido, " + usuario.getNombre() + "!");
                session.setAttribute("messageType", "success");

                // Redirigir a MascotaServlet para que cargue las mascotas y luego muestre el panel
                String contextPath = request.getContextPath();
                response.sendRedirect(contextPath + "/MascotaServlet"); // REDIRECCIÓN CLAVE
                System.out.println("LoginServlet - doPost: Login exitoso para usuario ID: " + usuario.getIdUsuario() + ". Redirigiendo a MascotaServlet.");
            } else {
                request.setAttribute("message", "Email o contraseña incorrectos.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                System.out.println("LoginServlet - doPost: Login fallido para email: " + email);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado durante el inicio de sesión.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            System.err.println("LoginServlet - doPost: Error inesperado durante el login: " + e.getMessage());
        }
    }

    /**
     * Maneja las solicitudes GET. En este caso, simplemente redirige a login.jsp.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("login.jsp");
        System.out.println("LoginServlet - doGet: Redirigiendo a login.jsp.");
    }
}
