package edu.pmdm.turbogranny;

public class Puntuacion {
    private String nickname;
    private long puntos;

    public Puntuacion(String nickname, long puntos) {
        this.nickname = nickname;
        this.puntos = puntos;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getPuntos() {
        return puntos;
    }

    public void setPuntos(long puntos) {
        this.puntos = puntos;
    }
}
