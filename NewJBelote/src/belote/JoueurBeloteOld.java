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
import java.awt.Component;
import java.awt.Graphics;


public class JoueurBeloteOld {

    /** La règle avec laquel il joue */
    protected RegleBelote regle;
    /** Ce que le joueur à dans sa main */
    protected PileDeCarte main;
    /** Le tas du joueur pour la partie en cours */
    protected PileDeCarte tas;
    public String nom;
    protected JoueurBelote precedent;
    protected JoueurBelote suivant;
    protected AnalyseurDeJeu analyseur;
    /** La position du joueur autour du tapis */
    protected int ordre;
    /** Utilisé par la REgleBelote pour savoir qui à gagné la partie et la manche */
    public int pointsTotaux, nbPerdues, nbPrises, nbCapot, pointsTotaux2;

    @Override
    public JoueurBeloteOld clone() {
        JoueurBelote j = new JoueurBelote(nom, ordre);
        j.regle = regle;
        j.main = main;
        j.tas = tas;
        j.precedent = precedent;
        j.suivant = suivant;
        j.analyseur = analyseur;
        j.pointsTotaux = pointsTotaux;
        j.nbPerdues = nbPerdues;
        j.nbCapot = nbCapot;
        j.nbPrises = nbPrises;
        return j;
    } 

    public JoueurBelote( String nom0, int ordre0 ) {
        nom = nom0;
        ordre = ordre0;
        main = new PileDeCarte();
        tas = new PileDeCarte();
    }

    /** Dit au joueur où il se trouve autour de la table */
    public void setOrdre( int pos) {
        ordre = pos;
    }

    /** Donne les joueurs précédent et suivant */
    public void setEntreLesJoueurs( JoueurBelote precedent0, JoueurBelote suivant0 ) {
        if ( precedent0 != null )  precedent = precedent0;
        if ( suivant0 != null )  suivant = suivant0;
    }

    /** Définit la règle avec laquelle il va jouer */
    public void setRegle( RegleBelote regle0) {
        regle = regle0;
        analyseur = regle.getAnalyseur();
    }

    /** Défini la PileDeCartes qu'il utilisera pour stocker son tas */
    public void setTas( PileDeCarte tas0 ) {  
        tas = tas0;
    }

    /** Renvoie la position du joueur */
    public int getOrdre() {
        return ordre;
    }

    /** Renvoie le joueur suivant */
    public JoueurBelote getSuivant() {
        return suivant;
    }

    /** Renvoie le joueur précédent */
    public JoueurBelote getPrecedent() {
        return precedent;
    }

    /** Renvoie le nom du joueur */
    public String getNom() {
        return nom;
    }

    /** Donne le tas que le joueur a gagné (il ne l'a donc plus) */
    public PileDeCarte rendTonTas() {
        PileDeCarte p = new PileDeCarte();
        while( tas.size() > 0) p.add( tas.remove(0));
        return p;
    }

    /** Donne les cartes qu'il a dans sa main (il ne les a donc plus) */
    public PileDeCarte rendTaMain() {
        PileDeCarte p = main;
        main = new PileDeCarte();
        return p;
    }

    public void ajoutATaMain(PileDeCarte cartes) {
        main.addAll( cartes);
    }

    public void ajoutATaMain(Carte carte) {
        main.add( carte);
    }

    public void ajoutATonTas( PileDeCarte cartes) {
        tas.addAll( cartes);
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

        main.add(carte = regle.getTapis().get(0));

        for ( Carte c : main) {
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
                     (main.nombreDe(c.getCouleur())>1) &&
                     ((!main.contient(c.getCouleur(), ValeureCarte.CARD_AS)) ||
                            (main.nombreDe(couleur)>=3) ))
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
        
        main.remove(carte);
        return force >= 7;
    }

    /** Renvoie la couleur qu'il choisi pour le deuxième tour d'atout (ou null) */
    public CouleurCarte getChoixAtout2( CouleurCarte sauf) {
        for ( CouleurCarte col : CouleurCarte.COULEURS)
            if ((col!= sauf) && getChoixAtout(col, 2))
                return col;

        return null;
    }

    /** Affiche avec 'g' les cartes qu'il a dans la main à la position où elle sont */
    public void dessineMain( Graphics g, int x, int y, int dx, int dy, boolean turned, Component comp) {

        for ( Carte c : main) {
            c.getRepresentationGraphique().paint(g, x, y, turned );
            x += dx;
            y += dy;
        }
    }
    
    @Override
    public String toString() {
        return "JoueurBelote["+nom+"]";

    }

    /** Renvoie le contenu de sa main (mais le garde) */
    public PileDeCarte getMain() {
        return main;
    }

    /** Renvoie un nombre entre 3 et 29 pour couper le jeu. */
    public int cut() {
        return 3 + (int)(Math.random()*29);
    }
    
    /** Renvoie la carte qu'il doit obligatoirement jouer maintenant */
    public Carte joueUneCarte() {

        Carte carte = null;
        CouleurCarte couleurDemandee = regle.getCouleurDemandee();
        CouleurCarte couleurAtout = regle.getCouleurAtout();

        if ( combienOntJoues() == 0 ) { // Premier à jouer ?
            if ( onAPris() ) {
                if ( ilOntDesAtouts() && jAiDeLaCouleur(couleurAtout) ) {

                        carte = donnePlusGrosseCarteA( couleurAtout );
                        if ( ! analyseur.estMaitrePourPlusTard(carte))

                            // Cherche à ne pas se faire manger un gros atout, mais à faire tomber les autres
                            if ((regle.getQuiAPris()==this) &&
                                    (nombreDeCartesNonJoueesPlusForteQue(carte)>0) &&
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
                            if ( analyseur.estMaitreTapis(regle.getCarteJoueePar(suivant.suivant)))
                                carte = donneUneGrosseCarteA(couleurDemandee);
                            else
                                carte = donneUnePetiteCarteA( couleurDemandee);
                    } else { // ! premier, sans la couleur demandée qui n'est pas de l'atout
                        if ( onEstMaitre() ) {

                            if ( ((combienOntJoues()==2) && ! ceJoueurCoupeA(suivant, couleurDemandee) &&
                                    analyseur.estMaitreTapis(regle.getCarteJoueePar(precedent.precedent))) ||
                                    (combienOntJoues()==3))
                                carte = donneLaGrosseDefausse();
                            else
                                if ( onAPris()) carte = donneUneQuiFaitUneCoupe();
                                else
                                    if ( monPartenaireSeraMaitre() && ! ceJoueurCoupeA(suivant, couleurDemandee) &&
                                            (analyseur.nombreDeCartesJoueesA(couleurDemandee)==regle.tapis.nombreDe(couleurDemandee)))
                                        carte = donneLaGrosseDefausse();
                                    else
                                        carte = donneUneQuiFaitUneCoupe(); // Joue la petite défausse
                        } else
                            if ( jAiDeLaCouleur(couleurAtout))
                                if ( regle.getQuiAPris()==this)
                                    carte = donneMeilleurAtoutNonMaitre(); // il faut couper petit si pas sur
                                else
                                    if ( monPartenaireSeraMaitre() && ! ceJoueurCoupeA(suivant, couleurDemandee))
                                        carte = donnePlusGrosseCarteA(couleurAtout);
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
                        if ( regle.getTapis().size() == 2) {
                            carte = donnePlusGrosseCarteA(couleurAtout);
                            if ((!analyseur.estMaitreTapis(carte)) && 
                                    (nombreDeCartesNonJoueesPlusForteQue(carte)>0) &&
                                    (regle.pointsDe(carte)>=14)) {
                                
                                        if ( donneCarteJusteDessous(carte, true) != null) {
                                            carte = donneCarteJusteDessous(carte, true);
                                            if ( analyseur.positionDe(carte)<
                                                    analyseur.positionDe(
                                                        analyseur.meilleurCarteDansA(regle.getTapis(), couleurAtout)))
                                                carte = donneMiniAtout();
                                        } else
                                            carte = donneMiniAtout();
                            }
                        } else {
                            carte = donnePlusGrosseCarteA(couleurAtout);
                            if ( ! analyseur.estMaitreTapis(carte)) carte = donneMiniAtout();
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

        main.remove(carte);
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

    /* Doit retourner une grosse carte qui n'est pas maitre */
    private Carte donnePlusGrosseCarteA(CouleurCarte couleur) {
        Carte meilleur = null;

        for ( Carte c : main)
            if ( c.estCouleur(couleur))
                if (meilleur != null) {
                        if (analyseur.positionDe(meilleur)<analyseur.positionDe(c)) meilleur = c;
                } else
                    meilleur = c;

        return meilleur;
    }

    private Carte donneUneCarteMaitre() {
        Carte meilleur = null;

        for ( Carte c : main)
            if ( ! c.estCouleur(regle.getCouleurAtout()))
                if ( analyseur.estMaitreTapis(c)&& ((meilleur==null)||(regle.pointsDe(meilleur)<regle.pointsDe(c))))
                        meilleur = c;
        
        return meilleur;
    }

    private boolean jAiDeLaCouleur(CouleurCarte color) {
        return main.contient(color);
    }

    private boolean estMaitrePourPlusTard( Carte c) {
        if ( (regle.getCarteJoueePar(regle.joueurQuiPrend) == null) &&
              (!analyseur.nAPlusDAtout[regle.joueurQuiPrend.getOrdre()]) &&
              analyseur.naPlusDe.get(c.getCouleur()).contains(regle.joueurQuiPrend))
                    return false;

        return ! lesToursProchainsSerontPourEux() && analyseur.estMaitrePourPlusTard(c);
    }

    /** Joue la plus grosse carte non maitre */
    private Carte donneLaGrosseDefausse() {
        Carte atout = null, meilleur = null, maitre = null;
        int nbPP1 = 10, nbDessous1 = 10, nbPP2 = -1, nbDessous2 = -1;

        /* pour les cartes de valeurs identiques et non maitres, comparer:
           - le nombre de carte directement en dessous (prendre celle qui en a le plus)
           - sinon le nombre de carte zero (prendre celle qui en à le moins)
         */
        for ( Carte c : main)
            if ( ! c.getCouleur().equals(regle.getCouleurAtout())) {
                if (((analyseur.positionDe(meilleur) < analyseur.positionDe(c)))
                        || ((analyseur.positionDe(meilleur) == analyseur.positionDe(c) &&
                                ((nbJusteDessous(c)<nbDessous1) || (nbPlusPetiteALaCouleur(c)<nbPP1)))))
                    if ( ! estMaitrePourPlusTard(c)) {
                        meilleur = c;
                        nbPP1 = nbPlusPetiteALaCouleur(c);
                        nbDessous1 = nbJusteDessous(c);
                    } else
                        if ( (analyseur.combienIlResteDeCartesNonJoueesA(c.getCouleur())>2))
                            if ((maitre==null) || (analyseur.positionDe(maitre)<analyseur.positionDe(c)) ||
                                    ((analyseur.positionDe(maitre) ==analyseur.positionDe(c)) &&
                                    ((nbJusteDessous(c)>nbDessous2) || (nbPlusPetiteALaCouleur(c)>nbPP2)))) {
                                        maitre = c;
                                        nbPP2 = nbPlusPetiteALaCouleur(c);
                                        nbDessous2 = nbJusteDessous(c);
                            }

            } else
                if ((atout == null) || (analyseur.positionDe(atout) < analyseur.positionDe(c)))
                    if ( ! analyseur.estMaitrePourPlusTard(c))
                        if ( analyseur.positionDe(donneMiniAtout()) <= analyseur.positionDe(c))
                            atout = c;

        // On sauve un bon atout qui risque de se faire zigouiller
        if ((! onAPris()) && (main.nombreDe(regle.getCouleurAtout())==1) &&
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

        for ( Carte c : main)
            if ( c.getCouleur().equals(couleur) && analyseur.estMaitreTapis(c))
                return true;

        return false;
    }

    private Carte donneUneGrosseCarteA(CouleurCarte couleur) {

        Carte meilleur = null;

        for ( Carte c : main)
            if ( c.getCouleur().equals(couleur))
                if ( (meilleur == null) || (analyseur.positionDe(meilleur)<analyseur.positionDe(c)))
                    meilleur = c;

        return meilleur;
    }

    private Carte donneUnePetiteCarteA(CouleurCarte couleur) {
        
        Carte pire = null;

        for ( Carte c : main)
            if ( c.getCouleur().equals(couleur))
                if ( (pire == null) || (analyseur.positionDe(pire)>analyseur.positionDe(c)))
                    pire = c;

        return pire;
    }

    private boolean onEstMaitre() {
        PileDeCarte tapis = regle.getTapis();
        Carte meilleur = analyseur.meilleurCarte(tapis);

        return ((tapis.size()==3) &&
                    (meilleur==tapis.get(1))) ||
                ((tapis.size()==2) && (meilleur==tapis.get(0)) &&
                    (!ceJoueurCoupeA(suivant, tapis.get(0).getCouleur())));
    }

    /** Dit si le joueur coupe à la couleur */
    private boolean ceJoueurCoupeA(JoueurBelote j, CouleurCarte c) {
        return (regle.getCarteJoueePar(j)!=null) && 
                 analyseur.resteCartesNonTombeesAPour(c,j) &&
               ! analyseur.nAPlusDAtout[j.ordre] &&
                 analyseur.naPlusDe.get(c).contains(j);
    }

    private int combienOntJoues() {
        return regle.getTapis().size();
    }

    private Carte donneMiniAtout() {
        Carte ok = null, pire = null;
        Carte mini = analyseur.meilleurCarteDansA(regle.getTapis(), regle.atout);

        for ( Carte c : main)
            if ( c.getCouleur().equals(regle.getCouleurAtout())) {
                if ((pire==null) || (analyseur.positionDe(pire)>analyseur.positionDe(c)))
                    pire = c;

                if ( (analyseur.positionDe(mini)<analyseur.positionDe(c))) {
                    if ((ok==null) || ((analyseur.positionDe(ok)>analyseur.positionDe(c))))
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
                                        if ( ! analyseur.estMaitrePourPlusTard(c)) {
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
                            if ((pire==null) ||( analyseur.positionDe(pire)>analyseur.positionDe(c)))
                                pire = c;
                }
            }

        if ( pire == null)
            pire = c = donneUnePetiteCarteA(regle.getCouleurAtout());

        return pire;
    }

    private Carte donneUneCarteMaitreA(CouleurCarte couleur) {
        Carte meilleur = null;

        for ( Carte c : main) {
            if ( c.getCouleur().equals(couleur) )
                if ( analyseur.estMaitreTapis(c) && ((meilleur==null)||(regle.pointsDe(meilleur)<regle.pointsDe(c))))
                        meilleur = c;
        }
        return meilleur;
    }

    /** Retourne true si la carte jouee par mon partenaire sera maitre */
    private boolean monPartenaireSeraMaitre() {
        Carte c = regle.getCarteJoueePar( suivant.suivant);
        if ( c == null) return false;

        if ( analyseur.estMaitreTapis(c))
            if ( c.getCouleur().equals(regle.getCouleurAtout()))
                return true;
            else
                if ( c.getCouleur().equals(regle.getCouleurDemandee())) {
                    switch ( combienOntJoues()) {
                        case 3: return true;
                        case 2: return onEstMaitre() && (analyseur.estMaitrePourPlusTard(
                                                            regle.getCarteJoueePar(suivant.suivant)) &&
                                                         ! analyseur.nAPlusDAtout[suivant.ordre]);
                        default: return false;
                    }
                } else return false;

        return false;
    }

    /** Affiche avec 'g' les plis qu'il a fait en position (x,y)
     *  avec un interval (dx,dy) entre chaque cartes */
    public void dessinePlis(Graphics g, int x, int y, int dx, int dy, Component comp) {
        for ( Carte c : tas) {
            c.getRepresentationGraphique().paint(g, x, y, false);
            x += dx;
            y += dy;
        }
    }


    /** Affiche la pile avec un espace toutes les 4 cartes */
    public void dessinePlis2(Graphics g, int x, int y, int dx, int dy, Component comp) {
        int n = 0;
        for ( Carte c : tas) {
            c.getRepresentationGraphique().paint(g, x, y, false);
            x += dx;
            y += dy;
            n++;
            if ( (n % 4) == 0) { x += dx; y += dy; }
        }

    }

    /** Renvoie la plus petite carte des cartes juste au Dessous 'carte' qui se suivent */
    private Carte donneCarteJusteDessous(Carte carte, boolean auMax) {
        Carte meilleur = null;
        boolean recommence;

        // Cherche d'abord la carte juste au dessous
        for ( Carte c : main) if ( c.estCouleur(carte) && ! c.equals(carte))
            if (analyseur.positionDe(carte)==(analyseur.positionDe(c)+1))
                    meilleur = c;

        if ( auMax && (meilleur!=null))
            do {
                recommence = false;
                // Recherche la carte juste avant carte
                for ( Carte c : main)
                    if ( c.estCouleur(carte) && ! c.equals(carte))
                        if (analyseur.positionDe(meilleur)==(analyseur.positionDe(c)+1)) {
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
        for ( Carte c : main) if ( c.estCouleur(carte) && ! c.equals(carte))
            if (analyseur.positionDe(carte)==(analyseur.positionDe(c)-1))
                    meilleur = c;

        if ( auMax && (meilleur!=null))
            do {
                recommence = false;
                // Recherche la carte juste avant carte
                for ( Carte c : main)
                    if ( c.estCouleur(carte) && ! c.equals(carte))
                        if (analyseur.positionDe(meilleur)==(analyseur.positionDe(c)-1)) {
                            meilleur = c;
                            recommence = true;
                            break;
                        }
            } while ( recommence);

        return meilleur;
    }


    public void setNom(String nom) {
        this.nom = nom;
    }

    /** Retourne VRAI si seulement un des deux adversaires possède autant
     *  d'atout que le nombre de tours restants */
    private boolean lesToursProchainsSerontPourEux() {
        return ((! analyseur.nAPlusDAtout[suivant.ordre])
                ^ (! analyseur.nAPlusDAtout[precedent.ordre]))
            &&
               ((main.size()-1) <= (8-analyseur.nombreJouees[regle.atout.toInt()]));
    }

    /** Renvoie une carte d'atout non maitre */
    private Carte donneMeilleurAtoutNonMaitre( ) {
        Carte meilleur, ok = donnePlusGrosseCarteA(regle.getCouleurAtout());
        boolean recommence;

        if ( donneUneCarteMaitreA(regle.atout) != null)
            return donneMiniAtout();

        if ( (ok!=null) && ! analyseur.estMaitrePourPlusTard(ok)) return ok;

        do {
            meilleur = null;
            recommence = false;
            for ( Carte c : main.deCouleur(regle.getCouleurAtout()))
                if ( (c == donneCarteJusteDessous(ok, false) && regle.ilPeutJouerCetteCarte(this, c))) {
                    ok = c;
                    recommence = true;
                    break;
                } else
                    if ((analyseur.positionDe(c)>analyseur.positionDe(meilleur)) &&
                        (analyseur.positionDe(c)<analyseur.positionDe(ok)))
                        meilleur = c;
            
        } while ( recommence);

        if ( meilleur != null) return meilleur;
        else return ok;
    }

    private int nombreDeCartesNonJoueesPlusForteQue(Carte carte) {

        int n = 0;
        for ( Carte c : analyseur.cartesNonJouees)
            if ( c.estCouleur(carte))
                if ( analyseur.positionDe(c)>analyseur.positionDe(carte))
                    n++;

        return n;
    }


    private Carte donneUneQuiFaitUneCoupe() {
        Carte c, c2;
        int nbDe = 10;
        CouleurCarte atout = regle.getCouleurAtout();

        // Cherche une carte non maitre unique à la couleur
        for ( CouleurCarte col : CouleurCarte.COULEURS) {

            if ( ! col.equals(atout) && (main.nombreDe(col)>0) && (main.nombreDe(col)<nbDe)) {
                    c = donneUnePetiteCarteA(col);
                    c2 = donneUneGrosseCarteA(col);
                    if ( ! analyseur.estMaitrePourPlusTard(c) && (regle.pointsDe(c)<10) &&
                            (analyseur.nombreRestantePlusForteQue(c2) >1) || (main.nombreDe(col)>2))
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
        
        for ( Carte c : main.deCouleur(carte.getCouleur()))
            if ( regle.pointsDe(c) < regle.pointsDe(carte)) n++;

        return n;
    }

    /** Renvoie le nombre de cartes ayant une valeur <= 2 pour la couleur */
    private int nbPetiteALaCouleur(Carte carte) {
        int n = 0;
        for ( Carte c : main.deCouleur(carte.getCouleur()))
            if ( regle.pointsDe(c) <= 2) n++;

        return n;

    }

    private Carte donneUneCarteQuiFaitMaitre() {
        int nbJouees = 10, point = 20;
        Carte carte = null;

        for ( Carte c : main)
            if ( ! c.getCouleur().equals(regle.getCouleurAtout()) &&
                 (analyseur.nombreRestantePlusForteQue(c)==1) &&
                 (main.nombreDe(c.getCouleur())>1) && ((carte == null) ||
                 (regle.pointsDe(donneUnePetiteCarteA(c.getCouleur())) < point) ||
                 (nbJouees > analyseur.nombreDeCartesJoueesA(c.getCouleur())))) {

                nbJouees = analyseur.nombreDeCartesJoueesA(c.getCouleur());
                point = regle.pointsDe(carte = donneUnePetiteCarteA(c.getCouleur()));
            }

        return carte;
    }

    private Carte donneUneQuiPrendUneGrosseDAtout() {
        if ( ilOntDesAtouts() ) {
            CouleurCarte atout = regle.getCouleurAtout();
            Carte meilleur = donnePlusGrosseCarteA(atout);
            if ( meilleur == null) return null;

            Carte aPrendre = null;
            int nombre = 0;
            for ( Carte c : analyseur.cartesNonJouees)
                if ( c.estCouleur(atout) && ! main.contains(c) &&
                     (analyseur.positionDe(c)<analyseur.positionDe(meilleur)) &&
                     (regle.pointsDe(c) >= 10)) {

                    aPrendre = c;
                    nombre++;
                }

            if ( nombre == 1) return meilleur;
        }
        return null;
    }

    public int getNbPlis() {
        return (int) ((tas.size() +suivant.suivant.tas.size())/4);
    }

}
