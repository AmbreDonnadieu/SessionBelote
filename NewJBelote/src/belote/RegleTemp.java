package belote;

import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;

public class RegleTemp {

	CouleurCarte couleurAtout;

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

    private int compteLesPointsDe(PileDeCarte tas) {
        int p = 0;
        for ( Carte c : tas) p += pointsDe(c);
        return p;
    }
	
    /** Vérifie que le joueur 'lui' peut jouer cette 'carte' */
    public boolean ilPeutJouerCetteCarte( JoueurBelote lui, Carte carte) {

        Carte p, c;
        if (tapis.size()==0) return true;
        p = tapis.get(0);

        if (carte.estCouleur(p)) { // si même couleur
            if ( p.estCouleur(couleurAtout)) { // si atout demandé
                return verifieAtoutJouePar( lui, carte);
            } else return true;

        } else { // ! même couleur
            if ( p.estCouleur(atout)) { // si atout demandé
                    if (  lui.main.contient(atout)) return false;
                    else return verifieDefausse(lui, carte);
            } else { // ! même couleur, ! atout demandé
                if ( lui.main.contient(p.getCouleur())) return false;

                if ( carte.estCouleur(atout)) {
                    if ( lui.main.contient(p.getCouleur())) return false;
                    else return verifieAtoutJouePar(lui, carte);
                } else { // il devrait couper mais ne l'a pas fait

                   return verifieDefausse( lui, carte);
                }
            }
        }
    }
}
