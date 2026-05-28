package com.travelupp.controller;

import com.google.gson.Gson;
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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<String, String> datos = gson.fromJson(sb.toString(), Map.class);
            String telefono = datos.get("telefono");

            if (telefono == null || telefono.isBlank()) {
                respuesta.put("ok", false);
                respuesta.put("error", "Debe indicar un número de teléfono");
            } else {
                UsuarioDAO dao = new UsuarioDAO();
                if (!dao.existeTelefono(telefono)) {
                    respuesta.put("ok", false);
                    respuesta.put("error", "El teléfono no está registrado");
                } else {
                    dao.actualizarCodigo(telefono);
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Código de verificación enviado");
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error en login: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
