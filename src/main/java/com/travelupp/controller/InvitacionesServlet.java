package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.ContactoDAO;
import com.travelupp.model.Invitacion;
import com.travelupp.model.InvitacionDAO;
import com.travelupp.model.ListarViajesDAO;
import com.travelupp.model.Usuario;
import com.travelupp.model.UsuarioDAO;
import com.travelupp.model.Viaje;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/invitaciones")
public class InvitacionesServlet extends HttpServlet {

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
                respuesta.put("ok", true);
                respuesta.put("total", 0);
                respuesta.put("invitaciones", List.of());
            } else {
                InvitacionDAO dao = new InvitacionDAO();
                List<Invitacion> invitaciones = dao.listarPendientesPorUsuario(usuarioId);
                respuesta.put("ok", true);
                respuesta.put("total", invitaciones.size());
                respuesta.put("invitaciones", invitaciones);
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                respuesta.put("error", "No autenticado");
            } else {
                Map<String, Object> datos = gson.fromJson(request.getReader(), Map.class);
                int viajeId = ((Double) datos.get("viajeId")).intValue();
                String accion = (String) datos.get("accion");

                InvitacionDAO dao = new InvitacionDAO();

                if ("salir".equals(accion)) {
                    dao.responderInvitacion(viajeId, usuarioId, false);
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Has salido del viaje");
                } else {
                    boolean aceptar = Boolean.TRUE.equals(datos.get("aceptar"));
                    dao.responderInvitacion(viajeId, usuarioId, aceptar);
                    if (aceptar) {
                        try {
                            ListarViajesDAO viajesDao = new ListarViajesDAO();
                            Viaje viaje = viajesDao.buscarPorId(viajeId);
                            if (viaje != null && viaje.getUsuarioId() != null) {
                                int ownerId = viaje.getUsuarioId();
                                UsuarioDAO usuarioDao = new UsuarioDAO();
                                ContactoDAO contactoDao = new ContactoDAO();
                                Usuario yo = usuarioDao.obtenerPorId(usuarioId);
                                Usuario owner = usuarioDao.obtenerPorId(ownerId);
                                if (yo != null && owner != null) {
                                    contactoDao.agregar(usuarioId, owner.getNombre(), owner.getTelefono());
                                    contactoDao.agregar(ownerId, yo.getNombre(), yo.getTelefono());
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    respuesta.put("ok", true);
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
