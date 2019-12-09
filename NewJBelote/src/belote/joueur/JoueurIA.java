package belote.joueur;

import java.awt.Component;
import java.awt.Graphics;

import belote.GestionnaireCartesLecture;
import belote.JoueurBelote;
import belote.RegleBelote;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;

public class JoueurIA extends AbstractJoueur {

	public JoueurIA(String nom0, int ordre0) {
		super(nom0, ordre0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JoueurIA clone() {
		JoueurIA j = new JoueurIA(nom, ordre);
		j.regle = regle;
		j.carteEnMain = carteEnMain;
		j.tas = tas;
		j.precedent = precedent;
		j.suivant = suivant;
		j.pointsTotaux = pointsTotaux;
		j.nbPerdues = nbPerdues;
		j.nbCapot = nbCapot;
		j.nbPrises = nbPrises;
		return j;
	}

	/** Renvoie le fait qu'il veuille prendre cet atout au premier tour */
	public boolean getChoixAtout1( Carte atout ) {
		return getChoixAtout( atout.getCouleur(), 1);
	}

	/** Renvoie le fait qu'il veuille prendre la 'couleur' cet atout au 'tour' */
	boolean getChoixAtout( CouleurCarte couleur, int tour) {
		int force = 0;
		int nbAtout = 0, nbAs = 0;
		boolean aValet = false, aDame = false, aRoi = false, aNeuf = false;
		boolean aCouleur[] = { false, false, false, false };
		Carte carte;

		carteEnMain.add(carte = gestionnaireCarte.getTapis().get(0));

		for ( Carte c : carteEnMain) {
			aCouleur[c.getCouleur().toInt()] = true;

			if ( c.getCouleur().equals(couleur) ) {
				nbAtout++;
				switch ( c.getValeur().toInt())  {
				case ValeureCarte.NCARD_V: aValet = true; force += 4; break;
				case ValeureCarte.NCARD_9: aNeuf = true; force += 2; break;
				case ValeureCarte.NCARD_R: aRoi = true; break;
				case ValeureCarte.NCARD_D: aDame = true; break;
				case ValeureCarte.NCARD_AS: force += 1; break;
				}
			} else {
				if ( (c.getValeur() == ValeureCarte.CARD_10) &&
						(carteEnMain.nombreDe(c.getCouleur())>1) &&
						((!carteEnMain.contient(c.getCouleur(), ValeureCarte.CARD_AS)) ||
								(carteEnMain.nombreDe(couleur)>=3) ))
					force += 1;

				else if ( c.getValeur() == ValeureCarte.CARD_AS ) force += 2;
			}
		}

		if ( (RegleBelote.JOUEUR_EST == ordre) || (RegleBelote.JOUEUR_OUEST == ordre))
			if ( (regle.getJoueurQuiCommence() == this) && (tour==1) )
				force += 1;

		if ( aRoi && aDame)
			force += 1;

		if ( nbAtout > 2) force += nbAtout-2;
		if ( nbAtout < 3) force -= 1;

		// Ajoute 1 si il y a une coupe franche
		if ( nbAtout >= 4) for ( int i = 0; i < 4; i++)
			if ( ! aCouleur[i] && (i!=couleur.toInt())) {
				force++;
				break;
			}

		// System.out.println( "" + toString() + "a " + atout + " score = " + force);

		carteEnMain.remove(carte);
		return force >= 7;
	}

	/** Renvoie la couleur qu'il choisi pour le deuxième tour d'atout (ou null) */
	public CouleurCarte getChoixAtout2( CouleurCarte sauf) {
		for ( CouleurCarte col : CouleurCarte.COULEURS)
			if ((col!= sauf) && getChoixAtout(col, 2))
				return col;

		return null;
	}

	/** Affiche avec 'g' les cartes qu'il a dans la carteEnMain à la position où elle sont */
	public void dessinecarteEnMain( Graphics g, int x, int y, int dx, int dy, boolean turned, Component comp) {

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

	/** Renvoie le contenu de sa carteEnMain (mais le garde) */
	public PileDeCarte getcarteEnMain() {
		return carteEnMain;
	}


	/** Renvoie la carte qu'il doit obligatoirement jouer carteEnMaintenant */
	public Carte joueUneCarte() {

		Carte carte = null;
		CouleurCarte couleurDemandee = gestionnaireCarte.getCouleurDemandee();
		CouleurCarte couleurAtout = regle.getCouleurAtout();

		if ( combienOntJoues() == 0 ) { // Premier à jouer ?
			if ( onAPris() ) {
				if ( ilOntDesAtouts() && jAiDeLaCouleur(couleurAtout) ) {

					carte = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain, couleurAtout);
					if ( ! gestionnaireCarte.estMaitrePourPlusTard(carte))

						// Cherche à ne pas se faire manger un gros atout, mais à faire tomber les autres
						if ((regle.getQuiAPris()==this) &&
								(gestionnaireCarte.nombreDeCartesNonJoueesPlusForteQue(carte)>0) &&
								(regle.pointsDe(carte)>=10))
							if ( donneUneCarteMaitre() != null)
								carte = donneCarteJusteDessous(carte, true);
							else
								carte = donneUnePetiteCarteA(couleurAtout);

				} else {
					carte = donneUneQuiPrendUneGrosseDAtout();
					if (carte == null) carte = donneUneCarteMaitre();
					if (carte == null) carte = donneUneCarteQuiFaitMaitre();
				}

			} else {
				carte = donneUneCarteMaitre();
				if (carte == null) carte = donneUneCarteQuiFaitMaitre();
			}
		} else { // ! premier à jouer
			if ( couleurDemandee != couleurAtout) {
				if ( jAiDeLaCouleur( couleurDemandee)) {
					if ( jAiUneCarteMaitreA( couleurDemandee))
						carte = donneUneCarteMaitreA( couleurDemandee);
					else
						if ( gestionnaireCarte.estMaitreTapis(regle.getCarteJoueePar(suivant.suivant)))
							carte = donneUneGrosseCarteA(couleurDemandee);
						else
							carte = donneUnePetiteCarteA( couleurDemandee);
				} else { // ! premier, sans la couleur demandÃ©e qui n'est pas de l'atout
					if ( onEstMaitre() ) {

						if ( ((combienOntJoues()==2) && ! ceJoueurCoupeA(suivant, couleurDemandee) &&
								gestionnaireCarte.estMaitreTapis(regle.getCarteJoueePar(precedent.precedent))) ||
								(combienOntJoues()==3))
							carte = donneLaGrosseDefausse();
						else
							if ( onAPris()) carte = donneUneQuiFaitUneCoupe();
							else
								if ( monPartenaireSeraMaitre() && ! ceJoueurCoupeA(suivant, couleurDemandee) &&
										(gestionnaireCarte.nombreDeCartesJoueesA(couleurDemandee)==regle.tapis.nombreDe(couleurDemandee)))
									carte = donneLaGrosseDefausse();
								else
									carte = donneUneQuiFaitUneCoupe(); // Joue la petite dÃ©fausse
					} else
						if ( jAiDeLaCouleur(couleurAtout))
							if ( regle.getQuiAPris()==this)
								carte = donneMeilleurAtoutNonMaitre(); // il faut couper petit si pas sur
								else
									if ( monPartenaireSeraMaitre() && ! ceJoueurCoupeA(suivant, couleurDemandee))
										carte = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain, couleurAtout);
									else
										carte = donneMiniAtout();

						else
							if ( (combienOntJoues()==1) &&
									(ceJoueurCoupeA(suivant.suivant, couleurDemandee) &&
											(!ceJoueurCoupeA(suivant, couleurDemandee))))
								carte = donneLaGrosseDefausse();
							else
								carte = donneUnePetiteCarte();
				}
			} else { // la couleur est de l'atout, ! premier à jouer
				if ( jAiDeLaCouleur(couleurAtout))
					if ( onAPris()) {
						// Si c'est mon partenaire qui joue atout, joue une grosse
						if ( gestionnaireCarte.getTapis().size() == 2) {
							carte = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain, couleurAtout);
							if ((!gestionnaireCarte.estMaitreTapis(carte)) && 
									(gestionnaireCarte.nombreDeCartesNonJoueesPlusForteQue(carte)>0) &&
									(regle.pointsDe(carte)>=14)) {

								if ( donneCarteJusteDessous(carte, true) != null) {
									carte = donneCarteJusteDessous(carte, true);
									if ( GestionnaireCartesLecture.positionDe(carte)<
											GestionnaireCartesLecture.positionDe(
													GestionnaireCartesLecture.meilleurCarteDansA(gestionnaireCarte.getTapis(), couleurAtout)))
										carte = donneMiniAtout();
								} else
									carte = donneMiniAtout();
							}
						} else {
							carte = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain, couleurAtout);
							if ( ! gestionnaireCarte.estMaitreTapis(carte)) carte = donneMiniAtout();
						}
					} else
						carte = donneMiniAtout();
				else
					if ( monPartenaireSeraMaitre()) carte = donneLaGrosseDefausse();
					else carte = donneUnePetiteCarte();

			}
		}


		if ( (carte == null) && (couleurDemandee != null))
			if ( couleurDemandee == couleurAtout)
				carte = donneMiniAtout();
			else {
				carte = donneUnePetiteCarteA(couleurDemandee);
				if ( carte == null)
					carte = donneMiniAtout();
			}

		if ( (carte == null) && (couleurDemandee == null))
			carte = donneUneCarteMaitre();

		if ( carte == null) carte = donneUnePetiteCarte();

		carteEnMain.remove(carte);
		return carte;
	}

	private boolean onAPris() {
		return regle.getQuiAPris().equals(this) ||
				regle.getQuiAPris().getSuivant().getSuivant().equals(this);
	}

	private boolean ilOntDesAtouts() {
		return analyseur.mesAdversairesOntEncoreDu(regle.getCouleurAtout(), this) &&
				analyseur.resteCartesNonTombeesAPour(regle.getCouleurAtout(), this);

	}

	private Carte donneUneCarteMaitre() {
		Carte meilleur = null;

		for ( Carte c : carteEnMain)
			if ( ! c.estCouleur(regle.getCouleurAtout()))
				if ( gestionnaireCarte.estMaitreTapis(c)&& ((meilleur==null)||(regle.pointsDe(meilleur)<regle.pointsDe(c))))
					meilleur = c;

		return meilleur;
	}

	private boolean jAiDeLaCouleur(CouleurCarte color) {
		return carteEnMain.contient(color);
	}

	private boolean estMaitrePourPlusTard( Carte c) {
		if ( (regle.getCarteJoueePar(regle.joueurQuiPrend) == null) &&
				(!analyseur.nAPlusDAtout[regle.joueurQuiPrend.getOrdre()]) &&
				analyseur.naPlusDe.get(c.getCouleur()).contains(regle.joueurQuiPrend))
			return false;

		return ! lesToursProchainsSerontPourEux() && gestionnaireCarte.estMaitrePourPlusTard(c);
	}

	/** Joue la plus grosse carte non maitre */
	private Carte donneLaGrosseDefausse() {
		Carte atout = null, meilleur = null, maitre = null;
		int nbPP1 = 10, nbDessous1 = 10, nbPP2 = -1, nbDessous2 = -1;

		/* pour les cartes de valeurs identiques et non maitres, comparer:
           - le nombre de carte directement en dessous (prendre celle qui en a le plus)
           - sinon le nombre de carte zero (prendre celle qui en à le moins)
		 */
		for ( Carte c : carteEnMain)
			if ( ! c.getCouleur().equals(regle.getCouleurAtout())) {
				if (((GestionnaireCartesLecture.positionDe(meilleur) < GestionnaireCartesLecture.positionDe(c)))
						|| ((GestionnaireCartesLecture.positionDe(meilleur) == GestionnaireCartesLecture.positionDe(c) &&
						((nbJusteDessous(c)<nbDessous1) || (nbPlusPetiteALaCouleur(c)<nbPP1)))))
					if ( ! estMaitrePourPlusTard(c)) {
						meilleur = c;
						nbPP1 = nbPlusPetiteALaCouleur(c);
						nbDessous1 = nbJusteDessous(c);
					} else
						if ( (gestionnaireCarte.combienIlResteDeCartesNonJoueesA(c.getCouleur())>2))
							if ((maitre==null) || (GestionnaireCartesLecture.positionDe(maitre)<GestionnaireCartesLecture.positionDe(c)) ||
									((GestionnaireCartesLecture.positionDe(maitre) ==GestionnaireCartesLecture.positionDe(c)) &&
											((nbJusteDessous(c)>nbDessous2) || (nbPlusPetiteALaCouleur(c)>nbPP2)))) {
								maitre = c;
								nbPP2 = nbPlusPetiteALaCouleur(c);
								nbDessous2 = nbJusteDessous(c);
							}

			} else
				if ((atout == null) || (GestionnaireCartesLecture.positionDe(atout) < GestionnaireCartesLecture.positionDe(c)))
					if ( ! gestionnaireCarte.estMaitrePourPlusTard(c))
						if ( GestionnaireCartesLecture.positionDe(donneMiniAtout()) <= GestionnaireCartesLecture.positionDe(c))
							atout = c;

		// On sauve un bon atout qui risque de se faire zigouiller
		if ((! onAPris()) && (carteEnMain.nombreDe(regle.getCouleurAtout())==1) &&
				(atout!=null) && ((meilleur==null) || (regle.pointsDe(meilleur)+6)<regle.pointsDe(atout)) &&
				(regle.pointsDe(atout)>=10))
			return atout;

		// recherche la meilleur des cartes directement supp a la meilleur courante
		if (meilleur!=null) {
			Carte c = donneCarteJusteDessus(meilleur, true);
			if ( c!=null) meilleur = c;
		}

		if (lesToursProchainsSerontPourEux()) {
			if ( regle.pointsDe(meilleur)>regle.pointsDe(maitre))
				return meilleur;
			else
				return maitre;
		}

		return meilleur;
	}


	private boolean jAiUneCarteMaitreA(CouleurCarte couleur) {

		for ( Carte c : carteEnMain)
			if ( c.getCouleur().equals(couleur) && gestionnaireCarte.estMaitreTapis(c))
				return true;

		return false;
	}

	private Carte donneUneGrosseCarteA(CouleurCarte couleur) {

		Carte meilleur = null;

		for ( Carte c : carteEnMain)
			if ( c.getCouleur().equals(couleur))
				if ( (meilleur == null) || (GestionnaireCartesLecture.positionDe(meilleur)<GestionnaireCartesLecture.positionDe(c)))
					meilleur = c;

		return meilleur;
	}

	private Carte donneUnePetiteCarteA(CouleurCarte couleur) {

		Carte pire = null;

		for ( Carte c : carteEnMain)
			if ( c.getCouleur().equals(couleur))
				if ( (pire == null) || (GestionnaireCartesLecture.positionDe(pire)>GestionnaireCartesLecture.positionDe(c)))
					pire = c;

		return pire;
	}

	private boolean onEstMaitre() {
		PileDeCarte tapis = gestionnaireCarte.getTapis();
		Carte meilleur = GestionnaireCartesLecture.meilleurCarte(tapis);

		return ((tapis.size()==3) &&
				(meilleur==tapis.get(1))) ||
				((tapis.size()==2) && (meilleur==tapis.get(0)) &&
						(!ceJoueurCoupeA(suivant, tapis.get(0).getCouleur())));
	}

	/** Dit si le joueur coupe à la couleur */
	private boolean ceJoueurCoupeA(JoueurBelote j, CouleurCarte c) {
		return (regle.getCarteJoueePar(j)!=null) && 
				gestionnaireCarte.resteCartesNonTombeesAPour(c,j) &&
				! analyseur.nAPlusDAtout[j.ordre] &&
				analyseur.naPlusDe.get(c).contains(j);
	}

	private int combienOntJoues() {
		return regle.getTapis().size();
	}

	private Carte donneMiniAtout() {
		Carte ok = null, pire = null;
		Carte mini = gestionnaireCarte.meilleurCarteDansA(regle.getTapis(), regle.atout);

		for ( Carte c : carteEnMain)
			if ( c.getCouleur().equals(regle.getCouleurAtout())) {
				if ((pire==null) || (GestionnaireCartesLecture.positionDe(pire)>GestionnaireCartesLecture.positionDe(c)))
					pire = c;

				if ( (GestionnaireCartesLecture.positionDe(mini)<GestionnaireCartesLecture.positionDe(c))) {
					if ((ok==null) || ((GestionnaireCartesLecture.positionDe(ok)>GestionnaireCartesLecture.positionDe(c))))
						ok = c;
				}
			}

		if ( ok != null) return ok;
		else return pire;
	}

	private Carte donneUnePetiteCarte() {
		Carte pire = null, c;
		int nbPP = -1;

		for ( CouleurCarte col : CouleurCarte.COULEURS) {
			if ( ! col.equals(regle.getCouleurAtout())) {
				c = donneUnePetiteCarteA(col);
				if ( c != null)
					if ((pire==null) ||(regle.pointsDe(pire)>regle.pointsDe(c))
							|| ((regle.pointsDe(pire)==regle.pointsDe(c)) 
									&& (nbPetiteALaCouleur(c)>nbPP)))
						if ( ! gestionnaireCarte.estMaitrePourPlusTard(c)) {
							nbPP = nbPetiteALaCouleur(c);
							pire = c;
						}
			}
		}

		if ( pire == null)
			for ( CouleurCarte col : CouleurCarte.COULEURS) {
				if ( ! col.equals(regle.getCouleurAtout())) {
					c = donneUnePetiteCarteA(col);
					if ( c != null)
						if ((pire==null) ||(GestionnaireCartesLecture.positionDe(pire)>GestionnaireCartesLecture.positionDe(c)))
							pire = c;
				}
			}

		if ( pire == null)
			pire = c = donneUnePetiteCarteA(regle.getCouleurAtout());

		return pire;
	}

	private Carte donneUneCarteMaitreA(CouleurCarte couleur) {
		Carte meilleur = null;

		for ( Carte c : carteEnMain) {
			if ( c.getCouleur().equals(couleur) )
				if ( gestionnaireCarte.estMaitreTapis(c) && ((meilleur==null)||(regle.pointsDe(meilleur)<regle.pointsDe(c))))
					meilleur = c;
		}
		return meilleur;
	}

	/** Retourne true si la carte jouee par mon partenaire sera maitre */
	private boolean monPartenaireSeraMaitre() {
		Carte c = regle.getCarteJoueePar(suivant.suivant);
		if ( c == null) return false;

		if ( gestionnaireCarte.estMaitreTapis(c))
			if ( c.getCouleur().equals(regle.getCouleurAtout()))
				return true;
			else
				if ( c.getCouleur().equals(gestionnaireCarte.getCouleurDemandee())) {
					switch ( combienOntJoues()) {
					case 3: return true;
					case 2: return onEstMaitre() && (gestionnaireCarte.estMaitrePourPlusTard(
							regle.getCarteJoueePar(suivant.suivant)) &&
							! analyseur.nAPlusDAtout[suivant.ordre]);
					default: return false;
					}
				} else return false;

		return false;
	}

	/** Renvoie la plus petite carte des cartes juste au Dessous 'carte' qui se suivent */
	private Carte donneCarteJusteDessous(Carte carte, boolean auMax) {
		Carte meilleur = null;
		boolean recommence;

		// Cherche d'abord la carte juste au dessous
		for ( Carte c : carteEnMain) if ( c.estCouleur(carte) && ! c.equals(carte))
			if (GestionnaireCartesLecture.positionDe(carte)==(GestionnaireCartesLecture.positionDe(c)+1))
				meilleur = c;

		if ( auMax && (meilleur!=null))
			do {
				recommence = false;
				// Recherche la carte juste avant carte
				for ( Carte c : carteEnMain)
					if ( c.estCouleur(carte) && ! c.equals(carte))
						if (GestionnaireCartesLecture.positionDe(meilleur)==(GestionnaireCartesLecture.positionDe(c)+1)) {
							meilleur = c;
							recommence = true;
							break;
						}
			} while ( recommence);

		return meilleur;
	}

	/** Renvoie la plus petite carte des cartes juste au Dessous 'carte' qui se suivent */
	private Carte donneCarteJusteDessus(Carte carte, boolean auMax) {
		Carte meilleur = null;
		boolean recommence;

		// Cherche d'abord la carte juste au dessous
		for ( Carte c : carteEnMain) if ( c.estCouleur(carte) && ! c.equals(carte))
			if (GestionnaireCartesLecture.positionDe(carte)==(GestionnaireCartesLecture.positionDe(c)-1))
				meilleur = c;

		if ( auMax && (meilleur!=null))
			do {
				recommence = false;
				// Recherche la carte juste avant carte
				for ( Carte c : carteEnMain)
					if ( c.estCouleur(carte) && ! c.equals(carte))
						if (GestionnaireCartesLecture.positionDe(meilleur)==(GestionnaireCartesLecture.positionDe(c)-1)) {
							meilleur = c;
							recommence = true;
							break;
						}
			} while ( recommence);

		return meilleur;
	}

	/** Retourne VRAI si seulement un des deux adversaires possède autant
	 *  d'atout que le nombre de tours restants */
	private boolean lesToursProchainsSerontPourEux() {
		return ((! analyseur.nAPlusDAtout[suivant.ordre])
				^ (! analyseur.nAPlusDAtout[precedent.ordre]))
				&&
				((carteEnMain.size()-1) <= gestionnaireCarte.combienIlResteDeCartesNonJoueesA(regle.getCouleurAtout()));
	}

	/** Renvoie une carte d'atout non maitre */
	private Carte donneMeilleurAtoutNonMaitre( ) {
		Carte meilleur, ok = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain, regle.getCouleurAtout());
		boolean recommence;

		if ( donneUneCarteMaitreA(regle.getCouleurAtout()) != null)
			return donneMiniAtout();

		if ( (ok!=null) && ! gestionnaireCarte.estMaitrePourPlusTard(ok)) return ok;

		do {
			meilleur = null;
			recommence = false;
			for ( Carte c : carteEnMain.deCouleur(regle.getCouleurAtout()))
				if ( (c == donneCarteJusteDessous(ok, false) && regle.ilPeutJouerCetteCarte(this, c))) {
					ok = c;
					recommence = true;
					break;
				} else
					if ((GestionnaireCartesLecture.positionDe(c)>GestionnaireCartesLecture.positionDe(meilleur)) &&
							(GestionnaireCartesLecture.positionDe(c)<GestionnaireCartesLecture.positionDe(ok)))
						meilleur = c;

		} while ( recommence);

		if ( meilleur != null) return meilleur;
		else return ok;
	}



	private Carte donneUneQuiFaitUneCoupe() {
		Carte c, c2;
		int nbDe = 10;
		CouleurCarte atout = regle.getCouleurAtout();

		// Cherche une carte non maitre unique à la couleur
		for ( CouleurCarte col : CouleurCarte.COULEURS) {

			if ( ! col.equals(atout) && (carteEnMain.nombreDe(col)>0) && (carteEnMain.nombreDe(col)<nbDe)) {
				c = donneUnePetiteCarteA(col);
				c2 = donneUneGrosseCarteA(col);
				if ( ! gestionnaireCarte.estMaitrePourPlusTard(c) && (regle.pointsDe(c)<10) &&
						(gestionnaireCarte.nombreDeCartesNonJoueesPlusForteQue(c2) >1)
						|| (carteEnMain.nombreDe(col)>2))
					return c;
			}
		}
		return null;
	}

	/** Renvoie le nombre de cartes directement dessous */
	private int nbJusteDessous(Carte c) {
		int n = 0;
		Carte d;
		while ( (d = donneCarteJusteDessous(c, false)) != null) {
			n++;
			c = d;
		}

		return n;
	}

	private int nbPlusPetiteALaCouleur(Carte carte) {
		int n = 0;

		for ( Carte c : carteEnMain.deCouleur(carte.getCouleur()))
			if ( regle.pointsDe(c) < regle.pointsDe(carte)) n++;

		return n;
	}

	/** Renvoie le nombre de cartes ayant une valeur <= 2 pour la couleur */
	private int nbPetiteALaCouleur(Carte carte) {
		int n = 0;
		for ( Carte c : carteEnMain.deCouleur(carte.getCouleur()))
			if ( regle.pointsDe(c) <= 2) n++;

		return n;

	}

	private Carte donneUneCarteQuiFaitMaitre() {
		int nbJouees = 10, point = 20;
		Carte carte = null;

		for ( Carte c : carteEnMain)
			if ( ! c.getCouleur().equals(regle.getCouleurAtout()) &&
					(gestionnaireCarte.nombreDeCartesNonJoueesPlusForteQue(c)==1) &&
					(carteEnMain.nombreDe(c.getCouleur())>1) && ((carte == null) ||
							(regle.pointsDe(donneUnePetiteCarteA(c.getCouleur())) < point) ||
							(nbJouees > gestionnaireCarte.nombreDeCartesJoueesA(c.getCouleur())))) {

				nbJouees = gestionnaireCarte.nombreDeCartesJoueesA(c.getCouleur());
				point = regle.pointsDe(carte = donneUnePetiteCarteA(c.getCouleur()));
			}

		return carte;
	}

	private Carte donneUneQuiPrendUneGrosseDAtout() {
		if ( ilOntDesAtouts() ) {
			CouleurCarte atout = regle.getCouleurAtout();
			Carte meilleur = gestionnaireCarte.donnePlusGrosseCarteA(carteEnMain,atout);
			if ( meilleur == null) return null;

			int nombre = gestionnaireCarte.resteCarteHautesValeursBatable(atout, carteEnMain);

			if ( nombre == 1) return meilleur;
		}
		return null;
	}

	public int getNbPlis() {
		return (int) ((tas.size() +suivant.suivant.tas.size())/4);
	}
}
