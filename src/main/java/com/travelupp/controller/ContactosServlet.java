package com.travelupp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.travelupp.model.Contacto;
import com.travelupp.model.ContactoDAO;
import com.travelupp.model.Usuario;
import com.travelupp.model.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/contactos")
public class ContactosServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private Integer obtenerUsuarioId(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            Object attr = request.getSession().getAttribute("usuarioId");
            if (attr instanceof Integer) return (Integer) attr;
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Integer usuarioId = obtenerUsuarioId(request);
            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "No autenticado");
            } else {
                ContactoDAO dao = new ContactoDAO();
                List<Contacto> contactos = dao.listarPorUsuario(usuarioId);
                respuesta.put("ok", true);
                respuesta.put("contactos", contactos);
                respuesta.put("total", contactos.size());
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
            Integer usuarioId = obtenerUsuarioId(request);
            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "No autenticado");
            } else {
                JsonObject datos = gson.fromJson(request.getReader(), JsonObject.class);
                String telefono = datos.has("telefono") ? datos.get("telefono").getAsString().trim() : "";

                if (telefono.isEmpty()) {
                    respuesta.put("ok", false);
                    respuesta.put("error", "Teléfono es obligatorio");
                } else {
                    UsuarioDAO usuarioDao = new UsuarioDAO();
                    if (!usuarioDao.existeTelefono(telefono)) {
                        respuesta.put("ok", false);
                        respuesta.put("error", "El teléfono no pertenece a un usuario registrado");
                    } else {
                        Usuario usuario = usuarioDao.obtenerPorTelefono(telefono);
                        String nombre = usuario != null ? usuario.getNombre() : telefono;
                        ContactoDAO dao = new ContactoDAO();
                        boolean exito = dao.agregar(usuarioId, nombre, telefono);
                        if (exito) {
                            respuesta.put("ok", true);
                            respuesta.put("nombre", nombre);
                            respuesta.put("mensaje", "Contacto añadido");
                        } else {
                            respuesta.put("ok", false);
                            respuesta.put("error", "No se pudo añadir el contacto");
                        }
                    }
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Este contacto ya existe");
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
            Integer usuarioId = obtenerUsuarioId(request);
            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "No autenticado");
            } else {
                String telefono = request.getParameter("telefono");
                if (telefono == null || telefono.isBlank()) {
                    respuesta.put("ok", false);
                    respuesta.put("error", "Teléfono es obligatorio");
                } else {
                    ContactoDAO dao = new ContactoDAO();
                    boolean exito = dao.eliminar(usuarioId, telefono);
                    if (exito) {
                        respuesta.put("ok", true);
                        respuesta.put("mensaje", "Contacto eliminado");
                    } else {
                        respuesta.put("ok", false);
                        respuesta.put("error", "Contacto no encontrado");
                    }
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
