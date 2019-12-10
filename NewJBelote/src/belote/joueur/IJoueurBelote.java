package belote.joueur;

import java.awt.Component;
import java.awt.Graphics;

import belote.GestionnaireCartesLecture;
import belote.RegleTemp;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;

public interface IJoueurBelote {

	/** Dit au joueur où il se trouve autour de la table */
	void setOrdre(int pos);

	/** Donne les joueurs précédent et suivant */
	void setEntreLesJoueurs(IJoueurBelote precedent0, IJoueurBelote suivant0);

	/** Définit la règle avec laquelle il va jouer */
	void setRegle(RegleTemp regle0);

	/** Défini la PileDeCartes qu'il utilisera pour stocker son tas */
	void setTas(PileDeCarte tas0);

	/** Renvoie la position du joueur */
	int getOrdre();
	
	public GestionnaireCartesLecture getGestionnaireCarte();
	
	public void setGestionnaireCarte(GestionnaireCartesLecture gestionnaireCarte);

	/** Renvoie le joueur suivant */
	IJoueurBelote getSuivant();

	/** Renvoie le joueur précédent */
	IJoueurBelote getPrecedent();

	/** Renvoie le nom du joueur */
	String getNom();

	/** Donne le tas que le joueur a gagné (il ne l'a donc plus) */
	PileDeCarte rendTonTas();

	/** Donne les cartes qu'il a dans sa main (il ne les a donc plus) */
	PileDeCarte rendTaMain();

	void ajoutATaMain(PileDeCarte cartes);

	void ajoutATaMain(Carte carte);

	void ajoutATonTas(PileDeCarte cartes);

	/** Affiche avec 'g' les cartes qu'il a dans la main à la position où elle sont */
	void dessineMain(Graphics g, int x, int y, int dx, int dy, boolean turned, Component comp);

	String toString();

	/** Renvoie le contenu de sa main (mais le garde) */
	PileDeCarte getMain();

	/** Affiche avec 'g' les plis qu'il a fait en position (x,y)
	 *  avec un interval (dx,dy) entre chaque cartes */
	void dessinePlis(Graphics g, int x, int y, int dx, int dy, Component comp);

	/** Affiche la pile avec un espace toutes les 4 cartes */
	void dessinePlis2(Graphics g, int x, int y, int dx, int dy, Component comp);

	void setNom(String nom);

	int getNbPlis();

	int cut();
	
	public boolean getChoixAtout1(Carte atout0);
	public CouleurCarte getChoixAtout2(CouleurCarte sauf);
	public Carte joueUneCarte();
	public void setNAPlusDe(CouleurCarte couleur);
	public void nouvellePartie();

	int getNbreDePoints();

	int getPointsTotaux2();

	int getNbCapot();

	int getNbPrises();

	int getNbPerdues();

	int getPointsTotaux();

	void sortCartesEnMain();

	void nbCapotPlusUn();

	void nbPrisePlusUn();

	void nbPerdusPlusUn();

	void addPointsTotaux(int nb);

	void trieTaMain();

	void ptTotauxVersTotaux2();

	public IJoueurBelote createClone();

	int getSizeTas();

	void setJoueurQuiCommence(IJoueurBelote joueurQuiCommence);

	void setQuiAPris(IJoueurBelote quiAPris);

	void setDernierCarteJoue(Carte dernierCarteJoue);
}