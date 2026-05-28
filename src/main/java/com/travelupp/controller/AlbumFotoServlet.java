package com.travelupp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.travelupp.model.AlbumFoto;
import com.travelupp.model.AlbumFotoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/album-fotos")
public class AlbumFotoServlet extends HttpServlet {

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
                AlbumFotoDAO dao = new AlbumFotoDAO();
                List<AlbumFoto> fotos = dao.listarPorViaje(viajeId);
                respuesta.put("ok", true);
                respuesta.put("fotos", fotos);
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
            Integer usuarioId = (Integer) request.getSession().getAttribute("usuarioId");
            if (usuarioId == null) {
                respuesta.put("ok", false);
                respuesta.put("error", "No autenticado");
                response.getWriter().print(gson.toJson(respuesta));
                return;
            }

            BufferedReader reader = request.getReader();
            JsonObject datos = gson.fromJson(reader, JsonObject.class);

            AlbumFoto foto = new AlbumFoto();
            foto.setViajeId(datos.get("viajeId").getAsInt());
            foto.setUsuarioId(usuarioId);
            foto.setImagen(datos.get("imagen").getAsString());

            AlbumFotoDAO dao = new AlbumFotoDAO();
            int id = dao.crear(foto);

            if (id > 0) {
                foto.setId(id);
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Foto subida correctamente");
                respuesta.put("foto", foto);
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo subir la foto");
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
