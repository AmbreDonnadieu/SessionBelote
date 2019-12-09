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



package belote.joueur;

import belote.*;

import cartes.*;
import graphisme.TapisDeBelote;
import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;



public class JoueurHumain extends AbstractJoueur {

	    TapisDeBelote tapis;
	    PileDeCarte nonTriees;

	    public JoueurHumain(AnalyseurDeJeu regle, String nom, int ordre, TapisDeBelote tapis0) {
	        super(nom, ordre);
	        tapis = tapis0;
	        nonTriees = new PileDeCarte();
	    }

	    public void setTapis( TapisDeBelote t) {
	        tapis = t;
	    }

	    static Carte carteAJouer;

	    @Override
	    public Carte joueUneCarte() {
	        boolean ancien = tapis.playWithMouse;
	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        Carte c = null;
	        do {
	            if ( tapis.playWithMouse) c = tapis.joueCarte(main);
	            else {
	                regle.graphic_listener.unlockRead();
	                try {
	                    SwingUtilities.invokeAndWait(new Runnable() {
	                        @Override
	                        public void run() {
	                            carteAJouer =
	                                    (Carte)JOptionPane.showInputDialog(tapis,
	                                            "Choisissez une carte à jouer ",
	                                                "Votre choix ?", JOptionPane.QUESTION_MESSAGE,
	                                                null, main.toArray(), null);
	                        }
	                    });
	                } catch (InterruptedException ex) { }
	                catch (InvocationTargetException ex) { }
	                tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	                regle.graphic_listener.lockRead();
	                if ( carteAJouer == null) tapis.playWithMouse = true;
	                c = carteAJouer;
	            }
	        }
	        while ( (c == null) && ! tapis.getRegle().getEndOfGame());

	        tapis.playWithMouse = ancien;
	        main.remove(c);
	        main.sort(analyseur);
	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	        return c;
	    }


	    /* Utilisé dans getChoixAtout1 */
	    int retourDialog;
	    Carte atout;
	    JoueurHumain moi;

	    @Override
	    public boolean getChoixAtout1(Carte atout0) {
	        
	        atout = atout0;
	        moi = this;

	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        regle.graphic_listener.unlockRead();
	        if ( tapis.playWithMouse) {
	            PileDeCarte p = main.deCouleur(regle.getTapis().get(0).getCouleur());
	            p.add( regle.getTapis().get(0));
	            Carte c = tapis.joueCarte(p);
	            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            return (c!= null);
	        }

	        else try {
	            SwingUtilities.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	                    retourDialog = JOptionPane.showConfirmDialog(tapis,
	                        ((regle.getJoueurQuiCommence()==moi)?"Vous allez commencer.\n":
	                        "Le joueur "+regle.getJoueurQuiCommence().getNom()+" commencera.\n") +
	                        "Voulez-vous prendre " + (atout.getValeur().equals(ValeureCarte.CARD_D) ? "la " :
	                            atout.getValeur().equals(ValeureCarte.CARD_AS) ? "l'" : "le ") +
	                            atout.getValeur().toLongString() + " de " + atout.getCouleur().toString() +
	                            " ?", "Votre choix ?", JOptionPane.YES_NO_OPTION);
	                }
	            });
	        } catch (InterruptedException ex) { } 
	          catch (InvocationTargetException ex) { }
	        
	        regle.graphic_listener.lockRead();
	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	        return retourDialog == JOptionPane.YES_OPTION;
	    }

	    static CouleurCarte retourCouleurDialog;
	    
	    @Override
	    public CouleurCarte getChoixAtout2(CouleurCarte sauf) {

	        regle.graphic_listener.unlockRead();
	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        if ( tapis.playWithMouse) {
	            Carte c = tapis.joueCarte(main.saufCouleur(regle.getTapis().get(0).getCouleur()));
	            regle.graphic_listener.lockRead();
	            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            return c==null ? null : c.getCouleur();
	        } else {
	            try {
	                SwingUtilities.invokeAndWait(new Runnable() {
	                    @Override
	                    public void run() {
	                        retourCouleurDialog =
	                                (CouleurCarte)JOptionPane.showInputDialog(tapis,
	                                        ((regle.getJoueurQuiCommence()==moi)?"Vous allez commencer.\n":
	                                            "Le joueur "+regle.getJoueurQuiCommence().getNom()+
	                                            " commencera.\n") + "Voulez-vous choisir une autre couleur ?",
	                                            "Votre choix ?", JOptionPane.QUESTION_MESSAGE,
	                                            null, CouleurCarte.COULEURS, null);
	                    }
	                });
	            } catch (InterruptedException ex) { }
	            catch (InvocationTargetException ex) { }
	            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            regle.graphic_listener.lockRead();
	            return retourCouleurDialog;

	        }
	    }

	    @Override
	    public int cut() {
	        int i;

	        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        if ( tapis.playWithMouse) {
	            do {
	                i = tapis.getRegle().getJeu().indexOf(tapis.joueCarte(tapis.getRegle().getJeu()));
	            } while ((i < 3) || (i>28));
	            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            return i;
	        } else {
	            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            return super.cut();
	        }
	    }

	    @Override
	    public void ajoutATaMain(PileDeCarte cartes) {
	        if ( main.size()==0) nonTriees.clear();
	        super.ajoutATaMain(cartes);
	        main.sort(analyseur);
	        nonTriees.addAll(cartes);
	    }

	    @Override
	    public PileDeCarte rendTaMain() {
	        PileDeCarte p = main;
	        main = nonTriees;
	        nonTriees = p;
	        nonTriees.clear();
	        return super.rendTaMain();
	    }

	}

	
	
	
	
}
