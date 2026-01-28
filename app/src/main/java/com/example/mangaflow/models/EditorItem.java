package com.example.mangaflow.models;

/**
 * Modèle de données polyvalent pour l'affichage des listes d'éditeurs.
 * Ce modèle utilise un système de types pour permettre au RecyclerView
 * d'afficher différents types de lignes (En-têtes vs Items).
 */
public class EditorItem {

    // Constantes permettant d'identifier la nature de l'item
    // TYPE_HEADER : Pour les titres de catégories (ex: "Mangas", "Artbooks")
    public static final int TYPE_HEADER = 0;
    // TYPE_MANGA : Pour les noms des séries individuelles
    public static final int TYPE_MANGA = 1;

    // Le type de l'item actuel (0 ou 1)
    public int type;

    // Le texte à afficher (soit le nom de la catégorie, soit le titre du manga)
    public String title;

    /**
     * Constructeur pour créer un nouvel item de liste.
     * @param type Le type d'affichage (TYPE_HEADER ou TYPE_MANGA)
     * @param title Le texte à afficher dans la ligne
     */
    public EditorItem(int type, String title) {
        this.type = type;
        this.title = title;
    }
}