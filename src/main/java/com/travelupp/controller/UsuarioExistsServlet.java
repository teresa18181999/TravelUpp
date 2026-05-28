package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/usuarios/existe")
public class UsuarioExistsServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            String telefono = request.getParameter("telefono");

            if (telefono == null || telefono.isBlank()) {
                respuesta.put("ok", false);
                respuesta.put("error", "Debe indicar un número de teléfono");
            } else {
                UsuarioDAO dao = new UsuarioDAO();
                boolean existe = dao.existeTelefono(telefono);
                respuesta.put("ok", true);
                respuesta.put("existe", existe);
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error al verificar: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
