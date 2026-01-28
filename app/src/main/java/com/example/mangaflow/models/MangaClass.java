package com.example.mangaflow.models;

import java.util.List;

/**
 * Modèle de données représentant un tome de manga individuel.
 * Cette classe centralise toutes les informations techniques, éditoriales
 * et l'état de lecture/possession pour l'utilisateur.
 */
public class MangaClass {
    // --- ATTRIBUTS ÉDITORIAUX ---
    private String titre_serie;    // Nom de la série (ex: "One Piece")
    private Integer numero_tome;   // Numéro du volume
    private String image_url;      // Lien vers l'image de couverture
    private String edition;        // Type d'édition (Standard, Deluxe, Collector...)
    private String isbn;           // Code EAN-13 unique du livre
    private String editeur;        // Maison d'édition (française)
    private String date_parution;  // Date de sortie
    private Float prix;            // Prix de vente
    private Integer nb_pages;      // Nombre de pages
    private List<String> auteurs;  // Liste des auteurs (scénariste, dessinateur)
    private List<String> genres_theme; // Tags (Action, Shonen, etc.)
    private String resume;         // Synopsis du tome

    // --- ÉTATS UTILISATEUR ---
    private Boolean lu;            // Marqueur de lecture
    private Boolean suivi;         // Marqueur de mise en favoris / liste de souhaits
    private Boolean possede;       // Marqueur de présence dans la collection physique

    private String titre;          // Champ additionnel pour l'affichage dynamique

    /**
     * Constructeur complet pour initialiser toutes les propriétés d'un manga.
     * Utilisé principalement lors du chargement initial depuis le catalogue global.
     */
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

    /**
     * Constructeur vide.
     * Indispensable pour certaines bibliothèques de parsing ou pour une initialisation pas à pas.
     */
    public MangaClass() {
        // Ce constructeur ne fait rien, mais il permet de créer l'objet
        // avant d'utiliser les "setters" comme setTitre
    }

    /**
     * Définit le titre d'affichage.
     * @param titre Le texte à stocker dans la variable interne
     */
    public void setTitre(String titre) {
        this.titre = titre; // Remplace 'this.titre' par le nom de ta variable interne
    }

    // --- GETTERS ---
    // Ces méthodes permettent aux Adapters d'accéder aux données privées sans les modifier.

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

    public Boolean getLu() {
        return lu;
    }

    public Boolean getSuivi() {
        return suivi;
    }

    public Boolean getPossede() {
        return possede;
    }
}