package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContactoDAO {

    public List<Contacto> listarPorUsuario(int usuarioId) throws SQLException {
        List<Contacto> contactos = new ArrayList<>();
        String sql = "SELECT id, usuario_id, nombre, telefono FROM contactos WHERE usuario_id = ? ORDER BY nombre";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Contacto c = new Contacto();
                    c.setId(rs.getInt("id"));
                    c.setUsuarioId(rs.getInt("usuario_id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    contactos.add(c);
                }
            }
        }
        return contactos;
    }

    public boolean agregar(int usuarioId, String nombre, String telefono) throws SQLException {
        String sql = "INSERT INTO contactos (usuario_id, nombre, telefono) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombre);
            stmt.setString(3, telefono);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int usuarioId, String telefono) throws SQLException {
        String sql = "DELETE FROM contactos WHERE usuario_id = ? AND telefono = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, telefono);
            return stmt.executeUpdate() > 0;
        }
    }
}
