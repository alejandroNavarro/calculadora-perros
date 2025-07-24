package com.calculadoraperros.web.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro para modificar las cabeceras Cross-Origin-Opener-Policy y Cross-Origin-Embedder-Policy.
 * Esto es necesario para que Google Sign-In funcione correctamente con iframes.
 */
@WebFilter(urlPatterns = {"/registro.jsp", "/login.jsp", "/UsuarioServlet"})
public class CrossOriginPolicyFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización del filtro, si es necesaria
        System.out.println("CrossOriginPolicyFilter inicializado.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Eliminar o relajar las cabeceras COOP/COEP
        // Google Sign-In a menudo requiere que COOP no esté configurada o esté en 'unsafe-none'
        // Y COEP no esté configurada o esté en 'unsafe-none'
        // Si tu aplicación no necesita estas cabeceras para otras funcionalidades de seguridad críticas,
        // eliminarlas completamente para estas rutas es la opción más sencilla para compatibilidad con GSI.
        httpResponse.setHeader("Cross-Origin-Opener-Policy", "unsafe-none"); // O simplemente removeHeader
        httpResponse.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none"); // O simplemente removeHeader

        // Si prefieres eliminar las cabeceras en lugar de establecerlas en 'unsafe-none':
        // httpResponse.setHeader("Cross-Origin-Opener-Policy", ""); // Establecer a vacío para eliminar
        // httpResponse.setHeader("Cross-Origin-Embedder-Policy", ""); // Establecer a vacío para eliminar
        // O directamente:
        // httpResponse.setHeader("Cross-Origin-Opener-Policy", null); // Esto no funciona en todos los servidores
        // httpResponse.setHeader("Cross-Origin-Embedder-Policy", null); // Esto no funciona en todos los servidores
        // La forma más robusta de "eliminar" es establecer a "unsafe-none" o no establecerla en absoluto
        // Si ya existen, setHeader las sobrescribe.

        System.out.println("CrossOriginPolicyFilter: Cabeceras COOP/COEP modificadas para la respuesta.");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Limpieza del filtro, si es necesaria
        System.out.println("CrossOriginPolicyFilter destruido.");
    }
}

