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

public class ValeureCarte implements Comparable<ValeureCarte> {

    int value;
    public static final ValeureCarte CARD_1 = new ValeureCarte(1);
    public static final ValeureCarte CARD_2 = new ValeureCarte(2);
    public static final ValeureCarte CARD_3 = new ValeureCarte(3);
    public static final ValeureCarte CARD_4 = new ValeureCarte(4);
    public static final ValeureCarte CARD_5 = new ValeureCarte(5);
    public static final ValeureCarte CARD_6 = new ValeureCarte(6);
    public static final ValeureCarte CARD_7 = new ValeureCarte(7);
    public static final ValeureCarte CARD_8 = new ValeureCarte(8);
    public static final ValeureCarte CARD_9 = new ValeureCarte(9);
    public static final ValeureCarte CARD_10 = new ValeureCarte(10);
    public static final ValeureCarte CARD_V = new ValeureCarte(11);
    public static final ValeureCarte CARD_C = new ValeureCarte(12);
    public static final ValeureCarte CARD_D = new ValeureCarte(13);
    public static final ValeureCarte CARD_R = new ValeureCarte(14);
    public static final ValeureCarte CARD_AS = new ValeureCarte(15);

    public static final int NCARD_1 = 1;
    public static final int NCARD_2 = 2;
    public static final int NCARD_3 = 3;
    public static final int NCARD_4 = 4;
    public static final int NCARD_5 = 5;
    public static final int NCARD_6 = 6;
    public static final int NCARD_7 = 7;
    public static final int NCARD_8 = 8;
    public static final int NCARD_9 = 9;
    public static final int NCARD_10 = 10;
    public static final int NCARD_V = 11;
    public static final int NCARD_C = 12;
    public static final int NCARD_D = 13;
    public static final int NCARD_R = 14;
    public static final int NCARD_AS = 15;

    /** Ordonnancement des cartes dans un jeu de 32 */
    public static final ValeureCarte[] SET_STD32 = {
        CARD_7, CARD_8, CARD_9, CARD_10, CARD_V, CARD_D, CARD_R, CARD_AS };

    /** Ordonnancement des cartes dans un jeu de belote */
    public static final ValeureCarte[] SET_BELOTE = {
        CARD_7, CARD_8, CARD_9, CARD_V, CARD_D, CARD_R, CARD_10, CARD_AS };

    /** Ordonnancement des cartes d'atout d'un jeu de belote */
    public static final ValeureCarte[] SET_BELOTE_ATOUT = {
        CARD_7, CARD_8, CARD_D, CARD_R, CARD_10, CARD_AS, CARD_9, CARD_V };

    /** Représentation texte de la carte */
    public static final String[] str = { "zéro", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "V", "C", "D", "R", "AS" };

    public static final String[] str_long = { "zéro", "As", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Valet", "Cavalier", "Dame", "Roi", "As" };

    public static ValeureCarte valueOf( String v) {
        switch ( (int)Integer.valueOf(v)) {
            case NCARD_1: return CARD_1;
            case NCARD_2: return CARD_2;
            case NCARD_3: return CARD_3;
            case NCARD_4: return CARD_4;
            case NCARD_5: return CARD_5;
            case NCARD_6: return CARD_6;
            case NCARD_7: return CARD_7;
            case NCARD_8: return CARD_8;
            case NCARD_9: return CARD_9;
            case NCARD_10: return CARD_10;
            case NCARD_V: return CARD_V;
            case NCARD_D: return CARD_D;
            case NCARD_R: return CARD_R;
            case NCARD_AS: return CARD_AS;
            default: return null;
        }
    }
    
    public ValeureCarte( int value0) {
        value = value0;
    }

    public boolean equals(ValeureCarte obj) {
        return value == obj.value;
    }

    @Override
    public int compareTo(ValeureCarte o) {
        if ( value > o.value) return 1;
        else if ( value < o.value ) return -1;
        else return 0;
    }

    public boolean isBetterThanWithOrder(ValeureCarte c, ValeureCarte[] order) {
        for ( ValeureCarte v : order) {
            if ( v.value == value) return false;
            if ( v.value == c.value) return true;
        }
        throw new UnsupportedOperationException("isBetterThanWithOrder: Impossible !!!");
    }

    @Override
    public String toString() {
        return str[ value];
    }

    public String toLongString() {
        return str_long[ value];
    }


    public int toInt() {
        return value;
    }
}
