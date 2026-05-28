package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvitacionDAO {

    public int sincronizarInvitacionesViaje(int viajeId, int invitadorUsuarioId, String participantes) throws SQLException {
        Set<Integer> invitadosObjetivo = resolverInvitados(participantes, invitadorUsuarioId);

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                eliminarInvitacionesQueYaNoAplican(conn, viajeId, invitadorUsuarioId, invitadosObjetivo);

                int invitacionesProcesadas = 0;
                if (!invitadosObjetivo.isEmpty()) {
                    String sqlInsert =
                            "INSERT INTO viaje_invitaciones (viaje_id, invitador_usuario_id, invitado_usuario_id, estado) " +
                            "VALUES (?, ?, ?, 'pendiente') " +
                            "ON DUPLICATE KEY UPDATE invitador_usuario_id = VALUES(invitador_usuario_id), estado = 'pendiente'";

                    try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                        for (Integer invitadoId : invitadosObjetivo) {
                            insertStmt.setInt(1, viajeId);
                            insertStmt.setInt(2, invitadorUsuarioId);
                            insertStmt.setInt(3, invitadoId);
                            insertStmt.executeUpdate();
                            invitacionesProcesadas++;
                        }
                    }
                }

                conn.commit();
                return invitacionesProcesadas;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Invitacion> listarPendientesPorUsuario(int usuarioId) throws SQLException {
        List<Invitacion> invitaciones = new ArrayList<>();
        String sql =
                "SELECT v.id AS viaje_id, " +
                "       COALESCE(NULLIF(v.nombre, ''), v.destino, 'Viaje') AS trip_name, " +
                "       DATE_FORMAT(v.fecha_inicio, '%d/%m/%Y') AS fecha_inicio_fmt, " +
                "       DATE_FORMAT(v.fecha_fin, '%d/%m/%Y') AS fecha_fin_fmt, " +
                "       COALESCE(v.participantes, '') AS participantes, " +
                "       COALESCE(v.cover_image, '') AS cover_image, " +
                "       COALESCE(u.nombre, '') AS invitador_nombre, " +
                "       vi.estado AS estado " +
                "FROM viaje_invitaciones vi " +
                "INNER JOIN viajes v ON v.id = vi.viaje_id " +
                "INNER JOIN usuarios u ON u.id = vi.invitador_usuario_id " +
                "WHERE vi.invitado_usuario_id = ? AND vi.estado = 'pendiente' " +
                "ORDER BY vi.fecha_creacion DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invitacion inv = new Invitacion();
                    int viajeId = rs.getInt("viaje_id");
                    inv.setId(String.valueOf(viajeId));
                    inv.setViajeId(viajeId);
                    inv.setTripName(rs.getString("trip_name"));
                    inv.setDates(rs.getString("fecha_inicio_fmt") + " - " + rs.getString("fecha_fin_fmt"));
                    inv.setInvitedBy(rs.getString("invitador_nombre"));
                    inv.setParticipants(parseParticipantes(rs.getString("participantes")));
                    inv.setImage(rs.getString("cover_image"));
                    inv.setType("manual");
                    inv.setStatus(rs.getString("estado"));
                    invitaciones.add(inv);
                }
            }
        }
        return invitaciones;
    }

    public void responderInvitacion(int viajeId, int usuarioId, boolean aceptar) throws SQLException {
        String nuevoEstado = aceptar ? "aceptada" : "rechazada";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE viaje_invitaciones SET estado = ? WHERE viaje_id = ? AND invitado_usuario_id = ?")) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, viajeId);
            stmt.setInt(3, usuarioId);
            stmt.executeUpdate();
        }
    }

    private void eliminarInvitacionesQueYaNoAplican(Connection conn, int viajeId, int invitadorUsuarioId, Set<Integer> invitadosObjetivo) throws SQLException {
        if (invitadosObjetivo.isEmpty()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM viaje_invitaciones WHERE viaje_id = ? AND invitador_usuario_id = ?")) {
                stmt.setInt(1, viajeId);
                stmt.setInt(2, invitadorUsuarioId);
                stmt.executeUpdate();
            }
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < invitadosObjetivo.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM viaje_invitaciones WHERE viaje_id = ? AND invitador_usuario_id = ? AND invitado_usuario_id NOT IN (" + placeholders + ")")) {
            int idx = 1;
            stmt.setInt(idx++, viajeId);
            stmt.setInt(idx++, invitadorUsuarioId);
            for (Integer invitadoId : invitadosObjetivo) {
                stmt.setInt(idx++, invitadoId);
            }
            stmt.executeUpdate();
        }
    }

    private Set<Integer> resolverInvitados(String participantes, int invitadorUsuarioId) throws SQLException {
        Set<Integer> invitados = new HashSet<>();
        UsuarioDAO dao = new UsuarioDAO();
        for (String token : parseParticipantes(participantes)) {
            if (esAliasUsuarioActual(token)) continue;
            Usuario usuario = dao.obtenerPorTelefonoONombre(token);
            if (usuario != null && usuario.getId() != invitadorUsuarioId) {
                invitados.add(usuario.getId());
            }
        }
        return invitados;
    }

    private List<String> parseParticipantes(String participantes) {
        List<String> resultado = new ArrayList<>();
        if (participantes == null || participantes.isBlank()) return resultado;
        for (String token : participantes.split(",")) {
            String valor = token != null ? token.trim() : "";
            if (!valor.isBlank()) resultado.add(valor);
        }
        return resultado;
    }

    private boolean esAliasUsuarioActual(String token) {
        if (token == null) return false;
        String n = token.trim().toLowerCase();
        return n.equals("tú") || n.equals("tu");
    }
}
