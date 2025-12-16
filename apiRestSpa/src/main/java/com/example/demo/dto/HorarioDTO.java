package com.example.demo.dto;

public class HorarioDTO {

    private String hora;
    private boolean disponible;

    public HorarioDTO(String hora, boolean disponible) {
        this.hora = hora;
        this.disponible = disponible;
    }

    public String getHora() {
        return hora;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
