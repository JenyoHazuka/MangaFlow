package com.example.mangaflow.models;

import java.util.List;

public class MangaClass {
    private String titre_serie;
    private Integer numero_tome;
    private String image_url;
    private String edition;
    private String isbn;
    private String editeur;
    private String date_parution;
    private Float prix;
    private Integer nb_pages;
    private List<String> auteurs;
    private List<String> genres_theme;
    private String resume;
    private Boolean lu;
    private Boolean suivi;
    private Boolean possede;

    private String titre;

    // Constructeur
    public MangaClass(String titre_serie, Integer numero_tome, String image_url, String edition, String isbn,
                      String editeur, String date_parution, Float prix, Integer nb_pages, List<String> auteurs,
                      List<String> genres_theme, String resume, Boolean lu, Boolean suivi, Boolean possede) {
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
        this.lu = lu;
        this.suivi = suivi;
        this.possede = possede;
    }

    // Ajoute ce constructeur vide
    public MangaClass() {
        // Ce constructeur ne fait rien, mais il permet de créer l'objet
        // avant d'utiliser les "setters" comme setTitre
    }
    public void setTitre(String titre) {
        this.titre = titre; // Remplace 'this.titre' par le nom de ta variable interne
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

    public String getIsbn() {
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
    public Boolean getLu() { return lu; }

    public Boolean getSuivi() { return suivi; }

    public Boolean getPossede() { return possede; }
}
