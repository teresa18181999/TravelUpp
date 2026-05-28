package com.travelupp.model;

import java.util.Map;

public class Viaje {

    private int id;
    private String nombre;
    private String destino;
    private String fechaInicio;
    private String fechaFin;
    private String participantes;
    private Map<String, String> participantesNombres;
    private String estado;
    private String coverImage;
    private Integer usuarioId;
    private boolean invitado;
    private String creadorNombre;
    private String creadorTelefono;

    public Viaje() {
    }

    public Viaje(int id, String nombre, String destino, String fechaInicio,
                 String fechaFin, String participantes, String estado, String coverImage, Integer usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.destino = destino;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.participantes = participantes;
        this.estado = estado;
        this.coverImage = coverImage;
        this.usuarioId = usuarioId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getParticipantes() { return participantes; }
    public void setParticipantes(String participantes) { this.participantes = participantes; }

    public Map<String, String> getParticipantesNombres() { return participantesNombres; }
    public void setParticipantesNombres(Map<String, String> participantesNombres) { this.participantesNombres = participantesNombres; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public boolean isInvitado() { return invitado; }
    public void setInvitado(boolean invitado) { this.invitado = invitado; }

    public String getCreadorNombre() { return creadorNombre; }
    public void setCreadorNombre(String creadorNombre) { this.creadorNombre = creadorNombre; }

    public String getCreadorTelefono() { return creadorTelefono; }
    public void setCreadorTelefono(String creadorTelefono) { this.creadorTelefono = creadorTelefono; }
}
