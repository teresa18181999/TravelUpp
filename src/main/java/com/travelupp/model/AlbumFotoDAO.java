package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumFotoDAO {

    public int crear(AlbumFoto foto) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO album_fotos (viaje_id, usuario_id, imagen) VALUES (?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, foto.getViajeId());
            stmt.setInt(2, foto.getUsuarioId());
            stmt.setString(3, foto.getImagen());
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<AlbumFoto> listarPorViaje(int viajeId) throws SQLException {
        List<AlbumFoto> fotos = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, viaje_id, usuario_id, imagen FROM album_fotos WHERE viaje_id = ? ORDER BY fecha_creacion DESC")) {
            stmt.setInt(1, viajeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlbumFoto f = new AlbumFoto();
                    f.setId(rs.getInt("id"));
                    f.setViajeId(rs.getInt("viaje_id"));
                    f.setUsuarioId(rs.getInt("usuario_id"));
                    f.setImagen(rs.getString("imagen"));
                    fotos.add(f);
                }
            }
        }
        return fotos;
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM album_fotos WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
