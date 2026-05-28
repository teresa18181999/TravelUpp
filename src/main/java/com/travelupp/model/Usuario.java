package com.travelupp.model;

public class Usuario {

    private int id;
    private String telefono;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private String codigoVerificacion;
    private boolean verificado;
    private boolean esAdmin;

    public Usuario() {
    }

    public Usuario(int id, String telefono, String nombre, String email, String fotoPerfil, String codigoVerificacion, boolean verificado, boolean esAdmin) {
        this.id = id;
        this.telefono = telefono;
        this.nombre = nombre;
        this.email = email;
        this.fotoPerfil = fotoPerfil;
        this.codigoVerificacion = codigoVerificacion;
        this.verificado = verificado;
        this.esAdmin = esAdmin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }
}
