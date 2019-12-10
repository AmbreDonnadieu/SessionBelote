package belote.joueur;

import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import belote.RegleBelote;
import belote.RegleBeloteInterfaceGraphique;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import cartes.ValeureCarte;
import graphisme.TapisDeBelote;

public class JoueurHumain extends AbstractJoueur {

	TapisDeBelote tapis;
	RegleBeloteInterfaceGraphique graphic_listener;
	
	public JoueurHumain(String nom0, int ordre0) {
		super(nom0, ordre0);
		// TODO Auto-generated constructor stub
	}
	
    PileDeCarte nonTriees;

    public JoueurHumain(RegleBelote regle, String nom, int ordre, TapisDeBelote tapis0, RegleBeloteInterfaceGraphique graphic_listener) {
        super(nom, ordre);
        tapis = tapis0;
        nonTriees = new PileDeCarte();
        this.graphic_listener=graphic_listener;
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
            if ( tapis.playWithMouse) c = tapis.joueCarte(carteEnMain);
            else {
                graphic_listener.unlockRead();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            carteAJouer =
                                    (Carte)JOptionPane.showInputDialog(tapis,
                                            "Choisissez une carte à jouer ",
                                                "Votre choix ?", JOptionPane.QUESTION_MESSAGE,
                                                null, carteEnMain.toArray(), null);
                        }
                    });
                } catch (InterruptedException ex) { }
                catch (InvocationTargetException ex) { }
                tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                graphic_listener.lockRead();
                if ( carteAJouer == null) tapis.playWithMouse = true;
                c = carteAJouer;
            }
        }
        while ( (c == null) && ! tapis.getRegle().getEndOfGame());

        tapis.playWithMouse = ancien;
        carteEnMain.remove(c);
        carteEnMain.sort(gestionnaireCarte);
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
        graphic_listener.unlockRead();
        if ( tapis.playWithMouse) {
            PileDeCarte p = carteEnMain.deCouleur(gestionnaireCarte.getTapis().get(0).getCouleur());
            p.add( gestionnaireCarte.getTapis().get(0));
            Carte c = tapis.joueCarte(p);
            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            return (c!= null);
        }

        else try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    retourDialog = JOptionPane.showConfirmDialog(tapis,
                        ((joueurQuiCommence==moi)?"Vous allez commencer.\n":
                        "Le joueur "+joueurQuiCommence.getNom()+" commencera.\n") +
                        "Voulez-vous prendre " + (atout.getValeur().equals(ValeureCarte.CARD_D) ? "la " :
                            atout.getValeur().equals(ValeureCarte.CARD_AS) ? "l'" : "le ") +
                            atout.getValeur().toLongString() + " de " + atout.getCouleur().toString() +
                            " ?", "Votre choix ?", JOptionPane.YES_NO_OPTION);
                }
            });
        } catch (InterruptedException ex) { } 
          catch (InvocationTargetException ex) { }
        
        graphic_listener.lockRead();
        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        return retourDialog == JOptionPane.YES_OPTION;
    }

    static CouleurCarte retourCouleurDialog;
    
    @Override
    public CouleurCarte getChoixAtout2(CouleurCarte sauf) {

        graphic_listener.unlockRead();
        tapis.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if ( tapis.playWithMouse) {
            Carte c = tapis.joueCarte(carteEnMain.saufCouleur(gestionnaireCarte.getTapis().get(0).getCouleur()));
            graphic_listener.lockRead();
            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            return c==null ? null : c.getCouleur();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        retourCouleurDialog =
                                (CouleurCarte)JOptionPane.showInputDialog(tapis,
                                        ((joueurQuiCommence==moi)?"Vous allez commencer.\n":
                                            "Le joueur "+joueurQuiCommence.getNom()+
                                            " commencera.\n") + "Voulez-vous choisir une autre couleur ?",
                                            "Votre choix ?", JOptionPane.QUESTION_MESSAGE,
                                            null, CouleurCarte.COULEURS, null);
                    }
                });
            } catch (InterruptedException ex) { }
            catch (InvocationTargetException ex) { }
            tapis.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            graphic_listener.lockRead();
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
        if ( carteEnMain.size()==0) nonTriees.clear();
        super.ajoutATaMain(cartes);
        carteEnMain.sort(gestionnaireCarte);
        nonTriees.addAll(cartes);
    }

    @Override
    public PileDeCarte rendTaMain() {
        PileDeCarte p = carteEnMain;
        carteEnMain = nonTriees;
        nonTriees = p;
        nonTriees.clear();
        return super.rendTaMain();
    }

}
