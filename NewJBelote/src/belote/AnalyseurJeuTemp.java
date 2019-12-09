package belote;

import java.util.ArrayList;
import java.util.HashMap;

import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;

public class AnalyseurJeuTemp {
	RegleTemp regle;
	/* Liste des joueurs qui coupent à la couleur */
	HashMap<CouleurCarte,ArrayList<JoueurBelote>> naPlusDe, aCouleur;
	/* Liste des joueurs qui n'ont plus d'atout */
	boolean[] nAPlusDAtout= new boolean[4];
	GestionnaireCartesEcriture gestionnaireCartes;
	
	public AnalyseurJeuTemp() {
		// TODO Auto-generated constructor stub
		naPlusDe = new HashMap<CouleurCarte, ArrayList<JoueurBelote>>();
		for( CouleurCarte c : CouleurCarte.COULEURS)
			naPlusDe.put(c, new ArrayList<JoueurBelote>());
	}
	
	public boolean mesAdversairesOntEncoreDu(CouleurCarte couleur, JoueurBelote moi) {
		if ( regle.isAtout(couleur))
			return ! (nAPlusDAtout[moi.suivant.ordre] && nAPlusDAtout[moi.precedent.ordre]);
		else
			return ! (naPlusDe.get(couleur).contains(moi.suivant) || naPlusDe.get(couleur).contains(moi.precedent));
	}
	
	private void setNaPlusDatout( JoueurBelote j) {
		nAPlusDAtout[j.ordre] = true;
		if ( (regle.getCouleurAtout() == null) || (naPlusDe.get(regle.getCouleurAtout())==null))
			System.out.print("PAS BON");
		naPlusDe.get(regle.getCouleurAtout()).add(regle.joueurCourant);
		/*   for ( CouleurCarte col : CouleurCarte.COULEURS)
	            naPlusDe.get(col).remove(regle.joueurCourant); */
	}
	
	void nouvellePartie() {
		for ( int i = 0; i < 4; i++) {
			nAPlusDAtout[i] = false;
		}

		for( ArrayList<JoueurBelote> l : naPlusDe.values()) 
			l.clear();

		gestionnaireCartes.nouvellePartie();
	}

	
	void addCarteJouee(Carte c) {
		carteJouees.add(c);
		cartesNonJouees.remove(c);
		PileDeCarte p = regle.getTapis();

		nombreJouees[ c.getCouleur().toInt()]++;

		// Vérifie qui devait fournir mais ne l'a pas fait
		if ( !  c.getCouleur().equals(regle.getCouleurDemandee()))
			if ( ! regle.getCouleurDemandee().equals(regle.getCouleurAtout())) {
				naPlusDe.get(regle.getCouleurDemandee()).add(regle.joueurCourant);
				if ( ! c.getCouleur().equals(regle.getCouleurAtout()))
					switch (p.size()) {
					case 2: // il n'a plus d'atout
						setNaPlusDatout(regle.joueurCourant);
						break;
					case 3:
					case 4: if ( meilleurCarte(p) == p.get(p.size()-3))
						break; // Le partenaire est maitre on ne peut pas savoir
					else
						setNaPlusDatout(regle.joueurCourant);
					}
			} else // il n'a pas fourni à la couleur qui était l'atout
				setNaPlusDatout(regle.joueurCourant);


		if ( c.getCouleur().equals(regle.getCouleurAtout()) && 
				(nombreJouees[ regle.getCouleurAtout().toInt()] == 8)) {
			// Les 8 atouts sont tombés, plus personne ne coupe
			for ( int i = 0; i < 4; i ++)
				nAPlusDAtout[i] = true;
		}
	}
	
}
