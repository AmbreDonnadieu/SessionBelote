package belote;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import belote.joueur.IJoueurBelote;
import belote.joueur.JoueurHumain;
import belote.joueur.JoueurIA;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;

public class AnalyseurJeuTemp implements Runnable {
	RegleTemp regle;
	/* Liste des joueurs qui coupent à la couleur */
	HashMap<CouleurCarte,ArrayList<IJoueurBelote>> naPlusDe, aCouleur;
	/* Liste des joueurs qui n'ont plus d'atout */
	boolean[] nAPlusDAtout= new boolean[4];
	GestionnaireCartesEcriture gestionnaireCartes;

	public static final int ETAT_RIEN = 0;
	public static final int ETAT_DISTRIBUE1 = 1;
	public static final int ETAT_ATOUT1 = 2;
	public static final int ETAT_ATOUT2 = 3;
	public static final int ETAT_DISTRIBUE2 = 4;
	public static final int ETAT_REFUS_JEU = 5;
	public static final int ETAT_JOUE = 6;
	public static final int ETAT_FINTOUR = 7;
	public static final int ETAT_FINPARTIE = 8;
	public static final int ETAT_FINJEU = 9;
	public static final int ETAT_COUPEJEU = 10;
	public static final String[] stringEtats = {
			"Aucune partie en cours",
			"Première distribution",
			"Premier tour d'atout",
			"Deuxième tour d'atout",
			"Distribution du restant des cartes",
			"Personne ne veut prendre",
			"Partie en cours", "Fin du tour",
			"Fin de la partie",
			"Fin du jeu", "Coupe du jeu" };

	public static final int JOUEUR_NORD  = 0;
	public static final int JOUEUR_EST   = 1;
	public static final int JOUEUR_SUD   = 2;
	public static final int JOUEUR_OUEST = 3;
	public static final String POSITIONS[] = { "NORD", "EST", "SUD", "OUEST" };

	/* Le délai d'attente pour règler la vitesse du jeu */
	private int gameDelay = 1400;

	/** Le nombre de points par manches par défaut */
	public static int POINTS_MANCHE = 1000;

	AnalyseurInterfaceGraphique graphic_listener;
	AnalyseurListener listener;

	ArrayList<IJoueurBelote> joueurs;
	IJoueurBelote joueurQuiDistribue;
	IJoueurBelote joueurCourant;
	IJoueurBelote joueurQuiPrend;
	IJoueurBelote joueurQuiCommence;
	CouleurCarte atout;
	public PileDeCarte dernierPli;  // la copie du dernier pli
	public Carte cartePrise;
	public StatistiquesBelote statistique;

	boolean partieEnCours;
	private boolean arretPartieDemande;
	int intEtatDuJeu, pointsRemisEnJeux;

	/** Contient le joueur qui a fait belote et rebelote ou null */
	private IJoueurBelote beloteEtRe;

	public boolean confirmPlis;
	private boolean confirmJeu = true, confirmBelote = true, confirmRebelote = false;
	private boolean avecAnnonces;
	private boolean avecHumain;

	/** Pour les statistiques */
	public int nbParties, nbManches, nbRefus, maxManches = 10000;


	public AnalyseurJeuTemp() {
		// TODO Auto-generated constructor stub
		naPlusDe = new HashMap<CouleurCarte, ArrayList<IJoueurBelote>>();
		for( CouleurCarte c : CouleurCarte.COULEURS)
			naPlusDe.put(c, new ArrayList<IJoueurBelote>());
	}

	void nouvellePartie() {
		for ( int i = 0; i < 4; i++) {
			nAPlusDAtout[i] = false;
		}

		for( ArrayList<IJoueurBelote> l : naPlusDe.values()) 
			l.clear();

		gestionnaireCartes.nouvellePartie();
	}


	void addCarteJouee(Carte c) {
		PileDeCarte p = gestionnaireCartes.getTapis();

		// Vérifie qui devait fournir mais ne l'a pas fait
		if ( !  c.getCouleur().equals(regle.getCouleurDemandee(p)))
			if ( ! regle.getCouleurDemandee(p).equals(regle.getCouleurAtout())) {
				naPlusDe.get(regle.getCouleurDemandee(p)).add(joueurCourant);
				if ( ! c.getCouleur().equals(regle.getCouleurAtout()))
					switch (p.size()) {
					case 2: // il n'a plus d'atout
						joueurCourant.setNAPlusDe(atout);
						break;
					case 3:
					case 4: if ( gestionnaireCartes.meilleurCarte(p) == p.get(p.size()-3))
						break; // Le partenaire est maitre on ne peut pas savoir
					else
						joueurCourant.setNAPlusDe(atout);
					}
			} else // il n'a pas fourni à la couleur qui était l'atout
				joueurCourant.setNAPlusDe(atout);

		if ( regle.isAtout(c) && 
				(gestionnaireCartes.combienIlResteDeCartesNonJoueesA(c.getCouleur()) == 0)) {
			// Les 8 atouts sont tombés, plus personne ne coupe
			for ( int i = 0; i < 4; i ++)
				nAPlusDAtout[i] = true;
		}
	}



	/* Crée une partie réseau avec ces quatre joueurs placé dans le sens horaire en commencant par le Nord. */
	public AnalyseurJeuTemp( PileDeCarte avecCeJeu, IJoueurBelote[] quatre_joueurs, int quiCommence) {
		int i;
		regle = new RegleTemp();
		gestionnaireCartes = new GestionnaireCartesEcriture(regle, avecCeJeu);

		statistique = new StatistiquesBelote();

		avecHumain = false;
		joueurs = new ArrayList<IJoueurBelote>(4);
		for ( i = 0; i < 4; i++ ) {
			if ( quatre_joueurs[i] instanceof JoueurHumain) avecHumain = true;
			quatre_joueurs[i].setOrdre( i);
			joueurs.add(quatre_joueurs[i] );
		}

		for ( i = 0; i< 4 ; i++)
			joueurs.get(i).setEntreLesJoueurs(
					joueurs.get((i+3)%4), joueurs.get((i+1)%4));

		joueurQuiDistribue = joueurs.get(quiCommence).getPrecedent(); // pour distribuer
		joueurCourant = joueurQuiDistribue.getPrecedent(); // pour couper le jeu
		partieEnCours = false;
		setGameSpeed(15);
		try { changeEtat(ETAT_RIEN); } catch (Exception ex) { }
	}

	/* Crée une partie locale composé de 3 joueur ordinateurs et d'un 'humain' placé au SUD du gestionnaireCartes.getTapis() */
	public AnalyseurJeuTemp( PileDeCarte avecCeJeu, IJoueurBelote humain) {
		int i;

		regle = new RegleTemp();
		gestionnaireCartes = new GestionnaireCartesEcriture(regle, avecCeJeu);
		avecHumain = humain != null;

		statistique = new StatistiquesBelote();
		if (nbManches == maxManches) nbManches = 0;

		joueurs = new ArrayList<IJoueurBelote>(4);
		for ( i = 0; i < 4; i++ )
			/*           if ( (i%2) == 1)
	                joueurs.add( new JoueurBelote(POSITIONS[i], i));
	            else
	                joueurs.add( new JoueurBelote1(POSITIONS[i], i));
	 /* */
			if ( (humain!=null) && (i == humain.getOrdre()))
				joueurs.add( humain);
			else
				joueurs.add( new JoueurIA(POSITIONS[i], i));
		/* */


		for ( i = 0; i< 4 ; i++)
			joueurs.get(i).setEntreLesJoueurs(
					joueurs.get((i+3)%4), joueurs.get((i+1)%4));

		joueurQuiDistribue = joueurCourant = joueurs.get(JOUEUR_EST);
		joueurCourant = joueurQuiDistribue.getPrecedent();

		/* Avec un tas commun :
	        PileDeCarte p1 = new PileDeCarte();
	        PileDeCarte p2 = new PileDeCarte();
	        joueurs.get(0).setTas(p1);
	        joueurs.get(1).setTas(p2);
	        joueurs.get(2).setTas(p1);
	        joueurs.get(3).setTas(p2);*/
		for ( i = 0; i < 4; i++) joueurs.get(i).setTas(new PileDeCarte());

		partieEnCours = false;
		try { changeEtat(ETAT_RIEN); } catch (Exception ex) { }
	}

	/* Change la vitesse de 0 à 20 (20=max) */;
	public void setGameSpeed(int speed) {
		if ( speed < 0) speed = 0;
		if ( speed > 20) speed = 20;
		gameDelay = (20-speed) * 200;
	}

	/** Doit-on afficher le gagnant de chaque pli ? */
	public void setConfirmMode( boolean mode) {
		confirmPlis = mode;
	}

	/** Doit on confirmer le gagnant de chaque partie ? */
	public void setConfirmModeJeu(boolean selected) {
		confirmJeu = selected;
	}

	/** Doit-on jouer avec l'annonce de la belote ? */
	public void setConfirmBelote( boolean mode) {
		confirmBelote = mode;
	}

	/** Renvoie le joueur à la 'position' */
	public IJoueurBelote getJoueur( int position) {
		return joueurs.get( position);
	}

	/** Spécifie le nouveau 'listener' graphique pour cette partie */
	public void setGraphicListener(AnalyseurInterfaceGraphique listener0) {
		graphic_listener = listener0;
	}

	/** Spécifie le nouveau 'listener' pour cette partie */
	public void setBeloteListener( AnalyseurListener l) {
		listener = l;
	}

	/** Renvoie le 'listener' actuel pour cette partie */
	public AnalyseurListener getBeloteListener() {
		return listener;
	}

	/** Demande un clique de la part de l'utilisateur */
	private void waitClick() {
		graphic_listener.unlockRead();
		graphic_listener.waitClick();
		graphic_listener.lockRead();
	}

	/** Fait jouer les quatre participants des parties, jusqu'à ce que l'on demande le fin définitive du jeu */
	@Override
	public void run() {

		arretPartieDemande = false;
		partieEnCours = true;
		synchroReseau( joueurCourant);
		graphic_listener.lockRead();

		try {

			do {
				nbParties++;
				changeEtat(ETAT_COUPEJEU);
				joueurQuiPrend = beloteEtRe = null;
				nouvellePartie();


				int coupe_a = joueurCourant.cut();
				if ( listener != null)
					listener.newBeloteEvent(
							new BeloteEvent(BeloteEvent.EV_COUPE, joueurCourant,
									coupe_a, null, null));

				gestionnaireCartes.coupeJeuA(coupe_a);
				joueurCourant = joueurQuiDistribue.getSuivant();
				setJoueurQuiCommence(joueurCourant);
				doSynchroReseau(joueurCourant);

				changeEtat( ETAT_DISTRIBUE1);
				for ( int j = 0; j < 4; j++) {
					joueurCourant.ajoutATaMain( gestionnaireCartes.distribueCartes(3));
					joueurCourant = joueurCourant.getSuivant();
					changeEtat( ETAT_DISTRIBUE1);
				}
				for ( int j = 0; j < 4; j++) {
					joueurCourant.ajoutATaMain( gestionnaireCartes.distribueCartes(2));
					joueurCourant = joueurCourant.getSuivant();
					changeEtat( ETAT_DISTRIBUE1);
				}
				gestionnaireCartes.getTapis().add( gestionnaireCartes.distribueUneCarte());
				atout = null;
				waitSynchroReseau();

				boolean okPrise = true;
				do {
					changeEtat( ETAT_ATOUT1);
					CouleurCarte a = null;
					int n = 0;
					do {
						if ( joueurCourant.getChoixAtout1( gestionnaireCartes.getTapis().get(0))) {
							a = gestionnaireCartes.getTapis().get(0).getCouleur();
							joueurQuiPrend = joueurCourant;

						} else {
							joueurCourant = joueurCourant.getSuivant();
						}
						n = n + 1;

						if ( listener != null)
							listener.newBeloteEvent(
									new BeloteEvent(BeloteEvent.EV_ATOUT1, 
											(a==null)?joueurCourant.getPrecedent():joueurCourant, (a==null)?0:1, null, null));

					} while ( (a==null) && (n < 4));

					if ( a==null) synchroReseau(joueurCourant);

					if ( a == null) {
						changeEtat( ETAT_ATOUT2);
						n = 0;
						do {
							a = joueurCourant.getChoixAtout2(gestionnaireCartes.getColorFirstCarte());
							if ( a != null) {
								if ( a.equals(gestionnaireCartes.getTapis().get(0).getCouleur())) {
									graphic_listener.unlockRead();
									JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Le joueur "+ joueurCourant.getNom() + " ne peut choisir " +
											a + " au deuxième tour d'atout.", "Désolé ...", JOptionPane.ERROR_MESSAGE);
									graphic_listener.lockRead();
									a = null;
								} else
									joueurQuiPrend = joueurCourant;
							} else {

								joueurCourant = joueurCourant.getSuivant();
								n = n + 1;

							}

							if ( listener != null)
								listener.newBeloteEvent(
										new BeloteEvent(BeloteEvent.EV_ATOUT2,
												(a==null)?joueurCourant.getPrecedent():joueurCourant, 0, a, null));

						} while ( (a==null) && (n < 4));
					}

					doSynchroReseau(joueurQuiCommence);

					if ( a == null ) { // personne ne prend on rend les cartes

						if ( confirmPlis) {
							graphic_listener.unlockRead();
							JOptionPane.showMessageDialog(graphic_listener.getComponent(),
									"Personne n'a pris, on recommence", "Info", JOptionPane.INFORMATION_MESSAGE);
							graphic_listener.lockRead();
						}

						changeEtat( ETAT_REFUS_JEU);
						for ( int j = 0; j < 4; j++) {
							gestionnaireCartes.remettreCartesDansJeu( joueurCourant.rendTaMain());
							joueurCourant = joueurCourant.getSuivant();
						}

						gestionnaireCartes.remettreUneCarteDansJeu(gestionnaireCartes.getTapis().donneUneCarte());
						atout = null;
						okPrise = true;

						changeEtat( ETAT_FINTOUR);
						waitSynchroReseau();

					} else { // distribue la fin du jeu

						atout = a;

						if ( confirmPlis) {
							graphic_listener.unlockRead();
							JOptionPane.showMessageDialog(graphic_listener.getComponent(),
									"Le joueur " + joueurQuiPrend.getNom() + " a pris à " +
											atout, "Info", JOptionPane.INFORMATION_MESSAGE);
							/*     int r =  JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
	                                "Le joueur " + joueurQuiPrend.getNom() + " a pris à " +
	                                atout, "Info", JOptionPane.INFORMATION_MESSAGE);
							 */
							graphic_listener.lockRead();
							/*   if ( r == JOptionPane.NO_OPTION) {
	                            for ( int j = 0; j < 4; j++) {
	                                jeu.addAll( joueurCourant.rendTaMain());
	                                joueurCourant = joueurCourant.suivant;
	                            }
	                            jeu.add(gestionnaireCartes.getTapis().donneUneCarte());
	                            atout = null;
	                            changeEtat( ETAT_FINTOUR);
	                            break;
	                        } else if ( r == JOptionPane.CANCEL_OPTION)
	                            continue;
	                        else */ okPrise = true;

						}

						changeEtat( ETAT_DISTRIBUE2);
						joueurCourant = joueurQuiDistribue.getSuivant();

						joueurQuiPrend.ajoutATaMain(cartePrise = gestionnaireCartes.getTapis().donneUneCarte());

						for ( int j = 0; j < 4; j++) {
							if ( joueurCourant != joueurQuiPrend )
								joueurCourant.ajoutATaMain( gestionnaireCartes.distribueCartes(3));
							else
								joueurCourant.ajoutATaMain( gestionnaireCartes.distribueCartes(2));

							joueurCourant.sortCartesEnMain();
							joueurCourant = joueurCourant.getSuivant();
							changeEtat( ETAT_DISTRIBUE2);
						}
						waitSynchroReseau();
					}
				} while ( ! okPrise);

				if ( ! okPrise) continue;

				if ( avecAnnonces) verifieLesAnnonces();

				if ( atout != null ) {
					this.setJoueurQuiCommence(joueurQuiDistribue.getSuivant());
					for (int tour = 1; tour <= 8; tour++) {
						joueUnTour();
						changeEtat(ETAT_FINTOUR);
						if ( ! faitGagnerLeTour() ) tour--;
					}

					// Compte les points
					int total = 162;
					int leurPoints = joueurQuiPrend.getSuivant().getNbreDePoints()+
							joueurQuiPrend.getPrecedent().getNbreDePoints();
					int points = joueurQuiPrend.getNbreDePoints()+ 
							joueurQuiPrend.getSuivant().getSuivant().getNbreDePoints();

					// 10 de der
					if ( (joueurQuiCommence == joueurQuiPrend) || (joueurQuiCommence == joueurQuiPrend.getSuivant().getSuivant()))
						points += 10;
					else leurPoints += 10;

					if (confirmBelote && (beloteEtRe != null)) {
						total = 182;
						if ((beloteEtRe==joueurQuiPrend) || (beloteEtRe==joueurQuiPrend.getSuivant().getSuivant()))
							points += 20;
						else
							leurPoints += 20;
					}

					changeEtat(ETAT_FINPARTIE);
					joueurQuiPrend.nbPrisePlusUn();

					graphic_listener.unlockRead();
					if ( (joueurQuiPrend.getSizeTas()==0) && (joueurQuiPrend.getSuivant().getSuivant().getSizeTas()==0) ) {
						if ( confirmJeu) { int i =
								JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
										"L'équipe " + joueurQuiPrend.getSuivant().getNom() + "," + joueurQuiPrend.getPrecedent().getNom() +
										", à fait un capot !\nElle gagne " + (total + 90) + " points.",
										"Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
						}

						joueurQuiPrend.getSuivant().addPointsTotaux(total + 90 + pointsRemisEnJeux);
						statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, points, total + 90 + pointsRemisEnJeux);
						pointsRemisEnJeux = 0;
						joueurQuiPrend.nbPerdusPlusUn();

					} else if ( (joueurQuiPrend.getSuivant().getSizeTas()==0) && (joueurQuiPrend.getPrecedent().getSizeTas()==0)) {
						if ( confirmJeu) { int i =
								JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
										"L'équipe " + joueurQuiPrend.getNom() +","+ joueurQuiPrend.getSuivant().getSuivant().getNom() +
										", à fait un capot !\nElle gagne " + (points + 90) + " points.",
										"Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
						}

						joueurQuiPrend.addPointsTotaux(points + 90 + pointsRemisEnJeux);
						joueurQuiPrend.getSuivant().addPointsTotaux(leurPoints);
						statistique.ajoutPartie( joueurQuiPrend, true, beloteEtRe, total + 90 + pointsRemisEnJeux, leurPoints);
						pointsRemisEnJeux = 0;
						joueurQuiPrend.nbCapotPlusUn();

					} else if ( points == leurPoints) {
						if ( confirmJeu) { int i =
								JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
										"Il y a litige !\nLes deux camps on autant de points ("+
												points+").\nL'équipe " + joueurQuiPrend.getSuivant().getNom() + "," + joueurQuiPrend.getPrecedent().getNom() +
												", gagne tout de suite " + points + ".\nLe reste est remis en jeu.",
												"Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
						}

						joueurQuiPrend.getSuivant().addPointsTotaux(points);
						pointsRemisEnJeux += points;
						statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, 0, leurPoints);

					} else if ( points > leurPoints) {
						if ( confirmJeu) { int i =
								JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
										joueurQuiPrend.getNom() + "," + joueurQuiPrend.getSuivant().getSuivant().getNom() +" gagne la partie avec " +
										points + " points contre " + leurPoints + ", sur un total de " + total + ".",
										"Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
						}

						joueurQuiPrend.getSuivant().addPointsTotaux(leurPoints);
						joueurQuiPrend.addPointsTotaux(points+ pointsRemisEnJeux);
						statistique.ajoutPartie( joueurQuiPrend, true, beloteEtRe, points+ pointsRemisEnJeux, leurPoints);
						pointsRemisEnJeux = 0;

					} else {
						if ( confirmJeu) { int i=
								JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
										joueurQuiPrend.getNom() + "," + joueurQuiPrend.getSuivant().getSuivant().getNom() + " est \"dedans\" avec seulement " +
										points + " points contre " + leurPoints + ", sur un total de " + total + ".",
										"Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
						}

						joueurQuiPrend.getSuivant().addPointsTotaux(total + pointsRemisEnJeux);
						statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, -points, total+ pointsRemisEnJeux);
						pointsRemisEnJeux = 0;
						joueurQuiPrend.nbPerdusPlusUn();
					}

					/* Vérfication de la fin d'une manche */
					int tN = joueurs.get(JOUEUR_NORD).getPointsTotaux() + joueurs.get(JOUEUR_SUD).getPointsTotaux();
					int tE = joueurs.get(JOUEUR_OUEST).getPointsTotaux()  + joueurs.get(JOUEUR_EST).getPointsTotaux();

					if ( (tN >= POINTS_MANCHE) || (tE >= POINTS_MANCHE)) {
						nbManches++;

						if ( tN > tE) {

							if ( avecHumain) {
								int i =JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
										getJoueur(JOUEUR_NORD).getNom() + "," + getJoueur(JOUEUR_SUD).getNom() + " à gagnée la manche avec " +
										tN + " points contre " + tE + ".",
										"Fin de manche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
								if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
							}

							statistique.ajoutManche(joueurs.get(JOUEUR_SUD));

						} else {

							if ( avecHumain) {

								int i = JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
										getJoueur(JOUEUR_EST).getNom() + "," + getJoueur(JOUEUR_OUEST).getNom() + 
										" à gagnée la manche avec " + tE + " points contre " + tN + ".",
										"Fin de manche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
								if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
							}

							statistique.ajoutManche(joueurs.get(JOUEUR_EST));
						}

						for ( int i = 0; i < 4; i ++) {
							joueurCourant.ptTotauxVersTotaux2();
							joueurCourant = joueurCourant.getSuivant();
						}

					}
					graphic_listener.lockRead();
					if ( ! confirmPlis && avecHumain) waitClick();

					joueurQuiPrend = null;

					for (int i = 0; i < 4; i++) {
						gestionnaireCartes.remettreCartesDansJeu(joueurCourant.rendTonTas());
						joueurCourant = joueurCourant.getSuivant();
					}

					if ( nbManches == maxManches ) intEtatDuJeu = ETAT_FINJEU;
				} else
					nbRefus++;

				joueurQuiDistribue = joueurQuiDistribue.getSuivant();
				joueurCourant = joueurQuiDistribue.getPrecedent(); // celui qui coupe
				synchroReseau( joueurCourant);

			} while ( intEtatDuJeu != ETAT_FINJEU );

		} catch (Exception ex) {  // fin de partie immédiate

			if ( ex.getMessage() == null) ex.printStackTrace();
			else if ( ! ex.getMessage().equals("Fin de partie")) {
				System.out.println("Fin de partie par l'exception: "+ ex);
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Exception :\n"+ex.getMessage(),"Fin de partie", JOptionPane.ERROR_MESSAGE);
			}

		}
		try { changeEtat(ETAT_RIEN); } catch (Exception ex) { }

		for ( IJoueurBelote j : joueurs) {
			gestionnaireCartes.remettreCartesDansJeu(j.rendTonTas());
			gestionnaireCartes.remettreCartesDansJeu(j.rendTaMain());
		}
		gestionnaireCartes.remettreCartesDansJeu(gestionnaireCartes.getTapis()); gestionnaireCartes.getTapis().clear();

		arretPartieDemande = partieEnCours = false;
		graphic_listener.endOfGame();
		if ( listener != null)
			listener.newBeloteEvent( new BeloteEvent(BeloteEvent.EV_QUIT, null, 0, null, null));
	}

	/** Pour déboggage programmeur */
	boolean tourDebug;

	/* Fait jouer une carte à chaque joueur */
	public void joueUnTour() throws Exception {

		Carte c;
		joueurCourant = joueurQuiCommence;

		for ( int j = 0; j < 4; j++) {
			do {
				changeEtat( ETAT_JOUE);
				/* Pour le débogage */
				if ( tourDebug )
					tourDebug = false;

				c = joueurCourant.joueUneCarte();

				if ( c != null ) {
					if ( ! ilPeutJouerCetteCarte( joueurCourant, c) ) {
						joueurCourant.ajoutATaMain(c);
						joueurCourant.trieTaMain();
						graphic_listener.unlockRead();

						JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Le joueur "+ joueurCourant.getNom() + " ne peut jouer " +
								c, "Désolé ...", JOptionPane.ERROR_MESSAGE);
						graphic_listener.lockRead();
						c = null;
					}
					else {
						if ( c.estCouleur(atout)) {
							if (((c.getValeur().equals(ValeureCarte.CARD_R)) && joueurCourant.getMain().contient(atout, ValeureCarte.CARD_D)) ||
									((c.getValeur().equals(ValeureCarte.CARD_D)) && joueurCourant.getMain().contient(atout, ValeureCarte.CARD_R))) {

								if ( confirmBelote && avecHumain) {
									graphic_listener.unlockRead();
									JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Belote pour le joueur " + joueurCourant.getNom(), "Belote",
											JOptionPane.INFORMATION_MESSAGE);
									graphic_listener.lockRead();
								}
								beloteEtRe = joueurCourant;
							} else if ( (beloteEtRe == joueurCourant) && confirmRebelote &&
									(c.getValeur().equals(ValeureCarte.CARD_R) ||
											c.getValeur().equals(ValeureCarte.CARD_D)) ) {
								graphic_listener.unlockRead();
								JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Rebelote pour le joueur " + joueurCourant.getNom(), "Belote",
										JOptionPane.INFORMATION_MESSAGE);
								graphic_listener.lockRead();
							}

						}

						if ( listener != null)
							listener.newBeloteEvent(
									new BeloteEvent(BeloteEvent.EV_CARTE, joueurCourant, 0, null, c));

						gestionnaireCartes.getTapis().add( c);
						addCarteJouee( c);
					}
				}

			} while (c == null);
			joueurCourant = joueurCourant.getSuivant();
		}
	}

	/** Vérifie que le joueur 'lui' peut jouer cette atout 'carte' */
	private boolean verifieAtoutJouePar( IJoueurBelote lui, Carte carte) {
		Carte m = GestionnaireCartesLecture.meilleurCarteDansA(gestionnaireCartes.getTapis(), atout);

		if ( GestionnaireCartesLecture.positionDe(carte) > GestionnaireCartesLecture.positionDe(m)) return true;
		else {
			carte = GestionnaireCartesLecture.meilleurCarteDansA(lui.getMain(), atout);
			if ( carte == null) return true;
			if ( GestionnaireCartesLecture.positionDe(carte) < GestionnaireCartesLecture.positionDe(m)) return true;
		}
		return false;
	}

	/** Vérifie que le joueur 'lui' peut jouer cette 'carte' */
	public boolean ilPeutJouerCetteCarte( IJoueurBelote lui, Carte carte) {

		Carte p;
		if (gestionnaireCartes.getTapis().size()==0) return true;
		p = gestionnaireCartes.getTapis().get(0);

		if (carte.estCouleur(p)) { // si même couleur
			if ( p.estCouleur(atout)) { // si atout demandé
				return verifieAtoutJouePar( lui, carte);
			} else return true;

		} else { // ! même couleur
			if ( p.estCouleur(atout)) { // si atout demandé
				if (  lui.getMain().contient(atout)) return false;
				else return verifieDefausse(lui, carte);
			} else { // ! même couleur, ! atout demandé
				if ( lui.getMain().contient(p.getCouleur())) return false;

				if ( carte.estCouleur(atout)) {
					if ( lui.getMain().contient(p.getCouleur())) return false;
					else return verifieAtoutJouePar(lui, carte);
				} else { // il devrait couper mais ne l'a pas fait

					return verifieDefausse( lui, carte);
				}
			}
		}
	}

	/** Vérifie que le joueur a le droit de ne pas couper */
	private boolean verifieDefausse( IJoueurBelote lui, Carte carte) {

		if ( ! lui.getMain().contient(atout)) return true;

		// son partenaire est il maitre ?
		if ( gestionnaireCartes.getTapis().size()>=2)
			if ( GestionnaireCartesLecture.meilleurCarte(gestionnaireCartes.getTapis()).equals(gestionnaireCartes.getTapis().get(gestionnaireCartes.getTapis().size()-2)))
				return true;

		if ( ! lui.getMain().contient(atout)) return true;
		return false;
	}

	/** Donne les cartes au joueur, et compte les points */
	private boolean faitGagnerLeTour() {

		Carte gagnante = GestionnaireCartesLecture.meilleurCarte(gestionnaireCartes.getTapis());
		IJoueurBelote j = joueurQuiCommence;

		for ( int i = 0; i < 4 ; i++)
			if ( gestionnaireCartes.getTapis().get(i) == gagnante)
				break;
			else j = j.getSuivant();

		doSynchroReseau(j);

		if ( confirmPlis) {
			graphic_listener.unlockRead();
			JOptionPane.showMessageDialog(graphic_listener.getComponent(), j.getNom() + " gagne ce pli.", "Qui remporte ?", JOptionPane.INFORMATION_MESSAGE);
			//int i = JOptionPane.showConfirmDialog(graphic_listener.getComponent(), j.getNom() + " gagne le pli", "Qui remporte ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			graphic_listener.lockRead();
			/* if ( i == JOptionPane.CANCEL_OPTION) {
	                // On redonne les cartes
	                for ( i = 0; i < 4; i++) {
	                    joueurCourant.ajoutATaMain(gestionnaireCartes.getTapis().get(i));
	                    joueurCourant = joueurCourant.suivant;
	                }
	                gestionnaireCartes.getTapis().clear();
	                return false;
	            } */
		} else
			if ( avecHumain ) waitClick();

		waitSynchroReseau();

		dernierPli = (PileDeCarte)gestionnaireCartes.getTapis().clone();
		j.ajoutATonTas( gestionnaireCartes.getTapis());
		setJoueurQuiCommence(j);
		gestionnaireCartes.getTapis().clear();
		return true;
	}

	/** Change l'état de la partie */
	private void changeEtat(int etat0) throws Exception {

		intEtatDuJeu = etat0;
		graphic_listener.unlockRead();
		graphic_listener.setStatus(stringEtats[intEtatDuJeu] + " ... ");

		if ( arretPartieDemande ) {
			intEtatDuJeu = ETAT_RIEN;
			throw new Exception("Fin de partie");

		}
		graphic_listener.repaint();
		if (etat0 != ETAT_FINTOUR)
			if ( (gameDelay > 0) )
				if ( ((etat0 == ETAT_DISTRIBUE1) || (etat0 == ETAT_DISTRIBUE2)))
					try { Thread.sleep(gameDelay/2); } catch ( InterruptedException e) { }
				else
					try { Thread.sleep(gameDelay); } catch ( InterruptedException e) { }
			else
				try { Thread.sleep(gameDelay); } catch ( InterruptedException e) { }

		graphic_listener.lockRead();
	}

	/** Demande la fin de la partie, en demandant aussi à l'utilisateur si 'verif' */
	public void demandeLaFinDeLaPartie(boolean verif) {
		if ( verif )
			if ((graphic_listener != null) && graphic_listener.verifyEndOfGame())
				arretPartieDemande = true;
			else
				return;
		arretPartieDemande = true;
	}

	/** Retourne le joueur qui doit joueur maintenant */
	public IJoueurBelote getJoueurCourrant() {
		return joueurCourant;
	}

	/* Retourne les cartes jouées à ce tour sur le gestionnaireCartes.getTapis() */
	public PileDeCarte getTapis() {
		return gestionnaireCartes.getTapis();
	}

	/** Retourne le joueur qui à distribué les carte de la partie en cours */
	public IJoueurBelote getJoueurQuiDistribue() {
		return joueurQuiDistribue;
	}

	/** Retourne le joueur qui à pris au cours de la partie actuelle */
	public IJoueurBelote getQuiAPris() {
		if ( (intEtatDuJeu == ETAT_FINPARTIE ) ||
				(intEtatDuJeu == ETAT_JOUE ) ||
				(intEtatDuJeu == ETAT_FINTOUR ) ||
				(intEtatDuJeu == ETAT_DISTRIBUE2 ))
			return joueurQuiPrend;
		else return null;
	}

	/** Retourne la couleur choisie à l'atout pour cette partie */
	public CouleurCarte getCouleurAtout() {
		if ((intEtatDuJeu == ETAT_ATOUT1) || (intEtatDuJeu == ETAT_ATOUT2)) return null;
		return atout;
	}

	/** Retourne la couleur demandée au 1er tour d'atout */
	public CouleurCarte getCouleurDemandee() {
		if ( gestionnaireCartes.getTapis().isEmpty() ) return null;
		else return gestionnaireCartes.getTapis().get(0).getCouleur();

	}

	/** Retourne le joueur qui a commencé le premier à ce tour */
	public IJoueurBelote getJoueurQuiCommence() {
		return joueurQuiCommence;
	}

	/** Retourne la carte du gestionnaireCartes.getTapis() jouee par le 'joueur' */
	Carte getCarteJoueePar(IJoueurBelote joueur) {
		IJoueurBelote j = joueurQuiCommence;
		for ( Carte c : gestionnaireCartes.getTapis())
			if ( j == joueur) return c;
			else j = j.getSuivant();

		return null;

	}

	/** Renvoie les points d'une carte en fonction de l'atout actuel */
	public int pointsDe( Carte c) {
		int p = 0;
		if ( c == null) return -1;

		if ( (atout!=null) && c.estCouleur(atout))
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



	/** Renvoie l'état du jeu */
	public int getEtatDuJeu() {
		return intEtatDuJeu;
	}

	/** Retourne le nom du joueur qui distribue */
	public String getQuiDistribue() {
		return joueurQuiDistribue.getNom();
	}

	/** Pour jouer avec les annonces ou pas */
	public void setAvecAnnonces(boolean selected) {
		avecAnnonces = selected;
	}

	/** Pour gèrer les annonces */
	private void verifieLesAnnonces() {

		return;

		// TODO: voir si il faut finir le mode avec les annonces...
	}

	/** Renvoie le 'listener' graphique associé à cette partie */
	public AnalyseurInterfaceGraphique getGraphicListener() {
		return graphic_listener;
	}


	private void synchroReseau( IJoueurBelote commenceAvec) {
		graphic_listener.unlockRead();
		if ( listener != null) 
			listener.newBeloteEvent(
					new BeloteEvent(BeloteEvent.EV_SYNCHRO, commenceAvec, 0, null, null));
		graphic_listener.lockRead();
	}

	/** Est-ce la fin de la partie ? */
	public boolean getEndOfGame() {
		return ! partieEnCours || arretPartieDemande;
	}

	public void changeJoueurPar(IJoueurBelote remplacant) {
		IJoueurBelote j = joueurs.get(remplacant.getOrdre());
		joueurs.set(remplacant.getOrdre(), remplacant);
		remplacant.getSuivant().precedent = remplacant;
		remplacant.precedent.suivant = remplacant;
		if ( joueurCourant == j ) joueurCourant = remplacant;
		if ( joueurQuiCommence == j ) joueurQuiCommence = remplacant;
		if ( joueurQuiDistribue == j ) joueurQuiDistribue = remplacant;
		if ( joueurQuiPrend == j ) joueurQuiPrend = remplacant;
		// TODO: vérifier où en est la partie cas là c'est quand même pas cool...
	}
	
	private void setJoueurQuiCommence(IJoueurBelote joueurQuiCommence) {
		this.joueurQuiCommence = joueurQuiCommence;
		for(IJoueurBelote j : joueurs) {
			j.setJoueurQuiCommence(joueurQuiCommence);
		}
	}

	boolean synchroReseauOk;
	private void doSynchroReseau(IJoueurBelote j) {
		synchroReseauOk = false;
		new Thread(new Runnable() {
			IJoueurBelote j;
			public Runnable setJ( IJoueurBelote j0) { j = j0; return this; }
			@Override public void run() {
				synchroReseau(j);
				synchroReseauOk = true;
			}
		}.setJ(j)).run();
	}

	private void waitSynchroReseau() {
		graphic_listener.unlockRead();
		while ( ! synchroReseauOk && partieEnCours) {
			try { Thread.sleep(100); } catch (InterruptedException ex) { }
		}
		graphic_listener.lockRead();
	}

	public void setConfirmRebelote(boolean armed) {
		confirmRebelote = armed;
	}

	public RegleTemp getRegle() {
		// TODO Auto-generated method stub
		return regle;
	}

}
