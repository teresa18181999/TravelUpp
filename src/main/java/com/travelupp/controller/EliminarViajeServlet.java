package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.EliminarViajeDAO;
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

@WebServlet("/api/eliminar-viaje")
public class EliminarViajeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
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
            int id = ((Double) datos.get("id")).intValue();

            EliminarViajeDAO dao = new EliminarViajeDAO();
            boolean exito = dao.eliminar(id);

            if (exito) {
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Viaje eliminado correctamente");
            } else {
                respuesta.put("ok", false);
                respuesta.put("error", "No se pudo eliminar el viaje");
            }

        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error al eliminar viaje: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
