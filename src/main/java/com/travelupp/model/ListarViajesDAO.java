package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListarViajesDAO {

    public List<Viaje> listarTodos() throws SQLException {
        List<Viaje> viajes = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, destino, fecha_inicio, fecha_fin, participantes, usuario_id, estado, cover_image FROM viajes ORDER BY fecha_creacion DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                viajes.add(mapearViaje(rs));
            }
        }
        return viajes;
    }

    public List<Viaje> listarPorUsuario(int usuarioId) throws SQLException {
        List<Viaje> viajes = new ArrayList<>();
        String sql = "SELECT v.*, FALSE AS invitado FROM viajes v WHERE v.usuario_id = ? " +
                     "UNION " +
                     "SELECT v.*, TRUE AS invitado FROM viajes v " +
                     "INNER JOIN viaje_invitaciones vi ON vi.viaje_id = v.id " +
                     "WHERE vi.invitado_usuario_id = ? AND vi.estado = 'aceptada' " +
                     "ORDER BY fecha_creacion DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    viajes.add(mapearViaje(rs));
                }
            }
        }
        return viajes;
    }

    public List<Viaje> listarPorUsuarioYEstado(int usuarioId, String estado) throws SQLException {
        List<Viaje> viajes = new ArrayList<>();
        String sql = "SELECT v.*, FALSE AS invitado FROM viajes v WHERE v.usuario_id = ? AND v.estado = ? " +
                     "UNION " +
                     "SELECT v.*, TRUE AS invitado FROM viajes v " +
                     "INNER JOIN viaje_invitaciones vi ON vi.viaje_id = v.id " +
                     "WHERE vi.invitado_usuario_id = ? AND vi.estado = 'aceptada' AND v.estado = ? " +
                     "ORDER BY fecha_creacion DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, estado);
            stmt.setInt(3, usuarioId);
            stmt.setString(4, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    viajes.add(mapearViaje(rs));
                }
            }
        }
        return viajes;
    }

    public static void autoCompletarViajes() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE viajes SET estado = 'Completado' WHERE fecha_fin < CURDATE() AND estado != 'Completado'")) {
            stmt.executeUpdate();
        }
    }

    public Viaje buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT v.*, FALSE AS invitado FROM viajes v WHERE v.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearViaje(rs);
            }
        }
        return null;
    }

    private Viaje mapearViaje(ResultSet rs) throws SQLException {
        Viaje viaje = new Viaje();
        viaje.setId(rs.getInt("id"));
        viaje.setNombre(rs.getString("nombre"));
        viaje.setDestino(rs.getString("destino"));
        viaje.setFechaInicio(rs.getDate("fecha_inicio").toString());
        viaje.setFechaFin(rs.getDate("fecha_fin").toString());
        viaje.setParticipantes(rs.getString("participantes"));
        viaje.setUsuarioId(rs.getObject("usuario_id", Integer.class));
        viaje.setEstado(rs.getString("estado"));
        viaje.setCoverImage(rs.getString("cover_image"));
        try {
            viaje.setInvitado(rs.getBoolean("invitado"));
        } catch (SQLException e) {
            viaje.setInvitado(false);
        }
        return viaje;
    }
}
