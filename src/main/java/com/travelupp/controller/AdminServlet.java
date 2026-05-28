package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.EliminarViajeDAO;
import com.travelupp.model.ListarViajesDAO;
import com.travelupp.model.Usuario;
import com.travelupp.model.UsuarioDAO;
import com.travelupp.model.Viaje;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private boolean esAdmin(HttpServletRequest request) {
        try {
            if (request.getSession(false) != null) {
                Object attr = request.getSession().getAttribute("usuarioId");
                if (attr instanceof Integer) {
                    UsuarioDAO dao = new UsuarioDAO();
                    Usuario usuario = dao.obtenerPorId((Integer) attr);
                    return usuario != null && usuario.isEsAdmin();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            if (!esAdmin(request)) {
                respuesta.put("ok", false);
                respuesta.put("error", "Acceso denegado");
                response.getWriter().print(gson.toJson(respuesta));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null) pathInfo = "/";

            switch (pathInfo) {
                case "/usuarios":
                    listarUsuarios(respuesta);
                    break;
                case "/viajes":
                    listarViajes(respuesta);
                    break;
                case "/stats":
                    obtenerEstadisticas(respuesta);
                    break;
                default:
                    respuesta.put("ok", false);
                    respuesta.put("error", "Ruta no válida");
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            if (!esAdmin(request)) {
                respuesta.put("ok", false);
                respuesta.put("error", "Acceso denegado");
                response.getWriter().print(gson.toJson(respuesta));
                return;
            }

            String pathInfo = request.getPathInfo();
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<String, Object> datos = gson.fromJson(sb.toString(), Map.class);

            if (pathInfo != null && pathInfo.equals("/usuarios")) {
                int userId = ((Double) datos.get("id")).intValue();
                UsuarioDAO dao = new UsuarioDAO();
                respuesta.put("ok", dao.eliminarUsuario(userId));
            } else if (pathInfo != null && pathInfo.equals("/viajes")) {
                int tripId = ((Double) datos.get("id")).intValue();
                EliminarViajeDAO dao = new EliminarViajeDAO();
                respuesta.put("ok", dao.eliminar(tripId));
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "Ruta no válida");
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }

    private void listarUsuarios(Map<String, Object> respuesta) throws Exception {
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> usuarios = dao.listarTodos();
        respuesta.put("ok", true);
        respuesta.put("total", usuarios.size());
        respuesta.put("usuarios", usuarios);
    }

    private void listarViajes(Map<String, Object> respuesta) throws Exception {
        ListarViajesDAO dao = new ListarViajesDAO();
        List<Viaje> viajes = dao.listarTodos();
        respuesta.put("ok", true);
        respuesta.put("total", viajes.size());
        respuesta.put("viajes", viajes);
    }

    private void obtenerEstadisticas(Map<String, Object> respuesta) throws Exception {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        ListarViajesDAO viajesDAO = new ListarViajesDAO();
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        List<Viaje> viajes = viajesDAO.listarTodos();

        long activos = viajes.stream().filter(v -> "En preparación".equals(v.getEstado()) || "En curso".equals(v.getEstado())).count();
        long completados = viajes.stream().filter(v -> "Completado".equals(v.getEstado())).count();

        respuesta.put("ok", true);
        respuesta.put("totalUsuarios", usuarios.size());
        respuesta.put("totalViajes", viajes.size());
        respuesta.put("viajesActivos", activos);
        respuesta.put("viajesCompletados", completados);
    }
}
