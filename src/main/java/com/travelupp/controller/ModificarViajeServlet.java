package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.InvitacionDAO;
import com.travelupp.model.ListarViajesDAO;
import com.travelupp.model.ModificarViajeDAO;
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
import java.util.Map;

@WebServlet("/api/modificar-viaje")
public class ModificarViajeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> respuesta = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<String, Object> datos = gson.fromJson(sb.toString(), Map.class);

            int viajeId = ((Double) datos.get("id")).intValue();

            ListarViajesDAO listarDAO = new ListarViajesDAO();
            Viaje existente = listarDAO.buscarPorId(viajeId);

            Viaje viaje = new Viaje();
            viaje.setId(viajeId);
            viaje.setNombre(datos.containsKey("nombre") ? (String) datos.get("nombre") : (existente != null ? existente.getNombre() : null));
            viaje.setDestino(datos.containsKey("destino") ? (String) datos.get("destino") : (existente != null ? existente.getDestino() : null));
            viaje.setFechaInicio(datos.containsKey("fechaInicio") ? (String) datos.get("fechaInicio") : (existente != null ? existente.getFechaInicio() : null));
            viaje.setFechaFin(datos.containsKey("fechaFin") ? (String) datos.get("fechaFin") : (existente != null ? existente.getFechaFin() : null));
            viaje.setParticipantes(datos.containsKey("participantes") ? (String) datos.get("participantes") : (existente != null ? existente.getParticipantes() : null));
            viaje.setEstado(datos.containsKey("estado") ? (String) datos.get("estado") : (existente != null ? existente.getEstado() : null));
            viaje.setCoverImage(datos.containsKey("coverImage") ? (String) datos.get("coverImage") : (existente != null ? existente.getCoverImage() : null));

            ModificarViajeDAO dao = new ModificarViajeDAO();
            boolean exito = dao.modificar(viaje);

            if (exito) {
                Integer usuarioIdSesion = null;
                if (request.getSession(false) != null) {
                    Object usuarioId = request.getSession().getAttribute("usuarioId");
                    if (usuarioId instanceof Integer) usuarioIdSesion = (Integer) usuarioId;
                }

                int invitacionesSincronizadas = 0;
                if (usuarioIdSesion != null) {
                    try {
                        InvitacionDAO invitacionDAO = new InvitacionDAO();
                        invitacionesSincronizadas = invitacionDAO.sincronizarInvitacionesViaje(
                                viaje.getId(), usuarioIdSesion, viaje.getParticipantes());
                    } catch (Exception ie) {
                        invitacionesSincronizadas = -1;
                    }
                }

                respuesta.put("ok", true);
                respuesta.put("mensaje", "Viaje modificado correctamente");
                respuesta.put("invitacionesSincronizadas", invitacionesSincronizadas);
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo modificar el viaje");
            }

        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error al modificar viaje: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
