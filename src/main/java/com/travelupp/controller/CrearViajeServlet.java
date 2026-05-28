package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.CrearViajeDAO;
import com.travelupp.model.InvitacionDAO;
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

@WebServlet("/api/crear-viaje")
public class CrearViajeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> respuesta = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<String, String> datos = gson.fromJson(sb.toString(), Map.class);

            Viaje viaje = new Viaje();
            viaje.setNombre(datos.get("nombre"));
            viaje.setDestino(datos.get("destino"));
            viaje.setFechaInicio(datos.get("fechaInicio"));
            viaje.setFechaFin(datos.get("fechaFin"));
            viaje.setParticipantes(datos.get("participantes"));
            viaje.setEstado(datos.get("estado"));
            viaje.setCoverImage(datos.get("coverImage"));

            Integer usuarioIdSesion = null;
            if (request.getSession(false) != null) {
                Object usuarioId = request.getSession().getAttribute("usuarioId");
                if (usuarioId instanceof Integer) {
                    usuarioIdSesion = (Integer) usuarioId;
                    viaje.setUsuarioId(usuarioIdSesion);
                }
            }

            CrearViajeDAO dao = new CrearViajeDAO();
            int idGenerado = dao.crear(viaje);

            if (idGenerado > 0) {
                int invitacionesEnviadas = 0;
                if (usuarioIdSesion != null) {
                    try {
                        InvitacionDAO invitacionDAO = new InvitacionDAO();
                        invitacionesEnviadas = invitacionDAO.sincronizarInvitacionesViaje(
                                idGenerado, usuarioIdSesion, viaje.getParticipantes());
                    } catch (Exception ie) {
                        invitacionesEnviadas = -1;
                    }
                }
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Viaje creado correctamente");
                respuesta.put("id", idGenerado);
                respuesta.put("invitacionesEnviadas", invitacionesEnviadas);
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo crear el viaje");
            }

        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error al crear viaje: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
