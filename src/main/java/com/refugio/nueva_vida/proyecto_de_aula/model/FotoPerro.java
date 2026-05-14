package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;

@Entity
@Table(name = "foto_perro")
public class FotoPerro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Integer idFoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perro", nullable = false)
    private Perro perro;

    @Column(name = "url_foto", nullable = false, length = 500)
    private String urlFoto;

    @Column(name = "es_perfil", nullable = false)
    private Boolean esPerfil = false;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    public FotoPerro() {}

    public FotoPerro(Perro perro, String urlFoto, Boolean esPerfil, Integer orden) {
        this.perro = perro;
        this.urlFoto = urlFoto;
        this.esPerfil = esPerfil;
        this.orden = orden;
    }

    public Integer getIdFoto() { return idFoto; }
    public void setIdFoto(Integer idFoto) { this.idFoto = idFoto; }
    public Perro getPerro() { return perro; }
    public void setPerro(Perro perro) { this.perro = perro; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
    public Boolean getEsPerfil() { return esPerfil; }
    public void setEsPerfil(Boolean esPerfil) { this.esPerfil = esPerfil; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
