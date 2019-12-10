package belote.joueur;

import java.awt.Component;
import java.awt.Graphics;

import belote.GestionnaireCartesLecture;
import belote.RegleTemp;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import java.lang.String;

import java.util.Set;

public abstract class AbstractJoueur implements IJoueurBelote {

	protected GestionnaireCartesLecture gestionnaireCarte;
	protected RegleTemp regle;
	/** Ce que le joueur à dans sa main */
	protected PileDeCarte carteEnMain;
	/** Le tas du joueur pour la partie en cours */
	protected PileDeCarte tas;
	protected String nom;
	protected AbstractJoueur precedent;
	protected AbstractJoueur suivant;
	/** La position du joueur autour du tapis */
	protected int ordre;
	/** Utilisé par la REgleBelote pour savoir qui a gagné la partie et la manche */
	public int pointsTotaux, nbPerdues, nbPrises, nbCapot, pointsTotaux2;
	protected AbstractJoueur quiAPris;
	protected Carte dernierCarteJoue;
	protected IJoueurBelote joueurQuiCommence;
	private Set<CouleurCarte> naPlusDe, aCouleur;

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

	public AbstractJoueur(String nom0, int ordre0) {
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
	public void setEntreLesJoueurs( IJoueurBelote precedent0, IJoueurBelote suivant0 ) {
		if ( precedent0 != null )  precedent = (AbstractJoueur) precedent0;
		if ( suivant0 != null )  suivant = (AbstractJoueur) suivant0;
	}

	/** Définit la règle avec laquelle il va jouer */
	@Override
	public void setRegle( RegleTemp regle0) {
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

	@Override
	public GestionnaireCartesLecture getGestionnaireCarte() {
		return gestionnaireCarte;
	}

	@Override
	public void setGestionnaireCarte(GestionnaireCartesLecture gestionnaireCarte) {
		this.gestionnaireCarte = gestionnaireCarte;
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

	/** Renvoie un nombre entre 3 et 29 pour couper le jeu. */
	@Override
	public int cut() {
		return 3 + (int)(Math.random()*29);
	}

	@Override
	public void setNAPlusDe(CouleurCarte couleur) {
		naPlusDe.add(couleur);
		aCouleur.remove(couleur);
	}

	protected boolean mesAdversairesOntEncoreDu(CouleurCarte couleur) {
		return ! (suivant.naPlusDe.contains(couleur) || precedent.naPlusDe.contains(couleur));
	}

	protected boolean ceJoueurNaPlusDe(AbstractJoueur j, CouleurCarte couleur) {
		return j.naPlusDe.contains(couleur);
	}

	@Override
	public void nouvellePartie(){
		for(CouleurCarte c : CouleurCarte.COULEURS) {
			aCouleur.add(c);
		}
		naPlusDe.clear();
	}

	/** Renvoie vrai si il reste des cartes à la couleur qui ne sont pas dans 
	 *  la main du joueur */
	protected boolean resteCartesNonTombeesAPour(CouleurCarte c) {
		return ! (( gestionnaireCarte.nombreDeCartesJoueesA(c)+carteEnMain.nombreDe(c)) == 8);
	}

	@Override
	public int getNbreDePoints() {
		return regle.compteLesPointsDe(tas);
	}
	@Override
	public int getPointsTotaux() {
		return pointsTotaux;
	}
	@Override
	public int getNbPerdues() {
		return nbPerdues;
	}
	@Override
	public int getNbPrises() {
		return nbPrises;
	}
	@Override
	public int getNbCapot() {
		return nbCapot;
	}
	@Override
	public int getPointsTotaux2() {
		return pointsTotaux2;
	}
	@Override
	public void sortCartesEnMain() {
		carteEnMain.sort(gestionnaireCarte);
	}
	@Override
	public void nbPrisePlusUn() {
		nbPrises++;
	}
	@Override
	public void nbPerdusPlusUn() {
		nbPrises++;
	}
	@Override
	public void nbCapotPlusUn() {
		nbPrises++;
	}
	@Override
	public void addPointsTotaux(int nb) {
		pointsTotaux+=nb;
	}
	@Override
	public void ptTotauxVersTotaux2() {
		pointsTotaux2+=pointsTotaux;
		pointsTotaux=0;
	}
	@Override
	public void trieTaMain() {
		carteEnMain.sort(gestionnaireCarte);
	}

	protected AbstractJoueur initClone(AbstractJoueur newJoueur) {
		newJoueur.regle = regle;
		newJoueur.carteEnMain = carteEnMain;
		newJoueur.tas = tas;
		newJoueur.precedent = precedent;
		newJoueur.suivant = suivant;
		newJoueur.pointsTotaux = pointsTotaux;
		newJoueur.nbPerdues = nbPerdues;
		newJoueur.nbCapot = nbCapot;
		newJoueur.nbPrises = nbPrises;
		return newJoueur;
	}
	@Override
	public int getSizeTas() {
		return tas.size();
	}
}
