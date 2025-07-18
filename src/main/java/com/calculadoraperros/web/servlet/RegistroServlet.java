package com.calculadoraperros.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Importar HttpSession
import java.io.IOException;
import java.sql.SQLException; // Importar SQLException

import com.calculadoraperros.web.dao.UsuarioDAO;
import com.calculadoraperros.web.modelo.Usuario;

/**
 * Servlet para manejar el registro de nuevos usuarios.
 */
@WebServlet("/RegistroServlet")
public class RegistroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    /**
     * Inicializa el Servlet y crea una instancia de UsuarioDAO.
     */
    public void init() {
        usuarioDAO = new UsuarioDAO();
    }

    /**
     * Maneja las solicitudes POST del formulario de registro.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Asegurar la codificación UTF-8 para evitar problemas con caracteres especiales
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Obtener la sesión actual o crear una nueva si no existe
        HttpSession session = request.getSession();

        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password"); // En una aplicación real, esta contraseña debería ser hasheada

        // Validar que los campos no estén vacíos (aunque el HTML ya tiene 'required')
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("message", "Todos los campos son obligatorios.");
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response); // Usar ruta absoluta
            return; // Detener la ejecución
        }

        try {
            // 1. Verificar si el correo electrónico ya existe
            if (usuarioDAO.existeEmail(email)) { // Este método ahora lanza SQLException
                request.setAttribute("message", "El correo electrónico ya está registrado. Por favor, usa otro o inicia sesión.");
                request.setAttribute("messageType", "danger");
                request.getRequestDispatcher("/registro.jsp").forward(request, response); // Usar ruta absoluta
            } else {
                // 2. Si el correo no existe, crear un nuevo usuario
                // En una aplicación real, aquí se hashearía la contraseña antes de guardarla
                Usuario nuevoUsuario = new Usuario(nombre, email, password);

                // 3. Intentar insertar el usuario en la base de datos
                boolean registrado = usuarioDAO.insertarUsuario(nuevoUsuario); // Este método ahora lanza SQLException

                if (registrado) {
                    // Si el registro es exitoso, redirigir al login.jsp con un mensaje de éxito
                    // Usamos sendRedirect para cambiar la URL en el navegador y evitar reenvíos de formulario.
                    // Construimos la URL completa para asegurar que la redirección es correcta.
                    String contextPath = request.getContextPath(); // Obtiene /calculadora-perros-web
                    session.setAttribute("message", "¡Registro exitoso! Ahora puedes iniciar sesión."); // Guardar mensaje en sesión
                    session.setAttribute("messageType", "success"); // Guardar tipo de mensaje en sesión
                    response.sendRedirect(contextPath + "/login.jsp");
                } else {
                    request.setAttribute("message", "Error al registrar el usuario. Por favor, inténtalo de nuevo.");
                    request.setAttribute("messageType", "danger");
                    request.getRequestDispatcher("/registro.jsp").forward(request, response); // Usar ruta absoluta
                }
            }
        } catch (SQLException e) {
            // Captura cualquier SQLException que venga de UsuarioDAO
            e.printStackTrace(); // Imprimir la traza de la pila para depuración
            request.setAttribute("message", "Error de base de datos durante el registro: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada
            e.printStackTrace(); // Imprimir la traza de la pila para depuración
            request.setAttribute("message", "Ocurrió un error inesperado durante el registro: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
        }
    }
}
