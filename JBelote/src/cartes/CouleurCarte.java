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

public class CouleurCarte implements Comparable<CouleurCarte> {

    public static final CouleurCarte TREFLE = new CouleurCarte(0);
    public static final CouleurCarte CARREAU = new CouleurCarte(1);
    public static final CouleurCarte COEUR = new CouleurCarte(2);
    public static final CouleurCarte PIQUE = new CouleurCarte(3);
    public static final CouleurCarte[] COULEURS = { TREFLE, CARREAU, COEUR, PIQUE };
    public static final String[] str = { "Trefle", "Carreau", "Coeur",  "Pique" };
    
    int color;
    public static int positionTrie[] = { 0, 1, 3, 2 };

    public static CouleurCarte valueOf( String v) {
        return COULEURS[ Integer.valueOf(v)];
    }

    private CouleurCarte( int col) {
        color = col;
    }

    public boolean equals( CouleurCarte c) {
        return c.color == color;
    }

    @Override
    public int compareTo(CouleurCarte o) {
        if ( positionTrie[color] > positionTrie[o.color]) return 1;
        else if ( positionTrie[color] < positionTrie[o.color] ) return -1;
        else return 0;
    }

    @Override
    public String toString() {
        return str[ color];
    }

    public int toInt() {
        return color;
    }
}
