package com.travelupp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.travelupp.model.Actividad;
import com.travelupp.model.ActividadDAO;
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

@WebServlet("/api/actividades")
public class ActividadServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            String viajeIdStr = request.getParameter("viajeId");
            if (viajeIdStr == null || viajeIdStr.isBlank()) {
                respuesta.put("ok", false);
                respuesta.put("error", "Debe indicar viajeId");
            } else {
                int viajeId = Integer.parseInt(viajeIdStr);
                ActividadDAO dao = new ActividadDAO();
                List<Actividad> actividades = dao.listarPorViaje(viajeId);
                respuesta.put("ok", true);
                respuesta.put("actividades", actividades);
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
            BufferedReader reader = request.getReader();
            JsonObject datos = gson.fromJson(reader, JsonObject.class);

            Actividad actividad = new Actividad();
            actividad.setViajeId(datos.get("viajeId").getAsInt());
            actividad.setDia(datos.has("dia") ? datos.get("dia").getAsInt() : 1);
            actividad.setTipo(datos.has("tipo") ? datos.get("tipo").getAsString() : "actividad");
            actividad.setHora(datos.has("hora") ? datos.get("hora").getAsString() : "");
            actividad.setHoraFin(datos.has("horaFin") ? datos.get("horaFin").getAsString() : "");
            actividad.setNombre(datos.get("nombre").getAsString());
            actividad.setDescripcion(datos.has("descripcion") ? datos.get("descripcion").getAsString() : "");
            actividad.setImagen(datos.has("imagen") ? datos.get("imagen").getAsString() : null);

            ActividadDAO dao = new ActividadDAO();
            int id = dao.crear(actividad);

            if (id > 0) {
                actividad.setId(id);
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Actividad creada correctamente");
                respuesta.put("actividad", actividad);
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo crear la actividad");
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
            BufferedReader reader = request.getReader();
            JsonObject datos = gson.fromJson(reader, JsonObject.class);
            int id = datos.get("id").getAsInt();

            ActividadDAO dao = new ActividadDAO();
            if (dao.eliminar(id)) {
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Actividad eliminada correctamente");
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo eliminar la actividad");
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
