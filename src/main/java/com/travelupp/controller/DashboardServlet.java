package com.travelupp.controller;

import com.google.gson.Gson;
import com.travelupp.model.ListarViajesDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

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
                respuesta.put("ok", false);
                respuesta.put("error", "Usuario no autenticado");
            } else {
                ListarViajesDAO dao = new ListarViajesDAO();
                respuesta.put("ok", true);
                respuesta.put("totalViajes", dao.listarPorUsuario(usuarioId).size());
                respuesta.put("viajesEnCurso", dao.listarPorUsuarioYEstado(usuarioId, "En preparación").size());
            }
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("error", e.getMessage());
        }

        response.getWriter().print(gson.toJson(respuesta));
    }
}
