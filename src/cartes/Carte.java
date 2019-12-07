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

import graphisme.GCarte;

public class Carte implements Comparable<Carte> {

    CouleurCarte col;
    ValeureCarte val;
    GCarte representation;

    public Carte( CouleurCarte c, ValeureCarte v) {

        col = c;
        val = v;
    }

    /** Compare les rangs des cartes */
    @Override
    public int compareTo(Carte c) {

        return (col.compareTo(c.col)==0)?val.compareTo( c.val):col.compareTo(c.col);
    }

    /** Retourne la valeur de la carte */
    public ValeureCarte getValeur() {
        return val;
    }

    /** Retourne la couleur de la carte */
    public CouleurCarte getCouleur() {
        return col;
    }

    public void setRepresentationGraphique(GCarte gCarte) {
        representation = gCarte;
    }

    public GCarte getRepresentationGraphique() {
        return representation;
    }

    @Override
    public String toString() {
        return "" + val + " de " + col;
    }

    public String toLongString() {
        return val.toLongString() + " de " + col;
    }

    public boolean estCouleur(CouleurCarte couleur) {
        return col.equals(couleur);
    }
    
    public boolean estCouleur(Carte c) {
        return col.equals(c.getCouleur());
    }
}
