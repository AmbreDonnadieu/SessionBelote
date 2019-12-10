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

import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Analyse et compte les cartes, réponds aux questions des joueurs IA sans tricher.
 * @author Clément
 */
public class AnalyseurDeJeuOld {

    RegleBelote regle;

    /* Les cartes jouées dans la partie */
    PileDeCarte carteJouees, cartesNonJouees, copieJeuDeCarte;

    /* Nombre de cartes jouées par couleur */
    int[] nombreJouees;

    /* Liste des joueurs qui coupent à la couleur */
    HashMap<CouleurCarte,ArrayList<JoueurBelote>> naPlusDe, aCouleur;

    /* Liste des joueurs qui n'ont plus d'atout */
    boolean[] nAPlusDAtout;

    public AnalyseurDeJeuOld( RegleBelote regle0) {
        regle = regle0;

        copieJeuDeCarte = (PileDeCarte)regle.jeu.clone();
        cartesNonJouees = (PileDeCarte)regle.jeu.clone();
        
        nombreJouees = new int[4];
        nAPlusDAtout = new boolean[4];
        naPlusDe = new HashMap<CouleurCarte, ArrayList<JoueurBelote>>();
        for( CouleurCarte c : CouleurCarte.COULEURS)
            naPlusDe.put(c, new ArrayList<JoueurBelote>());

        carteJouees = new PileDeCarte();
    }


    public boolean mesAdversairesOntEncoreDu(CouleurCarte couleur, JoueurBelote moi) {
        if ( couleur == regle.atout)
            return ! (nAPlusDAtout[moi.suivant.ordre] && nAPlusDAtout[moi.precedent.ordre]);
        else
            return ! (naPlusDe.get(couleur).contains(moi.suivant) || naPlusDe.get(couleur).contains(moi.precedent));
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
    public boolean estMaitreTapis(Carte carte) {

        if ( carte == null) return false;

        if ( ! estMeilleurQue(carte, regle.tapis)) return false;

        if ( regle.tapis.size() == 3) return true; // c'est le dernier à jouer
        
        int pos = positionDe(carte);
        int nb_cartesPlusFortes = 8 - pos;

        for ( Carte c : carteJouees)
            if ( c.estCouleur(carte))
                if ( positionDe(c)>pos)
                    nb_cartesPlusFortes--;

        return nb_cartesPlusFortes == 0;
    }

    /** Renvoie la meilleur carte du plis en tenant compte de l'atout */
    public Carte meilleurCarte( PileDeCarte pile) {
        Carte meilleur = null;

        for ( Carte c : pile)
            if (meilleur != null) {
                if (meilleur.getCouleur().equals(c.getCouleur())) {
                    if (positionDe(c)>positionDe(meilleur)) meilleur = c;
                } else
                    if ( c.getCouleur().equals(regle.atout))
                        meilleur = c;
                    else if ( meilleur.getCouleur() != regle.atout)
                            if ( c.getCouleur().equals(regle.getCouleurDemandee()))
                                meilleur = c;

            } else meilleur = c;

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
            nombreJouees[i] = 0;
            nAPlusDAtout[i] = false;
        }
        
        for( ArrayList<JoueurBelote> l : naPlusDe.values()) 
            l.clear();
            
        carteJouees.clear();
        cartesNonJouees = (PileDeCarte)copieJeuDeCarte.clone();
    }

    /** Renvoie la position (mini 1) de la carte dans la couleur
     *  en tenant compte de celle d'atout */
    public int positionDe(Carte c) {

        if ( c == null) return -1;
        
        ValeureCarte[] set = estAtout(c) ? ValeureCarte.SET_BELOTE_ATOUT
                                                : ValeureCarte.SET_BELOTE;
        int i = 1;
        while ( ! set[i-1].equals(c.getValeur())) i++;

        return i;
    }

    public boolean estAtout( Carte c) {
        if( regle.atout != null)
            return c.getCouleur().equals(regle.atout);
        else
            return false;
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
        return meilleurCarteEntre(carte, m) == carte;
    }

    Carte meilleurCarteEntre( Carte c1, Carte c2) {
        if ( c1 == null) return c2;
        if ( c2 == null) return c1;

        if ( c1.getCouleur()==c2.getCouleur())
            if ( positionDe(c1)>= positionDe(c2)) return c1;
            else return c2;
        else
            if ( c1.getCouleur().equals(regle.getCouleurAtout()) ||
                 (!c2.getCouleur().equals(regle.getCouleurAtout()) &&
                   c1.getCouleur().equals(regle.getCouleurDemandee()))) return c1;
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
