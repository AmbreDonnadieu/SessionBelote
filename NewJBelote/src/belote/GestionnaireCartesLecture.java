package belote;

import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;

public class GestionnaireCartesLecture {
	RegleTemp regle;

	/* Les cartes jouées dans la partie */
	PileDeCarte carteJouees, cartesNonJouees, copieJeuDeCarte;

	/* Nombre de cartes jouées par couleur */
	int[] nombreJouees;

	public GestionnaireCartesLecture( RegleTemp regle0, PileDeCarte jeuCarte) {
		regle = regle0;

		copieJeuDeCarte = (PileDeCarte)jeuCarte.clone();
		cartesNonJouees = (PileDeCarte)jeuCarte.clone();

		nombreJouees = new int[4];
		carteJouees = new PileDeCarte();
	}

	public int combienIlResteDeCartesNonJoueesA(CouleurCarte couleur) {
		return 8 - carteJouees.nombreDe( couleur);
	}

	/** Renvoie vraie si toutes les cartes > sont déjà tombées */
	public boolean estMaitrePourPlusTard(Carte carte) {

		if ( carte == null) return false;

		for ( Carte c : cartesNonJouees)
			if ( c.estCouleur(carte))
				if ( positionDe(c)>positionDe(carte))
					return false;


		return true;
	}

	/** Renvoie vraie si toutes les cartes > sont déjà tombées en tenant compte
	 *  des cartes déjà jouées du tapis */
	public boolean estMaitreTapis(Carte carte, PileDeCarte tapis) {

		if ( carte == null) return false;

		if ( ! estMeilleurQue(carte, tapis)) return false;

		if ( tapis.size() == 3) return true; // c'est le dernier à jouer

		int pos = positionDe(carte);
		int nb_cartesPlusFortes = 8 - pos;

		for ( Carte c : carteJouees)
			if ( c.estCouleur(carte))
				if ( positionDe(c)>pos)
					nb_cartesPlusFortes--;

		return nb_cartesPlusFortes == 0;
	}

	/** Renvoie la meilleur carte du plis en tenant compte de l'atout */
	public Carte meilleurCarte(PileDeCarte pile) {
		Carte meilleur = null;

		for ( Carte c : pile) {
			if (meilleur != null) {
				if (meilleur.getCouleur().equals(c.getCouleur())) {
					if (positionDe(c)>positionDe(meilleur)) meilleur = c;
				} else
					if ( regle.isAtout(c.getCouleur()) )
						meilleur = c;
					else if ( !regle.isAtout(meilleur.getCouleur()))
						if ( c.getCouleur().equals(regle.couleurDemandee(pile)))
							meilleur = c;

			} else meilleur = c;
		}
		return meilleur;
	}

	/** Renvoie vrai si il reste des cartes à la couleur qui ne sont pas dans 
	 *  la main du joueur */
	boolean resteCartesNonTombeesAPour(CouleurCarte c, JoueurBelote j ) {
		return ! (( nombreJouees[c.toInt()]+j.main.nombreDe(c)) == 8);
	}

	/** Renvoie le nombre de cartes déjà jouées à la couleur */
	int nombreDeCartesJoueesA( CouleurCarte c) {
		return nombreJouees[ c.toInt()];
	}

	void nouvellePartie() {
		for ( int i = 0; i < 4; i++) {
			nombreJouees[i] = 0;
		}

		carteJouees.clear();
		cartesNonJouees = (PileDeCarte)copieJeuDeCarte.clone();
	}

	/** Renvoie la position (mini 1) de la carte dans la couleur
	 *  en tenant compte de celle d'atout */
	public int positionDe(Carte c) {

		if ( c == null) return -1;

		ValeureCarte[] set = regle.isAtout(c.getCouleur()) ? ValeureCarte.SET_BELOTE_ATOUT : ValeureCarte.SET_BELOTE;
		int i = 1;
		while ( ! set[i-1].equals(c.getValeur())) i++;

		return i;
	}

	Carte meilleurCarteDansA(PileDeCarte pile, CouleurCarte couleur) {
		Carte meilleur = null;

		for ( Carte c : pile)
			if ( c.estCouleur(couleur))
				if ( (meilleur==null) || (positionDe(c)>positionDe(meilleur)))
					meilleur = c;

		return meilleur;
	}

	/**
	 * Retourne vrai si la carte est la meilleur de la pile, en tenant compte
	 * de la couleur d'atout.
	 */
	boolean estMeilleurQue(Carte carte, PileDeCarte pile) {

		Carte m = meilleurCarte(pile);
		return meilleurCarteEntre(carte, m, regle.couleurDemandee(pile)) == carte;
	}

	Carte meilleurCarteEntre( Carte c1, Carte c2, CouleurCarte couleurDemandee) {
		if ( c1 == null) return c2;
		if ( c2 == null) return c1;

		if ( c1.getCouleur()==c2.getCouleur())
			if ( positionDe(c1)>= positionDe(c2)) return c1;
			else return c2;
		else
			if ( c1.getCouleur().equals(regle.getCouleurAtout()) ||
					(!c2.getCouleur().equals(regle.getCouleurAtout()) &&
							c1.getCouleur().equals(couleurDemandee))) return c1;
			else return c2;
	}

	int nombreRestantePlusForteQue(Carte c2) {
		int n = 0;
		for ( Carte c : cartesNonJouees) {
			if ( c.getCouleur().equals(c2.getCouleur()))
				if (positionDe(c)>positionDe(c2))
					n++;
		}
		return n;
	}
}
