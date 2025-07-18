package com.calculadoraperros.web.servlet; // Usando el paquete correcto para Servlets

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet para manejar el cierre de sesión de usuarios.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Maneja las solicitudes GET para cerrar la sesión.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Obtener la sesión existente, no crear una nueva
        if (session != null) {
            session.invalidate(); // Invalidar la sesión
        }
        // Redirigir al usuario a la página de inicio de sesión o a la página principal
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/login.jsp"); // O a index.jsp si prefieres
    }

    /**
     * Maneja las solicitudes POST (aunque el botón de logout suele ser GET).
     * Simplemente llama al doGet.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
