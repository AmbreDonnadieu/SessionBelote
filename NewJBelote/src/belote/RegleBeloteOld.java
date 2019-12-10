/*
  A Belote game

  Copyright (C) 2012 Clément GERARDIN.

  This file is part of Belote.

  Gforth is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 3
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.
*/

package belote;

import cartes.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * GÃ¨re une suite de partie de belote
 * @author Clément
 */
public class RegleBeloteOld implements Runnable {

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
                            "PremiÃ¨re distribution",
                            "Premier tour d'atout",
                            "DeuxiÃ¨me tour d'atout",
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

    /* Le délai d'attente pour rÃ¨gler la vitesse du jeu */
    private int gameDelay = 1400;

    /** Le nombre de points par manches par défaut */
    public static int POINTS_MANCHE = 1000;

    RegleBeloteInterfaceGraphique graphic_listener;
    RegleBeloteListener listener;

    ArrayList<JoueurBelote> joueurs;
    JoueurBelote joueurQuiDistribue;
    JoueurBelote joueurCourant;
    JoueurBelote joueurQuiPrend;
    JoueurBelote joueurQuiCommence;
    CouleurCarte atout;
    PileDeCarte tapis;
    PileDeCarte jeu;
    public PileDeCarte dernierPli;  // la copie du dernier pli
    public Carte cartePrise;
    AnalyseurDeJeu analyseur;
    public StatistiquesBelote statistique;

    boolean partieEnCours;
    private boolean arretPartieDemande;
    int intEtatDuJeu, pointsRemisEnJeux;

    /** Contient le joueur qui a fait belote et rebelote ou null */
    private JoueurBelote beloteEtRe;

    public boolean confirmPlis;
    private boolean confirmJeu = true, confirmBelote = true, confirmRebelote = false;
    private boolean avecAnnonces;
    private boolean avecHumain;

    /** Pour les statistiques */
    public int nbParties, nbManches, nbRefus, maxManches = 10000;

    /* Crée une partie réseau avec ces quatre joueurs placé dans le sens horaire en commencant par le Nord. */
    public RegleBeloteOld( PileDeCarte avecCeJeu, JoueurBelote[] quatre_joueurs, int quiCommence) {
        int i;

        jeu = avecCeJeu;
        tapis = new PileDeCarte();
        analyseur = new AnalyseurDeJeu(this);
        statistique = new StatistiquesBelote();

        avecHumain = false;
        joueurs = new ArrayList<JoueurBelote>(4);
        for ( i = 0; i < 4; i++ ) {
            if ( quatre_joueurs[i] instanceof JoueurHumain) avecHumain = true;
            quatre_joueurs[i].setOrdre( i);
            quatre_joueurs[i].setRegle(this);
            joueurs.add(quatre_joueurs[i] );
        }
            
        for ( i = 0; i< 4 ; i++)
            joueurs.get(i).setEntreLesJoueurs(
                    joueurs.get((i+3)%4), joueurs.get((i+1)%4));

        joueurQuiDistribue = joueurs.get(quiCommence).precedent; // pour distribuer
        joueurCourant = joueurQuiDistribue.precedent; // pour couper le jeu
        partieEnCours = false;
        setGameSpeed(15);
        try { changeEtat(ETAT_RIEN); } catch (Exception ex) { }
    }

    /* Crée une partie locale composé de 3 joueur ordinateurs et d'un 'humain' placé au SUD du tapis */
    public RegleBeloteOld( PileDeCarte avecCeJeu, JoueurBelote humain) {
        int i;

        jeu = avecCeJeu;
        avecHumain = humain != null;
        
        tapis = new PileDeCarte();
        analyseur = new AnalyseurDeJeu(this);
        statistique = new StatistiquesBelote();
        if (nbManches == maxManches) nbManches = 0;

        joueurs = new ArrayList<JoueurBelote>(4);
        for ( i = 0; i < 4; i++ )
 /*           if ( (i%2) == 1)
                joueurs.add( new JoueurBelote(POSITIONS[i], i));
            else
                joueurs.add( new JoueurBelote1(POSITIONS[i], i));
 /* */
            if ( (humain!=null) && (i == humain.ordre))
                joueurs.add( humain);
            else
                joueurs.add( new JoueurBelote(POSITIONS[i], i));
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

        for ( i = 0; i < 4; i++) joueurs.get(i).setRegle(this);

        partieEnCours = false;
        try { changeEtat(ETAT_RIEN); } catch (Exception ex) { }
    }

    /* Change la vitesse de 0 Ã  20 (20=max) */;
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

    /** Renvoie le joueur Ã  la 'position' */
    public JoueurBelote getJoueur( int position) {
        return joueurs.get( position);
    }

    /** Spécifie le nouveau 'listener' graphique pour cette partie */
    public void setGraphicListener(RegleBeloteInterfaceGraphique listener0) {
        graphic_listener = listener0;
    }

    /** Spécifie le nouveau 'listener' pour cette partie */
    public void setBeloteListener( RegleBeloteListener l) {
        listener = l;
    }

    /** Renvoie le 'listener' actuel pour cette partie */
    public RegleBeloteListener getBeloteListener() {
        return listener;
    }

    /** Demande un clique de la part de l'utilisateur */
    private void waitClick() {
        graphic_listener.unlockRead();
        graphic_listener.waitClick();
        graphic_listener.lockRead();
    }

    /** Fait jouer les quatre participants des parties, jusqu'Ã  ce que l'on demande le fin définitive du jeu */
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
                analyseur.nouvellePartie();

                
                int coupe_a = joueurCourant.cut();
                if ( listener != null)
                    listener.newBeloteEvent(
                        new BeloteEvent(BeloteEvent.EV_COUPE, joueurCourant,
                                            coupe_a, null, null));
                
                jeu.coupeA(coupe_a);
                joueurCourant = joueurQuiCommence = joueurQuiDistribue.getSuivant();
                doSynchroReseau(joueurCourant);

                changeEtat( ETAT_DISTRIBUE1);
                for ( int j = 0; j < 4; j++) {
                    joueurCourant.ajoutATaMain( jeu.donneDesCartes(3));
                    joueurCourant = joueurCourant.getSuivant();
                    changeEtat( ETAT_DISTRIBUE1);
                }
                for ( int j = 0; j < 4; j++) {
                    joueurCourant.ajoutATaMain( jeu.donneDesCartes(2));
                    joueurCourant = joueurCourant.getSuivant();
                    changeEtat( ETAT_DISTRIBUE1);
                }
                tapis.add( jeu.donneUneCarte());
                atout = null;
                waitSynchroReseau();

                boolean okPrise = true;
                do {
                changeEtat( ETAT_ATOUT1);
                CouleurCarte a = null;
                int n = 0;
                do {
                    if ( joueurCourant.getChoixAtout1( tapis.get(0))) {
                        a = tapis.get(0).getCouleur();
                        joueurQuiPrend = joueurCourant;

                    } else {
                        joueurCourant = joueurCourant.getSuivant();
                    }
                    n = n + 1;

                    if ( listener != null)
                        listener.newBeloteEvent(
                            new BeloteEvent(BeloteEvent.EV_ATOUT1, 
                                    (a==null)?joueurCourant.precedent:joueurCourant, (a==null)?0:1, null, null));

                } while ( (a==null) && (n < 4));

                if ( a==null) synchroReseau(joueurCourant);

                if ( a == null) {
                    changeEtat( ETAT_ATOUT2);
                    n = 0;
                    do {
                        a = joueurCourant.getChoixAtout2(jeu.get(0).getCouleur());
                        if ( a != null) {
                            if ( a.equals(tapis.get(0).getCouleur())) {
                                graphic_listener.unlockRead();
                                JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Le joueur "+ joueurCourant.nom + " ne peut choisir " +
                                        a + " au deuxiÃ¨me tour d'atout.", "Désolé ...", JOptionPane.ERROR_MESSAGE);
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
                                            (a==null)?joueurCourant.precedent:joueurCourant, 0, a, null));

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
                        jeu.addAll( joueurCourant.rendTaMain());
                        joueurCourant = joueurCourant.suivant;
                    }

                    jeu.add(tapis.donneUneCarte());
                    atout = null;
                    okPrise = true;
                    
                    changeEtat( ETAT_FINTOUR);
                    waitSynchroReseau();

                } else { // distribue la fin du jeu

                    atout = a;

                    if ( confirmPlis) {
                        graphic_listener.unlockRead();
                        JOptionPane.showMessageDialog(graphic_listener.getComponent(),
                                "Le joueur " + joueurQuiPrend.nom + " a pris Ã  " +
                                atout, "Info", JOptionPane.INFORMATION_MESSAGE);
                   /*     int r =  JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
                                "Le joueur " + joueurQuiPrend.nom + " a pris Ã  " +
                                atout, "Info", JOptionPane.INFORMATION_MESSAGE);
*/
                        graphic_listener.lockRead();
                     /*   if ( r == JOptionPane.NO_OPTION) {
                            for ( int j = 0; j < 4; j++) {
                                jeu.addAll( joueurCourant.rendTaMain());
                                joueurCourant = joueurCourant.suivant;
                            }
                            jeu.add(tapis.donneUneCarte());
                            atout = null;
                            changeEtat( ETAT_FINTOUR);
                            break;
                        } else if ( r == JOptionPane.CANCEL_OPTION)
                            continue;
                        else */ okPrise = true;

                    }
                    
                    changeEtat( ETAT_DISTRIBUE2);
                    joueurCourant = joueurQuiDistribue.getSuivant();

                    joueurQuiPrend.ajoutATaMain(cartePrise = tapis.donneUneCarte());

                    for ( int j = 0; j < 4; j++) {
                        if ( joueurCourant != joueurQuiPrend )
                            joueurCourant.ajoutATaMain( jeu.donneDesCartes(3));
                        else
                            joueurCourant.ajoutATaMain( jeu.donneDesCartes(2));

                        joueurCourant.main.sort(analyseur);
                        joueurCourant = joueurCourant.getSuivant();
                        changeEtat( ETAT_DISTRIBUE2);
                    }
                    waitSynchroReseau();
                }
                } while ( ! okPrise);

                if ( ! okPrise) continue;

                if ( avecAnnonces) verifieLesAnnonces();

                if ( atout != null ) {
                    joueurQuiCommence = joueurQuiDistribue.getSuivant();
                    for (int tour = 1; tour <= 8; tour++) {
                        joueUnTour();
                        changeEtat(ETAT_FINTOUR);
                        if ( ! faitGagnerLeTour() ) tour--;
                    }

                    // Compte les points
                    int total = 162;
                    int leurPoints = compteLesPointsDe( joueurQuiPrend.suivant.tas) +
                            compteLesPointsDe( joueurQuiPrend.precedent.tas);
                    int points = compteLesPointsDe( joueurQuiPrend.tas) + 
                                    compteLesPointsDe( joueurQuiPrend.suivant.suivant.tas);

                    // 10 de der
                    if ( (joueurQuiCommence == joueurQuiPrend) || (joueurQuiCommence == joueurQuiPrend.suivant.suivant))
                        points += 10;
                    else leurPoints += 10;

                    if (confirmBelote && (beloteEtRe != null)) {
                        total = 182;
                        if ((beloteEtRe==joueurQuiPrend) || (beloteEtRe==joueurQuiPrend.suivant.suivant))
                            points += 20;
                        else
                            leurPoints += 20;
                    }

                    changeEtat(ETAT_FINPARTIE);
                    joueurQuiPrend.nbPrises++;

                    graphic_listener.unlockRead();
                    if ( (joueurQuiPrend.tas.size()==0) && (joueurQuiPrend.suivant.suivant.tas.size()==0) ) {
                        if ( confirmJeu) { int i =
                            JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
                                    "L'équipe " + joueurQuiPrend.suivant.nom + "," + joueurQuiPrend.precedent.nom +
                                    ", Ã  fait un capot !\nElle gagne " + (total + 90) + " points.",
                                    "Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                        joueurQuiPrend.suivant.pointsTotaux += total + 90 + pointsRemisEnJeux;
                        statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, points, total + 90 + pointsRemisEnJeux);
                        pointsRemisEnJeux = 0;
                        joueurQuiPrend.nbPerdues++;
                        
                    } else if ( (joueurQuiPrend.suivant.tas.size()==0) && (joueurQuiPrend.precedent.tas.size()==0)) {
                        if ( confirmJeu) { int i =
                            JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
                                    "L'équipe " + joueurQuiPrend.nom +","+ joueurQuiPrend.suivant.suivant.nom +
                                    ", Ã  fait un capot !\nElle gagne " + (points + 90) + " points.",
                                    "Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                        joueurQuiPrend.pointsTotaux += points + 90 + pointsRemisEnJeux;
                        joueurQuiPrend.suivant.pointsTotaux += leurPoints;
                        statistique.ajoutPartie( joueurQuiPrend, true, beloteEtRe, total + 90 + pointsRemisEnJeux, leurPoints);
                        pointsRemisEnJeux = 0;
                        joueurQuiPrend.nbCapot++;
                        
                    } else if ( points == leurPoints) {
                        if ( confirmJeu) { int i =
                            JOptionPane.showConfirmDialog(graphic_listener.getComponent(),
                                "Il y a litige !\nLes deux camps on autant de points ("+
                                points+").\nL'équipe " + joueurQuiPrend.suivant.nom + "," + joueurQuiPrend.precedent.nom +
                                ", gagne tout de suite " + points + ".\nLe reste est remis en jeu.",
                                "Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                        joueurQuiPrend.suivant.pointsTotaux += points;
                        pointsRemisEnJeux += points;
                        statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, 0, leurPoints);

                    } else if ( points > leurPoints) {
                        if ( confirmJeu) { int i =
                            JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
                                joueurQuiPrend.nom + "," + joueurQuiPrend.suivant.suivant.nom +" gagne la partie avec " +
                                points + " points contre " + leurPoints + ", sur un total de " + total + ".",
                                "Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }
                        
                        joueurQuiPrend.suivant.pointsTotaux += leurPoints;
                        joueurQuiPrend.pointsTotaux += points+ pointsRemisEnJeux;
                        statistique.ajoutPartie( joueurQuiPrend, true, beloteEtRe, points+ pointsRemisEnJeux, leurPoints);
                        pointsRemisEnJeux = 0;

                    } else {
                        if ( confirmJeu) { int i=
                            JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
                            joueurQuiPrend.nom + "," + joueurQuiPrend.suivant.suivant.nom + " est \"dedans\" avec seulement " +
                                points + " points contre " + leurPoints + ", sur un total de " + total + ".",
                                "Résultats de la partie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                        joueurQuiPrend.suivant.pointsTotaux += total + pointsRemisEnJeux;
                        statistique.ajoutPartie( joueurQuiPrend, false, beloteEtRe, -points, total+ pointsRemisEnJeux);
                        pointsRemisEnJeux = 0;
                        joueurQuiPrend.nbPerdues++;
                    }

                    /* Vérfication de la fin d'une manche */
                    int tN = joueurs.get(JOUEUR_NORD).pointsTotaux + joueurs.get(JOUEUR_SUD).pointsTotaux;
                    int tE = joueurs.get(JOUEUR_OUEST).pointsTotaux  + joueurs.get(JOUEUR_EST).pointsTotaux;

                    if ( (tN >= RegleBelote.POINTS_MANCHE) || (tE >= RegleBelote.POINTS_MANCHE)) {
                        nbManches++;
                        
                        if ( tN > tE) {

                            if ( avecHumain) {
                             int i =JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
                                     getJoueur(RegleBelote.JOUEUR_NORD).nom + "," + getJoueur(RegleBelote.JOUEUR_SUD).nom + " Ã  gagnée la manche avec " +
                                tN + " points contre " + tE + ".",
                                "Fin de manche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                            if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                            statistique.ajoutManche(joueurs.get(JOUEUR_SUD));

                        } else {

                            if ( avecHumain) {
                                
                                int i = JOptionPane.showConfirmDialog(graphic_listener.getComponent(), "L'équipe " +
                                        getJoueur(RegleBelote.JOUEUR_EST).nom + "," + getJoueur(RegleBelote.JOUEUR_OUEST).nom + 
                                        " Ã  gagnée la manche avec " + tE + " points contre " + tN + ".",
                                    "Fin de manche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                if ( i == JOptionPane.CANCEL_OPTION ) demandeLaFinDeLaPartie(true);
                            }

                            statistique.ajoutManche(joueurs.get(JOUEUR_EST));
                        }

                        for ( int i = 0; i < 4; i ++) {
                            joueurCourant.pointsTotaux2 += joueurCourant.pointsTotaux;
                            joueurCourant.pointsTotaux = 0;
                            joueurCourant = joueurCourant.suivant;
                        }

                    }
                    graphic_listener.lockRead();
                    if ( ! confirmPlis && avecHumain) waitClick();

                    joueurQuiPrend = null;

                    for (int i = 0; i < 4; i++) {
                        jeu.addAll(joueurCourant.rendTonTas());
                        joueurCourant = joueurCourant.getSuivant();
                    }

                    if ( nbManches == maxManches ) intEtatDuJeu = ETAT_FINJEU;
                } else
                    nbRefus++;

                joueurQuiDistribue = joueurQuiDistribue.suivant;
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

        for ( JoueurBelote j : joueurs) {
            jeu.addAll(j.rendTonTas());
            jeu.addAll(j.rendTaMain());
        }
        jeu.addAll(tapis); tapis.clear();

        arretPartieDemande = partieEnCours = false;
        graphic_listener.endOfGame();
        if ( listener != null)
                listener.newBeloteEvent( new BeloteEvent(BeloteEvent.EV_QUIT, null, 0, null, null));
    }

    /** Pour déboggage programmeur */
    boolean tourDebug;

    /* Fait jouer une carte Ã  chaque joueur */
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
                        joueurCourant.main.sort(analyseur);
                        graphic_listener.unlockRead();

                        JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Le joueur "+ joueurCourant.nom + " ne peut jouer " +
                                c, "Désolé ...", JOptionPane.ERROR_MESSAGE);
                        graphic_listener.lockRead();
                        c = null;
                    }
                    else {
                        if ( c.estCouleur(atout)) {
                            if (((c.getValeur().equals(ValeureCarte.CARD_R)) && joueurCourant.main.contient(atout, ValeureCarte.CARD_D)) ||
                                ((c.getValeur().equals(ValeureCarte.CARD_D)) && joueurCourant.main.contient(atout, ValeureCarte.CARD_R))) {

                                    if ( confirmBelote && avecHumain) {
                                        graphic_listener.unlockRead();
                                        JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Belote pour le joueur " + joueurCourant.nom, "Belote",
                                            JOptionPane.INFORMATION_MESSAGE);
                                        graphic_listener.lockRead();
                                    }
                                    beloteEtRe = joueurCourant;
                            } else if ( (beloteEtRe == joueurCourant) && confirmRebelote &&
                                            (c.getValeur().equals(ValeureCarte.CARD_R) ||
                                             c.getValeur().equals(ValeureCarte.CARD_D)) ) {
                                graphic_listener.unlockRead();
                                JOptionPane.showMessageDialog(graphic_listener.getComponent(), "Rebelote pour le joueur " + joueurCourant.nom, "Belote",
                                            JOptionPane.INFORMATION_MESSAGE);
                                graphic_listener.lockRead();
                            }

                        }

                        if ( listener != null)
                            listener.newBeloteEvent(
                                new BeloteEvent(BeloteEvent.EV_CARTE, joueurCourant, 0, null, c));

                        tapis.add( c);
                        analyseur.addCarteJouee( c);
                    }
                }

            } while (c == null);
            joueurCourant = joueurCourant.getSuivant();
        }
    }

    /** Vérifie que le joueur 'lui' peut jouer cette atout 'carte' */
    private boolean verifieAtoutJouePar( JoueurBelote lui, Carte carte) {
        Carte m = analyseur.meilleurCarteDansA(tapis, atout);

        if ( analyseur.positionDe(carte) > analyseur.positionDe(m)) return true;
        else {
            carte = analyseur.meilleurCarteDansA(lui.main, atout);
            if ( carte == null) return true;
            if ( analyseur.positionDe(carte) < analyseur.positionDe(m)) return true;
        }
        return false;
    }

    /** Vérifie que le joueur 'lui' peut jouer cette 'carte' */
    public boolean ilPeutJouerCetteCarte( JoueurBelote lui, Carte carte) {

        Carte p, c;
        if (tapis.size()==0) return true;
        p = tapis.get(0);

        if (carte.estCouleur(p)) { // si mÃªme couleur
            if ( p.estCouleur(atout)) { // si atout demandé
                return verifieAtoutJouePar( lui, carte);
            } else return true;

        } else { // ! mÃªme couleur
            if ( p.estCouleur(atout)) { // si atout demandé
                    if (  lui.main.contient(atout)) return false;
                    else return verifieDefausse(lui, carte);
            } else { // ! mÃªme couleur, ! atout demandé
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

    /** Vérifie que le joueur a le droit de ne pas couper */
    private boolean verifieDefausse( JoueurBelote lui, Carte carte) {

         if ( ! lui.main.contient(atout)) return true;

        // son partenaire est il maitre ?
        if ( tapis.size()>=2)
            if ( analyseur.meilleurCarte(tapis).equals(tapis.get(tapis.size()-2)))
                return true;

        if ( ! lui.main.contient(atout)) return true;
        return false;
    }

    /** Donne les cartes au joueur, et compte les points */
    private boolean faitGagnerLeTour() {
    	
        Carte gagnante = analyseur.meilleurCarte(tapis);
        JoueurBelote j = joueurQuiCommence;

        for ( int i = 0; i < 4 ; i++)
            if ( tapis.get(i) == gagnante)
                break;
            else j = j.suivant;

        doSynchroReseau(j);

        if ( confirmPlis) {
            graphic_listener.unlockRead();
            JOptionPane.showMessageDialog(graphic_listener.getComponent(), j.nom + " gagne ce pli.", "Qui remporte ?", JOptionPane.INFORMATION_MESSAGE);
            //int i = JOptionPane.showConfirmDialog(graphic_listener.getComponent(), j.nom + " gagne le pli", "Qui remporte ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            graphic_listener.lockRead();
            /* if ( i == JOptionPane.CANCEL_OPTION) {
                // On redonne les cartes
                for ( i = 0; i < 4; i++) {
                    joueurCourant.ajoutATaMain(tapis.get(i));
                    joueurCourant = joueurCourant.suivant;
                }
                tapis.clear();
                return false;
            } */
        } else
            if ( avecHumain ) waitClick();

        waitSynchroReseau();
        
        dernierPli = (PileDeCarte)tapis.clone();
        j.ajoutATonTas( tapis);
        joueurQuiCommence = j;
        tapis.clear();
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

    /** Demande la fin de la partie, en demandant aussi Ã  l'utilisateur si 'verif' */
    public void demandeLaFinDeLaPartie(boolean verif) {
        if ( verif )
            if ((graphic_listener != null) && graphic_listener.verifyEndOfGame())
                arretPartieDemande = true;
            else
                return;
        arretPartieDemande = true;
    }

    /** Retourne le joueur qui doit joueur maintenant */
    public JoueurBelote getJoueurCourrant() {
        return joueurCourant;
    }

    /* Retourne les cartes jouées Ã  ce tour sur le tapis */
    public PileDeCarte getTapis() {
        return tapis;
    }

    /** Retourne le joueur qui Ã  distribué les carte de la partie en cours */
    public JoueurBelote getJoueurQuiDistribue() {
        return joueurQuiDistribue;
    }

    /** Retourne le joueur qui Ã  pris au cours de la partie actuelle */
    public JoueurBelote getQuiAPris() {
        if ( (intEtatDuJeu == ETAT_FINPARTIE ) ||
             (intEtatDuJeu == ETAT_JOUE ) ||
             (intEtatDuJeu == ETAT_FINTOUR ) ||
             (intEtatDuJeu == ETAT_DISTRIBUE2 ))
                return joueurQuiPrend;
        else return null;
    }

    /** Retourne la couleur choisie Ã  l'atout pour cette partie */
    public CouleurCarte getCouleurAtout() {
        if ((intEtatDuJeu == ETAT_ATOUT1) || (intEtatDuJeu == ETAT_ATOUT2)) return null;
        return atout;
    }

    /** Retourne la couleur demandée au 1er tour d'atout */
    public CouleurCarte getCouleurDemandee() {
        if ( tapis.isEmpty() ) return null;
        else return tapis.get(0).getCouleur();

    }

    /** Donne l'analyseur de jeu de cette partie */
    public AnalyseurDeJeu getAnalyseur() {
        return analyseur;
    }

    /** Retourne le joueur qui a commencé le premier Ã  ce tour */
    public JoueurBelote getJoueurQuiCommence() {
        return joueurQuiCommence;
    }

    /** Retourne la carte du tapis jouee par le 'joueur' */
    Carte getCarteJoueePar(JoueurBelote joueur) {
        JoueurBelote j = joueurQuiCommence;
        for ( Carte c : tapis)
            if ( j == joueur) return c;
            else j = j.suivant;

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

    private int compteLesPointsDe(PileDeCarte tas) {
        int p = 0;
        for ( Carte c : tas) p += pointsDe(c);
        return p;
    }

    /** Renvoie l'état du jeu */
    public int getEtatDuJeu() {
        return intEtatDuJeu;
    }

    /** Retourne le nom du joueur qui distribue */
    public String getQuiDistribue() {
        return joueurQuiDistribue.nom;
    }

    /** Renvoie le jeu de carte utilisé dans cette partie */
    public PileDeCarte getJeu() {
        return jeu;
    }

    /** Pour jouer avec les annonces ou pas */
    public void setAvecAnnonces(boolean selected) {
        avecAnnonces = selected;
    }


    /** Pour gÃ¨rer les annonces */
    private void verifieLesAnnonces() {

        return;

        // TODO: voir si il faut finir le mode avec les annonces...
        /* seul l'annonce la plus forte peut Ãªtre annoncée.
           elle peut Ãªtre découverte au deuxiÃ¨me tour si un des adversaires le demandent
         * l'annonce se rajoute au total des points de la partie donc au 152+10
         
        boolean cartes[][];

        for ( JoueurBelote j : joueurs ) {
            cartes = new boolean[4][ValeureCarte.SET_BELOTE.length];
            for ( Carte c :j.getMain())
                cartes[c.getCouleur().toInt()][c.getValeur().toInt()] = true;

            // Vérifie les carrés de 9, V D, R
            for ( int v = ValeureCarte.NCARD_9; v < ValeureCarte.NCARD_R; v++) {

                int n = 0;
                for ( int i = 0; i < 4 ; i++) if (cartes[n][v]) n++;
                if ( n == 4) switch (v) {
                    case ValeureCarte.NCARD_9:
                        annonceCarre(j, ValeureCarte.CARD_9, 150);
                        j.pointsTotaux += 150;
                        break;
                    case ValeureCarte.NCARD_V:
                        annonceCarre(j, ValeureCarte.CARD_V, 200);
                        j.pointsTotaux += 200;
                        break;
                    case ValeureCarte.NCARD_D:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                    case ValeureCarte.NCARD_R:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                    case ValeureCarte.NCARD_10:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                }
            }

            // Vérifie les suites
            for ( int v = ValeureCarte.NCARD_7; v < ValeureCarte.NCARD_AS; v++) {

                int n = 0;
                for ( int i = 0; i < 4 ; i++) if (cartes[n][v]) n++;
                if ( n == 4) switch (v) {
                    case ValeureCarte.NCARD_9:
                        annonceCarre(j, ValeureCarte.CARD_9, 150);
                        j.pointsTotaux += 150;
                        break;
                    case ValeureCarte.NCARD_V:
                        annonceCarre(j, ValeureCarte.CARD_V, 200);
                        j.pointsTotaux += 200;
                        break;
                    case ValeureCarte.NCARD_D:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                    case ValeureCarte.NCARD_R:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                    case ValeureCarte.NCARD_10:
                        annonceCarre(j, ValeureCarte.CARD_V, 100);
                        j.pointsTotaux += 100;
                        break;
                }
            }
        }
*/
    }

    /** Renvoie le 'listener' graphique associé Ã  cette partie */
    public RegleBeloteInterfaceGraphique getGraphicListener() {
        return graphic_listener;
    }


    private void synchroReseau( JoueurBelote commenceAvec) {
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

    public void changeJoueurPar(JoueurBelote remplacant) {
        JoueurBelote j = joueurs.get(remplacant.ordre);
        joueurs.set(remplacant.ordre, remplacant);
        remplacant.suivant.precedent = remplacant;
        remplacant.precedent.suivant = remplacant;
        if ( joueurCourant == j ) joueurCourant = remplacant;
        if ( joueurQuiCommence == j ) joueurQuiCommence = remplacant;
        if ( joueurQuiDistribue == j ) joueurQuiDistribue = remplacant;
        if ( joueurQuiPrend == j ) joueurQuiPrend = remplacant;
        // TODO: vérifier oÃ¹ en est la partie cas lÃ  c'est quand mÃªme pas cool...
    }

    boolean synchroReseauOk;
    private void doSynchroReseau(JoueurBelote j) {
        synchroReseauOk = false;
        new Thread(new Runnable() {
            JoueurBelote j;
            public Runnable setJ( JoueurBelote j0) { j = j0; return this; }
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

    /*
    private ValeureCarte valeurCarre;
    private int pointsAnnonces;

    protected void annonceCarre( JoueurBelote lui, ValeureCarte val, int points) {

        pointsAnnonces = points;
        valeurCarre = val;
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Le joueur " + j.nom + 
                            " annonce un carré de " + valeurCarre.toString() + ".\n" +
                            "Ajoutant " + pointsAnnonces + " Ã  son équipe.");
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(RegleBelote.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RegleBelote.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
*/

    public void setConfirmRebelote(boolean armed) {
        confirmRebelote = armed;
    }
    
}