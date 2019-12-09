package belote;

import cartes.Carte;
import cartes.PileDeCarte;

public class GestionnaireCartesEcriture extends GestionnaireCartesLecture {

	public GestionnaireCartesEcriture(RegleTemp regle0, PileDeCarte jeuCarte) {
		super(regle0, jeuCarte);
		// TODO Auto-generated constructor stub
	}
	
	void addCartesJouee(Carte c) {
		carteJouees.add(c);
		cartesNonJouees.remove(c);
		
		nombreJouees[ c.getCouleur().toInt()]++;
	}
	
	public void setTapis(PileDeCarte tapis) {
		this.tapis = tapis;
	}
}
