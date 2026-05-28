package com.travelupp.model;

public class AlbumFoto {
    private int id;
    private int viajeId;
    private int usuarioId;
    private String imagen;

    public AlbumFoto() {}

    public AlbumFoto(int id, int viajeId, int usuarioId, String imagen) {
        this.id = id;
        this.viajeId = viajeId;
        this.usuarioId = usuarioId;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getViajeId() { return viajeId; }
    public void setViajeId(int viajeId) { this.viajeId = viajeId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
