package belote;

import belote.joueur.IJoueurBelote;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;

public class RegleTemp {

	CouleurCarte couleurAtout;
	private boolean partieEnCours;
	private boolean arretPartieDemande;

	public void setPartieEnCours(boolean partieEnCours) {
		this.partieEnCours = partieEnCours;
	}

	public void setArretPartieDemande(boolean arretPartieDemande) {
		this.arretPartieDemande = arretPartieDemande;
	}
	
	/** Est-ce la fin de la partie ? */
	public boolean getEndOfGame() {
		return ! partieEnCours || arretPartieDemande;
	}

	public CouleurCarte getCouleurAtout() {
		return couleurAtout;
	}

	public void setCouleurAtout(CouleurCarte couleurAtout) {
		this.couleurAtout = couleurAtout;
	}
	
	public boolean isAtout(CouleurCarte couleur) {
		if(couleurAtout != null)
			return couleurAtout.equals(couleur);
		else
			return false;
	}
	
	public boolean isAtout(Carte carte) {
		return isAtout(carte.getCouleur());
	}
	
	public CouleurCarte getCouleurDemandee(PileDeCarte pile) {
		if(pile.isEmpty())
			return null;
		else
			return pile.get(0).getCouleur();
	}
	
    /** Renvoie les points d'une carte en fonction de l'atout actuel */
    public int pointsDe( Carte c) {
        int p = 0;
        if ( c == null) return -1;

        if ( (couleurAtout!=null) && c.estCouleur(couleurAtout))
            switch ( c.getValeur().toInt()) {
                case ValeureCarte.NCARD_V: p += 18; break;
                case ValeureCarte.NCARD_9: p += 14; break;
            }

        switch ( c.getValeur().toInt()) {
            case ValeureCarte.NCARD_AS: p += 11; break;
            case ValeureCarte.NCARD_10: p += 10; break;
            case ValeureCarte.NCARD_R: p += 4; break;
            case ValeureCarte.NCARD_D: p += 3; break;
            case ValeureCarte.NCARD_V: p += 2; break;
        }
        return p;
    }

    public int compteLesPointsDe(PileDeCarte tas) {
        int p = 0;
        for ( Carte c : tas) p += pointsDe(c);
        return p;
    }
	
    /** Vérifie que le joueur 'lui' peut jouer cette 'carte' */
    public boolean ilPeutJouerCetteCarte(PileDeCarte tapis, IJoueurBelote lui, Carte carte) {

        Carte p;
        PileDeCarte saMain = lui.getMain();
        if (tapis.size()==0) return true;
        p = tapis.get(0);

        if (carte.estCouleur(p)) { // si même couleur
            if ( p.estCouleur(couleurAtout)) { // si atout demandé
                return verifieAtoutJouePar(tapis, lui, carte);
            } else return true;

        } else { // ! même couleur
            if ( p.estCouleur(couleurAtout)) { // si atout demandé
                    if (  lui.getMain().contient(couleurAtout)) return false;
                    else return verifieDefausse(tapis, lui, carte);
            } else { // ! même couleur, ! atout demandé
                if ( saMain.contient(p.getCouleur())) return false;

                if ( carte.estCouleur(couleurAtout)) {
                    if ( saMain.contient(p.getCouleur())) return false;
                    else return verifieAtoutJouePar(tapis, lui, carte);
                } else { // il devrait couper mais ne l'a pas fait

                   return verifieDefausse(tapis, lui, carte);
                }
            }
        }
    }
    
    /** Vérifie que le joueur 'lui' peut jouer cette atout 'carte' */
    private boolean verifieAtoutJouePar(PileDeCarte tapis, IJoueurBelote lui, Carte carte) {
        Carte m = GestionnaireCartesLecture.meilleurCarteDansA(tapis, couleurAtout);

        if ( GestionnaireCartesLecture.positionDe(carte) > GestionnaireCartesLecture.positionDe(m)) return true;
        else {
            carte = GestionnaireCartesLecture.meilleurCarteDansA(lui.getMain(), couleurAtout);
            if ( carte == null) return true;
            if ( GestionnaireCartesLecture.positionDe(carte) < GestionnaireCartesLecture.positionDe(m)) return true;
        }
        return false;
    }
    
    /** Vérifie que le joueur a le droit de ne pas couper */
    private boolean verifieDefausse(PileDeCarte tapis, IJoueurBelote lui, Carte carte) {
    	PileDeCarte saMain = lui.getMain();
         if ( ! saMain.contient(couleurAtout)) return true;
        // son partenaire est il maitre ?
        if ( tapis.size()>=2)
            if ( GestionnaireCartesLecture.meilleurCarte(tapis).equals(tapis.get(tapis.size()-2)))
                return true;

        if ( ! saMain.contient(couleurAtout)) return true;
        return false;
    }
    
}
