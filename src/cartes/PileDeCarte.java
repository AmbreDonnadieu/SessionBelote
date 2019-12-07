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

package cartes;

import belote.AnalyseurDeJeu;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;


public class PileDeCarte extends ArrayList<Carte> {

    /** Coupe cette pile ne position 'pos' */
    public void coupeA(int pos) {
        int s = size();
        if ( s < 2 ) return;
        if ( pos >= s) return;
        
        for ( int i = 0; i < pos; i++)
            add(remove(0));
    }

    /** Mélange ce jeu */
    public void melange() {
        java.util.Random r = new Random(System.currentTimeMillis());
        for ( int i = 0; i < 200; i++) {
            int d = 2 + r.nextInt(26);
            int n = 2 + r.nextInt(28-d);
            for (int j = 0; j < n; j++) {
                add(remove(d));
            }
            coupeA( 3 + r.nextInt(26));
        }
    }

    /** Renvoie une pile des 'nb' premières cartes de cette pile */
    public PileDeCarte donneDesCartes( int nb) {
        PileDeCarte p = new PileDeCarte();

        while ( (size() > 0) && ( nb > 0) ) {
            p.add( remove(0));
            nb--;
        }
        return p;
    }

    /** Donne la première carte de cette pile */
    public Carte donneUneCarte() {
        PileDeCarte p = new PileDeCarte();

        return remove(0);
    }

    /* Retourne une pile de carte pour jouer à la belote */
    public static PileDeCarte getJeuBelote() {
        PileDeCarte cp = new PileDeCarte();

        for ( CouleurCarte c :  CouleurCarte.COULEURS)
            for ( ValeureCarte v : ValeureCarte.SET_BELOTE)
                cp.add( new Carte(c, v));

        return cp;
    }


    /** Retourne vrai si une caarte de la couleur est dans cette pile */
    public boolean contient(CouleurCarte couleur) {
        for ( Carte c : this)
             if ( c.getCouleur().equals(couleur)) return true;

        return false;
    }

    /** Retourne le nombre de carte de cette couleur dans ce tas */
    public int nombreDe( CouleurCarte couleur) {
        int n = 0;
        for ( Carte c : this)
             if ( c.getCouleur().equals(couleur)) n++;

        return n;
    }

    /** Retourne vrai si il existe une carte plus forte, non jouée, de la même couleur */
    public boolean ilExistePlusForteQue(Carte carte) {
        for ( Carte c : this)
            if ( c.getCouleur().equals(carte.getCouleur())) {
                if ( carte.compareTo(c)<0) return true;
            }

        return true;
    }

    @Override
    public String toString() {
        String s = "[";
        for( Carte c : this)
            s += c.toString() + " , ";

        return s + "]";
    }

    public boolean contient(CouleurCarte couleur, ValeureCarte val) {
        for ( Carte c : this)
            if ( c.getCouleur().equals(couleur) && c.getValeur().equals(val))
                return true;

        return false;
    }

    /** Renvoie la carte graphique contenant ce point */
    public Carte quelleCarteEstALaPosition( int x0, int y0, boolean aPartirDuDebut) {

        if ( aPartirDuDebut) {
            for ( Carte c : this)
                if ( c.getRepresentationGraphique().contains(x0, y0))
                    return c;
        } else {
            for ( int i = this.size()-1; i >= 0; i --)
                if ( this.get(i).getRepresentationGraphique().contains(x0,y0))
                    return this.get(i);
        }

        return null;
    }

    /** Affiche toutes les cartes de cette pile avec 'g' en partant de (x0,y0) */
    public void paint(Graphics g, int x0, int y0, int dx, int dy, boolean turned) {
        int x = x0;
        int y =y0;
        
        for ( Carte c : this) {
            c.getRepresentationGraphique().paint(g, x, y, turned);
            x += dx;
            y += dy;
        }
    }

        // TREFLE, CARREAU, COEUR, PIQUE
    static final int triSansCarreau[] =  { 0, 1, 2, 3 };
    static final int triSansPique[] =  { 1, 3, 0, 2 };
    static final int triNormal[] =  { 0, 1, 3, 2 };
    
    /** Trie cette pile de cartes */
    public void sort( AnalyseurDeJeu a) {
        // Tri à bule
        boolean ok;
        Carte c1, c2;
        if ( ! contient( CouleurCarte.CARREAU) && contient( CouleurCarte.PIQUE) && contient( CouleurCarte.TREFLE)) {
            CouleurCarte.positionTrie = triSansCarreau;
        } else if ( ! contient( CouleurCarte.PIQUE) && contient( CouleurCarte.COEUR) && contient( CouleurCarte.CARREAU)) {
            CouleurCarte.positionTrie = triSansPique;
        } else {
            CouleurCarte.positionTrie = triNormal;
        }

        do {
            ok = true;
            for ( int i = 0; i < size()-1; i++) {
                c1=get(i);
                c2=get(i+1);
                if (((c1.col == c2.col) &&
                     (a.positionDe(c1)>a.positionDe( c2))) ||
                     (c1.col!=c2.col) && (c1.compareTo(c2)>0)) {
                    ok = false;
                    set(i, c2);
                    set(i+1, c1);
                    break;
                }
            }
        } while ( ! ok);
    }

    /** Renvoie une copie des cartes de 'couleur' contenus dans cette pile */
    public PileDeCarte deCouleur(CouleurCarte couleur) {
        PileDeCarte p = new PileDeCarte();

        for ( Carte c : this)
            if ( c.getCouleur().equals(couleur))
                p.add(c);

        return p;
    }

    /** Renvoie une copie des cartes n'étant pas de 'couleur' contenus dans cette pile */
    public PileDeCarte saufCouleur(CouleurCarte couleur) {
        PileDeCarte p = new PileDeCarte();

        for ( Carte c : this)
            if ( ! c.getCouleur().equals(couleur))
                p.add(c);

        return p;
    }

    /** Crée la liste des cartes au format n°couleur,n°valeur séparées par ':' */
    public String encode() {
        String r = "";
        for ( Carte c : this) r+= c.col.toInt() + "," + c.val.toInt() + ":";
        return r;
    }

    /** Décode une pile de cartes au format C1,F1:C2,F2:C3,F3... */
    public static PileDeCarte decode(String r) {
        PileDeCarte p = new PileDeCarte();
        if ( r.charAt(r.length()-1) == '\n') r = r.substring(0, r.length()-1);
        for( String c : r.split(":")) {
            p.add( new Carte(CouleurCarte.valueOf(c.split(",")[0]), ValeureCarte.valueOf(c.split(",")[1])));
        }
        return p;
    }
}
