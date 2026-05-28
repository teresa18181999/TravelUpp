package com.travelupp.controller;

import com.google.gson.Gson;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/listar-viajes")
public class ListarViajesServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> respuesta = new HashMap<>();

        try {
            String estado = request.getParameter("estado");
            Integer usuarioId = null;
            if (request.getSession(false) != null) {
                Object attr = request.getSession().getAttribute("usuarioId");
                if (attr instanceof Integer) usuarioId = (Integer) attr;
            }

            ListarViajesDAO.autoCompletarViajes();

            ListarViajesDAO dao = new ListarViajesDAO();
            List<Viaje> viajes;
            if (usuarioId == null) {
                viajes = List.of();
            } else if (estado != null && !estado.isBlank()) {
                viajes = dao.listarPorUsuarioYEstado(usuarioId, estado);
            } else {
                viajes = dao.listarPorUsuario(usuarioId);
            }

            respuesta.put("ok", true);
            respuesta.put("total", viajes.size());

            UsuarioDAO usuarioDao = new UsuarioDAO();
            java.util.HashSet<String> todosTelefonos = new java.util.HashSet<>();
            java.util.Map<String, java.util.List<Viaje>> telefonosPorViaje = new java.util.HashMap<>();
            for (Viaje v : viajes) {
                String partes = v.getParticipantes();
                if (partes != null && !partes.isBlank()) {
                    String[] phones = partes.split(",");
                    for (String p : phones) {
                        String limpio = p.trim();
                        if (!limpio.isBlank()) {
                            todosTelefonos.add(limpio);
                            telefonosPorViaje.computeIfAbsent(v.getId() + ":" + limpio, k -> new java.util.ArrayList<>()).add(v);
                        }
                    }
                }
            }
            if (!todosTelefonos.isEmpty()) {
                java.util.Map<String, String> nombresMap = usuarioDao.obtenerNombresPorTelefonos(todosTelefonos);
                for (Viaje v : viajes) {
                    java.util.Map<String, String> subMap = new java.util.HashMap<>();
                    String partes = v.getParticipantes();
                    if (partes != null && !partes.isBlank()) {
                        for (String p : partes.split(",")) {
                            String limpio = p.trim();
                            if (!limpio.isBlank() && nombresMap.containsKey(limpio)) {
                                subMap.put(limpio, nombresMap.get(limpio));
                            }
                        }
                    }
                    v.setParticipantesNombres(subMap);
                }
            }

            Set<Integer> ownerIds = new HashSet<>();
            for (Viaje v : viajes) {
                if (v.getUsuarioId() != null) ownerIds.add(v.getUsuarioId());
            }
            Map<Integer, Usuario> ownerMap = new HashMap<>();
            for (Integer oid : ownerIds) {
                Usuario u = usuarioDao.obtenerPorId(oid);
                if (u != null) ownerMap.put(oid, u);
            }
            for (Viaje v : viajes) {
                if (v.getUsuarioId() != null && ownerMap.containsKey(v.getUsuarioId())) {
                    Usuario owner = ownerMap.get(v.getUsuarioId());
                    v.setCreadorNombre(owner.getNombre());
                    v.setCreadorTelefono(owner.getTelefono());
                }
            }

            respuesta.put("viajes", viajes);

        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error al listar viajes: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
