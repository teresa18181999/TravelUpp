package com.travelupp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.travelupp.model.Usuario;
import com.travelupp.model.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/perfil")
public class PerfilServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Integer usuarioId = null;
            if (request.getSession(false) != null) {
                Object attr = request.getSession().getAttribute("usuarioId");
                if (attr instanceof Integer) usuarioId = (Integer) attr;
            }

            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "Usuario no autenticado");
            } else {
                UsuarioDAO dao = new UsuarioDAO();
                Usuario usuario = dao.obtenerPorId(usuarioId);
                if (usuario != null) {
                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("id", usuario.getId());
                    perfil.put("nombre", usuario.getNombre());
                    perfil.put("telefono", usuario.getTelefono());
                    perfil.put("email", usuario.getEmail() != null ? usuario.getEmail() : "");
                    perfil.put("fotoPerfil", usuario.getFotoPerfil());
                    perfil.put("esAdmin", usuario.isEsAdmin());
                    respuesta.put("ok", true);
                    respuesta.put("perfil", perfil);
                } else {
                    respuesta.put("ok", false);
                    respuesta.put("error", "Usuario no encontrado");
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Integer usuarioId = null;
            if (request.getSession(false) != null) {
                Object attr = request.getSession().getAttribute("usuarioId");
                if (attr instanceof Integer) usuarioId = (Integer) attr;
            }

            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "Usuario no autenticado");
            } else {
                BufferedReader reader = request.getReader();
                JsonObject datos = gson.fromJson(reader, JsonObject.class);

                String nombre = datos.has("nombre") ? datos.get("nombre").getAsString() : "";
                String email = datos.has("email") ? datos.get("email").getAsString() : "";
                String fotoPerfil = datos.has("fotoPerfil") && !datos.get("fotoPerfil").isJsonNull()
                        ? datos.get("fotoPerfil").getAsString() : null;

                UsuarioDAO dao = new UsuarioDAO();
                boolean actualizado = dao.actualizarPerfil(usuarioId, nombre, email, fotoPerfil);

                if (actualizado) {
                    request.getSession().setAttribute("nombre", nombre);
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Perfil actualizado correctamente");
                } else {
                    respuesta.put("ok", false);
                    respuesta.put("error", "No se pudo actualizar el perfil");
                }
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
            Integer usuarioId = null;
            if (request.getSession(false) != null) {
                Object attr = request.getSession().getAttribute("usuarioId");
                if (attr instanceof Integer) usuarioId = (Integer) attr;
            }

            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "Usuario no autenticado");
            } else {
                UsuarioDAO dao = new UsuarioDAO();
                boolean eliminado = dao.eliminarUsuario(usuarioId);

                if (eliminado) {
                    request.getSession().invalidate();
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Cuenta eliminada correctamente");
                } else {
                    respuesta.put("ok", false);
                    respuesta.put("error", "No se pudo eliminar la cuenta");
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
