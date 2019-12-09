package belote.joueur;

import java.awt.Component;
import java.awt.Graphics;

import belote.AnalyseurDeJeu;
import belote.JoueurBelote;
import belote.RegleBelote;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;
import java.lang.String;

public abstract class AbstractJoueur implements IJoueurBelote {

	protected RegleBelote regle;
    /** Ce que le joueur à dans sa main */
    protected PileDeCarte carteEnMain;
    /** Le tas du joueur pour la partie en cours */
    protected PileDeCarte tas;
    private String nom;
    protected AbstractJoueur precedent;
    protected AbstractJoueur suivant;
    /** La position du joueur autour du tapis */
    protected int ordre;
    /** Utilisé par la REgleBelote pour savoir qui a gagné la partie et la manche */
    public int pointsTotaux, nbPerdues, nbPrises, nbCapot, pointsTotaux2;

    /*@Override
    public IJoueurBelote clone() {
    	AbstractJoueur j = new AbstractJoueur(nom, ordre);
        j.carteEnMain = carteEnMain;
        j.tas = tas;
        j.precedent = precedent;
        j.suivant = suivant;
        j.pointsTotaux = pointsTotaux;
        j.nbPerdues = nbPerdues;
        j.nbCapot = nbCapot;
        j.nbPrises = nbPrises;
        return j;
    }*/ 

    public AbstractJoueur( String nom0, int ordre0 ) {
        nom = nom0;
        ordre = ordre0;
        carteEnMain = new PileDeCarte();
        tas = new PileDeCarte();
    }

    /** Dit au joueur où il se trouve autour de la table */
    @Override
	public void setOrdre( int pos) {
        ordre = pos;
    }

    /** Donne les joueurs précédent et suivant */
    @Override
	public void setEntreLesJoueurs( AbstractJoueur precedent0, AbstractJoueur suivant0 ) {
        if ( precedent0 != null )  precedent = precedent0;
        if ( suivant0 != null )  suivant = suivant0;
    }

    /** Définit la règle avec laquelle il va jouer */
    @Override
	public void setRegle( RegleBelote regle0) {
        regle = regle0;
    }

    /** Défini la PileDeCartes qu'il utilisera pour stocker son tas */
    @Override
	public void setTas( PileDeCarte tas0 ) {  
        tas = tas0;
    }
    
    /** Renvoie la position du joueur */
    @Override
	public int getOrdre() {
        return ordre;
    }

    /** Renvoie le joueur suivant */
    @Override
	public IJoueurBelote getSuivant() {
        return suivant;
    }

    /** Renvoie le joueur précédent */
    @Override
	public IJoueurBelote getPrecedent() {
        return precedent;
    }

    /** Renvoie le nom du joueur */
    @Override
	public String getNom() {
        return nom;
    }

    /** Donne le tas que le joueur a gagné (il ne l'a donc plus) */
    @Override
	public PileDeCarte rendTonTas() {
        PileDeCarte p = new PileDeCarte();
        while( tas.size() > 0) p.add( tas.remove(0));
        return p;
    }

    /** Donne les cartes qu'il a dans sa main (il ne les a donc plus) */
    @Override
	public PileDeCarte rendTaMain() {
        PileDeCarte p = carteEnMain;
        carteEnMain = new PileDeCarte();
        return p;
    }

    @Override
	public void ajoutATaMain(PileDeCarte cartes) {
        carteEnMain.addAll( cartes);
    }

    @Override
	public void ajoutATaMain(Carte carte) {
        carteEnMain.add( carte);
    }

    @Override
	public void ajoutATonTas( PileDeCarte cartes) {
        tas.addAll( cartes);
    }

    /** Affiche avec 'g' les cartes qu'il a dans la main à la position où elle sont */
    @Override
	public void dessineMain( Graphics g, int x, int y, int dx, int dy, boolean turned, Component comp) {

        for ( Carte c : carteEnMain) {
            c.getRepresentationGraphique().paint(g, x, y, turned );
            x += dx;
            y += dy;
        }
    }
    
    @Override
    public String toString() {
        return "JoueurBelote["+nom+"]";

    }

    /** Renvoie le contenu de sa main (mais le garde) */
    @Override
	public PileDeCarte getMain() {
        return carteEnMain;
    }

    /** Affiche avec 'g' les plis qu'il a fait en position (x,y)
     *  avec un interval (dx,dy) entre chaque cartes */
    @Override
	public void dessinePlis(Graphics g, int x, int y, int dx, int dy, Component comp) {
        for ( Carte c : tas) {
            c.getRepresentationGraphique().paint(g, x, y, false);
            x += dx;
            y += dy;
        }
    }


    /** Affiche la pile avec un espace toutes les 4 cartes */
    @Override
	public void dessinePlis2(Graphics g, int x, int y, int dx, int dy, Component comp) {
        int n = 0;
        for ( Carte c : tas) {
            c.getRepresentationGraphique().paint(g, x, y, false);
            x += dx;
            y += dy;
            n++;
            if ( (n % 4) == 0) { x += dx; y += dy; }
        }

    }


    @Override
	public void setNom(String nom) {
        this.nom = nom;
    }


    @Override
	public int getNbPlis() {
        return (int) ((tas.size() + suivant.suivant.tas.size())/4);
    }

	
	
}
