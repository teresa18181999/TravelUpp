package com.travelupp.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrearViajeDAO {

    public int crear(Viaje viaje) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO viajes (nombre, destino, fecha_inicio, fecha_fin, participantes, usuario_id, estado, cover_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, viaje.getNombre());
            stmt.setString(2, viaje.getDestino());
            stmt.setDate(3, Date.valueOf(viaje.getFechaInicio()));
            stmt.setDate(4, Date.valueOf(viaje.getFechaFin()));
            stmt.setString(5, viaje.getParticipantes());
            if (viaje.getUsuarioId() != null) {
                stmt.setInt(6, viaje.getUsuarioId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setString(7, viaje.getEstado() != null ? viaje.getEstado() : "En preparación");
            stmt.setString(8, viaje.getCoverImage());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return 0;
    }
}
