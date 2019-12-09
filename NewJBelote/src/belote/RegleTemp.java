package belote;

import cartes.CouleurCarte;
import cartes.PileDeCarte;

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
	
	public CouleurCarte couleurDemandee(PileDeCarte pile) {
		if(pile.isEmpty())
			return null;
		else
			return pile.get(0).getCouleur();
	}
	
}
