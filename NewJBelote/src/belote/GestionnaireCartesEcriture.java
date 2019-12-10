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
	
	public void coupeJeuA(int pos) {
		jeuDeCarte.coupeA(pos);
	}
	
	public PileDeCarte distribueCartes(int nb) {
		return jeuDeCarte.donneDesCartes(nb);
	}
	
	public Carte distribueUneCarte() {
		return jeuDeCarte.donneUneCarte();
	}
	
	public void remettreCartesDansJeu(PileDeCarte p) {
		jeuDeCarte.addAll(p);
	}
	
	
	public void remettreUneCarteDansJeu(Carte c) {
		jeuDeCarte.add(c);
	}
}
