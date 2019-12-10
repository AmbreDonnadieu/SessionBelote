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
import graphisme.FenetrePartieReseau;


public class JoueurReseauOld extends JoueurBelote {

    FenetrePartieReseau reseau;

    public JoueurReseauOld(String nom0, int ordre0, FenetrePartieReseau reseau0) {
        super(nom0, ordre0);
        reseau = reseau0;
    }

    @Override
    public int cut() {
        String r;
        reseau.getTapis().unlockRead();
        do {
            r = reseau.getActionOf(this, FenetrePartieReseau.T_COUPE);
            if ( r == null) {// Le joueur est parti
                if ( suivant.precedent != this)
                        r = String.valueOf(super.cut());
            }
        } while ( ! regle.getEndOfGame() && (r == null));
        reseau.getTapis().lockRead();
        
        return Integer.valueOf(r.split("\\|")[0]);
    }

    @Override
    public boolean getChoixAtout1(Carte atout) {
        String r;
        reseau.getTapis().unlockRead();
        do {
            r = reseau.getActionOf(this, FenetrePartieReseau.T_ATOUT1);
            if ( r == null) {// Le joueur est parti
                if ( suivant.precedent != this)
                        r = super.getChoixAtout1(atout)?"Y":"N";
            }
        } while ( ! regle.getEndOfGame() && (r == null));
        reseau.getTapis().lockRead();
        return r.equals("Y");
    }

    @Override
    public CouleurCarte getChoixAtout2(CouleurCarte sauf) {
        String r;
        reseau.getTapis().unlockRead();
        r = reseau.getActionOf(this, FenetrePartieReseau.T_ATOUT2);
        if ( r == null) {// Le joueur est parti
            if ( suivant.precedent != this)
                r = String.valueOf(super.getChoixAtout2(sauf));
        }
        reseau.getTapis().lockRead();
        return r.equals("N")?null:CouleurCarte.valueOf(r);
    }

    @Override
    public Carte joueUneCarte() {
        String r;
        Carte c = null;
        reseau.getTapis().unlockRead();
        r = reseau.getActionOf(this, FenetrePartieReseau.T_CARTE);
        if ( r == null) {// Le joueur est parti
            if ( suivant.precedent != this)
                c = super.joueUneCarte();
        }
        reseau.getTapis().lockRead();
        if ( c!= null) return c;
        return getCarte(Integer.valueOf(r.split(",")[0]), Integer.valueOf(r.split(",")[1]));
    }
    
    Carte getCarte( int face, int color) {
        Carte r = null;
        
        for ( Carte c : main)
            if ( (c.getCouleur().toInt()==color) && (c.getValeur().toInt()==face)) 
                r = c;

        main.remove(r);
        return r;
    }

}
