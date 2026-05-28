package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActividadDAO {

    public int crear(Actividad actividad) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO actividades (viaje_id, dia, tipo, hora, hora_fin, nombre, descripcion, imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, actividad.getViajeId());
            stmt.setInt(2, actividad.getDia());
            stmt.setString(3, actividad.getTipo());
            stmt.setString(4, actividad.getHora());
            stmt.setString(5, actividad.getHoraFin());
            stmt.setString(6, actividad.getNombre());
            stmt.setString(7, actividad.getDescripcion());
            stmt.setString(8, actividad.getImagen());
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Actividad> listarPorViaje(int viajeId) throws SQLException {
        List<Actividad> actividades = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, viaje_id, dia, tipo, hora, hora_fin, nombre, descripcion, imagen FROM actividades WHERE viaje_id = ? ORDER BY dia, hora")) {
            stmt.setInt(1, viajeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Actividad a = new Actividad();
                    a.setId(rs.getInt("id"));
                    a.setViajeId(rs.getInt("viaje_id"));
                    a.setDia(rs.getInt("dia"));
                    a.setTipo(rs.getString("tipo"));
                    a.setHora(rs.getString("hora"));
                    a.setHoraFin(rs.getString("hora_fin"));
                    a.setNombre(rs.getString("nombre"));
                    a.setDescripcion(rs.getString("descripcion"));
                    a.setImagen(rs.getString("imagen"));
                    actividades.add(a);
                }
            }
        }
        return actividades;
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM actividades WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorViaje(int viajeId) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM actividades WHERE viaje_id = ?")) {
            stmt.setInt(1, viajeId);
            return stmt.executeUpdate() > 0;
        }
    }
}
