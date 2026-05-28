package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.Usuario;
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

@WebServlet("/api/verify-login")
public class VerifyLoginServlet extends HttpServlet {

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
            String codigo = datos.get("codigo");

            if (telefono == null || telefono.isBlank() || codigo == null || codigo.isBlank()) {
                respuesta.put("ok", false);
                respuesta.put("error", "Debe indicar teléfono y código de verificación");
            } else {
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.verificarCodigo(telefono, codigo)) {
                    Usuario usuario = dao.obtenerPorTelefono(telefono);
                    if (usuario != null) {
                        request.getSession(true).setAttribute("usuarioId", usuario.getId());
                        request.getSession().setAttribute("telefono", usuario.getTelefono());
                        request.getSession().setAttribute("nombre", usuario.getNombre());
                    }
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Verificación correcta");
                } else {
                    respuesta.put("ok", false);
                    respuesta.put("error", "Código de verificación incorrecto");
                }
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", "Error en verificación: " + e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        out.flush();
    }
}
