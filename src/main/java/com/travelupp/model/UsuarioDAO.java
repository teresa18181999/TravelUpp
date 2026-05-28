package com.travelupp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsuarioDAO {

    private static final String CODIGO_FIJO = "123456";

    public boolean existeTelefono(String telefono) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM usuarios WHERE telefono = ?")) {
            stmt.setString(1, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Usuario obtenerPorTelefono(String telefono) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, telefono, nombre, email, foto_perfil, codigo_verificacion, verificado, es_admin FROM usuarios WHERE telefono = ?")) {
            stmt.setString(1, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                    u.setFotoPerfil(rs.getString("foto_perfil"));
                    u.setCodigoVerificacion(rs.getString("codigo_verificacion"));
                    u.setVerificado(rs.getBoolean("verificado"));
                    u.setEsAdmin(rs.getBoolean("es_admin"));
                    return u;
                }
            }
        }
        return null;
    }

    public Usuario obtenerPorTelefonoONombre(String token) throws SQLException {
        if (token == null || token.isBlank()) return null;
        String telefono = token.trim().replaceAll("\\D", "");
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, telefono, nombre, email, foto_perfil, codigo_verificacion, verificado, es_admin " +
                     "FROM usuarios WHERE telefono = ? OR LOWER(nombre) = LOWER(?) LIMIT 1")) {
            stmt.setString(1, telefono);
            stmt.setString(2, token.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                    u.setFotoPerfil(rs.getString("foto_perfil"));
                    u.setCodigoVerificacion(rs.getString("codigo_verificacion"));
                    u.setVerificado(rs.getBoolean("verificado"));
                    u.setEsAdmin(rs.getBoolean("es_admin"));
                    return u;
                }
            }
        }
        return null;
    }

    public Usuario obtenerPorId(int id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, telefono, nombre, email, foto_perfil, codigo_verificacion, verificado, es_admin FROM usuarios WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                    u.setFotoPerfil(rs.getString("foto_perfil"));
                    u.setCodigoVerificacion(rs.getString("codigo_verificacion"));
                    u.setVerificado(rs.getBoolean("verificado"));
                    u.setEsAdmin(rs.getBoolean("es_admin"));
                    return u;
                }
            }
        }
        return null;
    }

    public int crearUsuario(Usuario usuario) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO usuarios (telefono, nombre, email, foto_perfil, codigo_verificacion, verificado) VALUES (?, ?, ?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getTelefono());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getEmail() != null ? usuario.getEmail() : "");
            stmt.setString(4, usuario.getFotoPerfil());
            stmt.setString(5, CODIGO_FIJO);
            stmt.setBoolean(6, false);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean actualizarCodigo(String telefono) throws SQLException {
        String codigo = "618941168".equals(telefono) ? "442255" : CODIGO_FIJO;
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("UPDATE usuarios SET codigo_verificacion = ? WHERE telefono = ?")) {
            stmt.setString(1, codigo);
            stmt.setString(2, telefono);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean verificarCodigo(String telefono, String codigo) throws SQLException {
        String sql = "UPDATE usuarios SET verificado = TRUE WHERE telefono = ? AND codigo_verificacion = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, telefono);
            stmt.setString(2, codigo);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean estaVerificado(String telefono) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT verificado FROM usuarios WHERE telefono = ?")) {
            stmt.setString(1, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("verificado");
            }
        }
    }

    public boolean actualizarPerfil(int usuarioId, String nombre, String email, String fotoPerfil) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("UPDATE usuarios SET nombre = ?, email = ?, foto_perfil = ? WHERE id = ?")) {
            stmt.setString(1, nombre);
            stmt.setString(2, email != null ? email : "");
            stmt.setString(3, fotoPerfil);
            stmt.setInt(4, usuarioId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminarUsuario(int id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, telefono, nombre, email, verificado, es_admin FROM usuarios ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setTelefono(rs.getString("telefono"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                u.setVerificado(rs.getBoolean("verificado"));
                u.setEsAdmin(rs.getBoolean("es_admin"));
                usuarios.add(u);
            }
        }
        return usuarios;
    }

    public Map<String, String> obtenerNombresPorTelefonos(Collection<String> telefonos) throws SQLException {
        Map<String, String> resultado = new HashMap<>();
        if (telefonos == null || telefonos.isEmpty()) return resultado;
        String limpios = telefonos.stream().map(t -> t.replaceAll("\\D", "")).filter(t -> !t.isBlank()).collect(Collectors.joining("','"));
        if (limpios.isBlank()) return resultado;
        String sql = "SELECT telefono, nombre FROM usuarios WHERE telefono IN ('" + limpios + "')";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resultado.put(rs.getString("telefono"), rs.getString("nombre"));
            }
        }
        return resultado;
    }

    public static String obtenerCodigoFijo() {
        return CODIGO_FIJO;
    }
}
