package com.travelupp.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ModificarViajeDAO {

    public boolean modificar(Viaje viaje) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE viajes SET nombre = ?, destino = ?, fecha_inicio = ?, fecha_fin = ?, participantes = ?, estado = ?, cover_image = ? WHERE id = ?")) {

            stmt.setString(1, viaje.getNombre());
            stmt.setString(2, viaje.getDestino());
            stmt.setDate(3, Date.valueOf(viaje.getFechaInicio()));
            stmt.setDate(4, Date.valueOf(viaje.getFechaFin()));
            stmt.setString(5, viaje.getParticipantes());
            stmt.setString(6, viaje.getEstado());
            stmt.setString(7, viaje.getCoverImage());
            stmt.setInt(8, viaje.getId());

            return stmt.executeUpdate() > 0;
        }
    }
}
