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

import belote.joueur.IJoueurBelote;
import cartes.Carte;
import cartes.CouleurCarte;

/**
 *  Un évènement effectué par un joueur d'une partie de belote,
 *  envoyé par un objet RegleBelote a un objet RegleBeloteListener.
 */
public class BeloteEvent {

    /** Un joueur coupe */
    public static final int EV_COUPE = 1;
    /** Un joueur répond à l'atout 1 */
    public static final int EV_ATOUT1 = 2;
    /** Un joueur répond à l'atout 2 */
    public static final int EV_ATOUT2 = 3;
    /** Un joueur joue une carte */
    public static final int EV_CARTE = 4;
    /** Synchronisation du tour */
    public static final int EV_SYNCHRO = 10;
    /** Fin de partie Réseau */
    public static final int EV_QUIT = 100;

    public int type;
    public int value;
    public Carte card;
    public CouleurCarte color;
    public IJoueurBelote from;

    public BeloteEvent(int type, IJoueurBelote from, int value, CouleurCarte vCouleur, Carte vCarte) {
        this.value = value;
        this.type  = type;
        this.from  = from;
        this.card  = vCarte;
        this.color = vCouleur;
    }

    @Override
    public String toString() {
        switch (type) {
            case EV_COUPE: return from + " COUPE(" + value + ")";
            case EV_ATOUT1: return from + " ATOUT1(" + value + ")";
            case EV_ATOUT2: return from + " ATOUT2(" + color + ")";
            case EV_CARTE: return from + " CARTE(" + card + ")";
            case EV_SYNCHRO: return "SYNCHRO(" + from + ")";
            default: return "type="+type+","+from+","+value+","+color+","+card;
        }
    }
}
