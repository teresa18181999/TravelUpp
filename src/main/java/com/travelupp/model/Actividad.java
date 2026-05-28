package com.travelupp.model;

public class Actividad {

    private int id;
    private int viajeId;
    private int dia;
    private String tipo;
    private String hora;
    private String horaFin;
    private String nombre;
    private String descripcion;
    private String imagen;

    public Actividad() {
    }

    public Actividad(int id, int viajeId, int dia, String tipo, String hora, String horaFin, String nombre, String descripcion, String imagen) {
        this.id = id;
        this.viajeId = viajeId;
        this.dia = dia;
        this.tipo = tipo;
        this.hora = hora;
        this.horaFin = horaFin;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getViajeId() { return viajeId; }
    public void setViajeId(int viajeId) { this.viajeId = viajeId; }

    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
