package com.example.mangaflow.utils;

import java.util.List;

public class MangaClass {
    private String titre_serie;
    private Integer numero_tome;
    private String image_url;
    private String edition;
    private Integer isbn;
    private String editeur;
    private String date_parution;
    private Float prix;
    private Integer nb_pages;
    private List<String> auteurs;
    private List<String> genres_theme;
    private String resume;

    // Constructeur
    public MangaClass(String titre_serie, Integer numero_tome, String image_url, String edition, Integer isbn,
                      String editeur, String date_parution, Float prix, Integer nb_pages, List<String> auteurs,
                      List<String> genres_theme, String resume) {
        this.titre_serie = titre_serie;
        this.numero_tome = numero_tome;
        this.image_url = image_url;
        this.edition = edition;
        this.isbn = isbn;
        this.editeur = editeur;
        this.date_parution = date_parution;
        this.prix = prix;
        this.nb_pages = nb_pages;
        this.auteurs = auteurs;
        this.genres_theme = genres_theme;
        this.resume = resume;
    }

    public String getTitre_serie() {
        return titre_serie;
    }

    public Integer getNumero_tome() {
        return numero_tome;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getEdition() {
        return edition;
    }

    public Integer getIsbn() {
        return isbn;
    }

    public String getEditeur() {
        return editeur;
    }

    public String getDate_parution() {
        return date_parution;
    }

    public Float getPrix() {
        return prix;
    }

    public Integer getNb_pages() {
        return nb_pages;
    }

    public List<String> getAuteurs() {
        return auteurs;
    }

    public List<String> getGenres_theme() {
        return genres_theme;
    }

    public String getResume() {
        return resume;
    }
}
