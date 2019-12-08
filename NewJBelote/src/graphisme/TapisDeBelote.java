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

package graphisme;


import belote.JoueurHumain;
import belote.RegleBelote;
import belote.RegleBeloteInterfaceGraphique;
import cartes.Carte;
import cartes.CouleurCarte;
import cartes.PileDeCarte;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Représeante graphiquement les joueurs et les cartes sur un tapis
 * @author Clément
 */
public class TapisDeBelote extends JPanel implements RegleBeloteInterfaceGraphique, MouseListener, MouseMotionListener {

    RegleBelote regle;
    private Thread partieEnCours;
    private Carte carteSelectionnee;
    private boolean demandeUneNouvelleCarte;
    public Image imgCartes;
    Image[] imgCouleurs = { null, null, null, null };
    java.awt.Font font;

    /** Couleur utilisée pour dessiner le tapis */
    private final Color couleurFondTapis;
    private JLabel labelEtat, labelDistribution;
    private PileDeCarte pileDansLaquelleChoisir;
    public  boolean afficheCarteGagnante, waitClick = false;
    public  boolean playWithMouse;
    private JMenuItem jMenuItemNouveau;

    public Cursor CURSOR_HAND_UP, CURSOR_HAND_DOWN;
    private boolean handup, choixOK;
    public int cardsW;
    public int cardsH;

    public TapisDeBelote( ) {

        labelEtat = null;
        setMinimumSize(new Dimension(320, 200));
        setPreferredSize(new Dimension(720, 550));
        addMouseListener(this);
        addMouseMotionListener(this);
        font = Font.decode("Courrier-BOLD-15");

        java.net.URL url_image;

        url_image = FenetreDeJeuDeBelote.class.getResource("/images/hand-up.png");
        Image img = null;
        try {
            img = ImageIO.read(url_image); // java.awt.Toolkit.getDefaultToolkit().getImage(url_image);
        } catch (IOException ex) {
            Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
        }
        CURSOR_HAND_UP = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(25,25), "HAND_UP");

        url_image = FenetreDeJeuDeBelote.class.getResource("/images/hand-down.png");
        try {
            img = ImageIO.read(url_image); //java.awt.Toolkit.getDefaultToolkit().getImage(url_image);
        } catch (IOException ex) {
            Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
        }
        CURSOR_HAND_DOWN = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(25,25), "HAND_DOWN");

        if ( imgCartes == null ) setTailleCartes(2);
  /*      url_image = FenetreDeJeuDeBelote.class.getResource("/images/jeu2.png");
        try {
            imgCartes = ImageIO.read(url_image);
        } catch (IOException ex) {
            Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
        }
      /*  imgCartes = java.awt.Toolkit.getDefaultToolkit().getImage(url_image);
                    imgCartes.flush();
        imgCartes.getHeight(this);
*/
        
        for ( int i = 0; i < 4; i++) {
            url_image = FenetreDeJeuDeBelote.class.getResource("/images/icon-"+CouleurCarte.str[i]+".png");
            try {
                imgCouleurs[i] = ImageIO.read(url_image); //java.awt.Toolkit.getDefaultToolkit().getImage(url_image);
            } catch (IOException ex) {
                Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        couleurFondTapis = new Color(0, 128, 0);

        /* Ordinateur Seul 
        regle = new RegleBelote( construitNouveauJeu(), null);
        /* */
        /* Avec un humain */
        regle = new RegleBelote( decoreJeuDeCarte(PileDeCarte.getJeuBelote()),
                    new JoueurHumain(null, "SUD", RegleBelote.JOUEUR_SUD, this));
        /* */
        
        regle.setGraphicListener(this);
    }

    boolean imageOK = false;

    public void setTailleCartes(int taille) {
        if ( (taille<1) || (taille>4)) return;

        java.net.URL url_image = FenetreDeJeuDeBelote.class.getResource("/images/jeu.png");
        try {
            imgCartes = ImageIO.read(url_image);
        } catch (IOException ex) {
            Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
        }

        double ratio = (double)(imgCartes.getWidth(null)/13)/(double)(imgCartes.getHeight(null)/5);
     //   if ( ! imageOK) {
        switch(taille) {
            case 1: GCarte.gWidth = (int)(ratio * (double)(GCarte.gHeight = 100)); break;
            case 3: GCarte.gWidth = (int)(ratio * (double)(GCarte.gHeight = 145)); break;
            case 4: GCarte.gWidth = (int)(ratio * (double)(GCarte.gHeight = 170)); break;

            default:
                GCarte.gHeight = imgCartes.getHeight(null)/5;
                GCarte.gWidth = imgCartes.getWidth(null)/13;
        }

        cardsW = GCarte.gWidth;
        cardsH = GCarte.gHeight;
        /*
          //      if ( imagesChargees)
                    for ( Carte carte : regle.getCopieJeu() ) {
                        GCarte gc = carte.getRepresentationGraphique();
                        gc.imgCartes = imgCartes;
                        gc.width = cardsW;
                        gc.height = cardsH;
                    }
*/
//
    //           imageOK = true;
  //         }
     //   imageOK = false;
        repaint();
    }

    public void paintChoosedColor( Graphics g, int x, int y, int color) {
        Graphics2D g2 = (Graphics2D) g;
        
        RenderingHints hints = new RenderingHints(
                                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        ((Graphics2D)g2).setRenderingHints(hints);
        
        Composite comp = g2.getComposite();
        AlphaComposite rule = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        
        g2.setComposite(rule);
        Image img = imgCouleurs[color];
        g2.setColor(Color.gray);
        g2.drawArc( x+1, y-1,
                img.getWidth(this), img.getHeight(this) + 4, 0, 360);
        g2.setColor(Color.gray);
        g2.drawArc( x, y-2,
                img.getWidth(this), img.getHeight(this) + 4, 0, 360);
        g2.setColor(Color.white);
        g2.fillOval(x, y -2,
                img.getWidth(this), img.getHeight(this) + 4);
        g2.drawImage(img, x, y +((CouleurCarte.COEUR.toInt()==color)?5:0), null);
        g2.setComposite(comp);
    }
    
    @Override
    protected void paintComponent(Graphics g) {

            while ( (partieEnCours!=null) && (partieVerrouillee>0))
                try {
                    Thread.sleep(50);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() { repaint(); }});
                    return;
                }
                catch (Exception e) { }

            peutVerouillerPartie = false;

            g.setColor( couleurFondTapis);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(font);
            
            if ( regle.getQuiAPris() != null) {
                g.setColor( Color.BLACK);
                g.drawString( regle.getQuiAPris().nom + " a pris à " + regle.getCouleurAtout() + " (" +
                              regle.getQuiAPris().getNbPlis() + "|" + regle.getQuiAPris().getSuivant().getNbPlis() + ')', 5, 20);
            }

            // Affiche les cartes des joueurs
            belote.JoueurBelote j = regle.getJoueurQuiDistribue().getSuivant();
            if ( (regle.getEtatDuJeu() != RegleBelote.ETAT_RIEN) &&
                 ( regle.getEtatDuJeu() != RegleBelote.ETAT_FINPARTIE))
                for ( int i = 0; i < 4; i++) {
                    int nbc = j.getMain().size();

                        int BORDER = 30, dx, dy;
                        if ( nbc>0) {
                            dx = (getWidth() - 2 * (cardsW+2*BORDER)) / nbc;
                            dy = (getHeight() - 2 * (cardsH+2*BORDER)) / nbc;
                        } else
                            dx = dy = 3;

                        if ( dx > cardsW ) dx = cardsW + 3;
                        if ( dy > cardsH) dy = cardsH + 3;
                        int ox = BORDER+(getWidth()-(cardsW + dx*(nbc)))/2;
                        int oy = (getHeight()-(cardsH + dy*(nbc)))/2;

                        g.setColor(Color.BLACK);
                        switch ( j.getOrdre()) {
                            case RegleBelote.JOUEUR_NORD :
                                    g.drawString( j.nom, getWidth()/2, BORDER);
                                    if (( j == regle.getQuiAPris()) && ( regle.getCouleurAtout() != null))
                                        if ( regle.getCouleurAtout() != null)
                                            paintChoosedColor(g, ox + cardsW, BORDER + cardsH, regle.getCouleurAtout().toInt() );
                                    
                                    j.dessineMain(g, ox, BORDER, dx, 3, (!(j instanceof JoueurHumain)) && cartesTournees, this);
                                    break;
                            case RegleBelote.JOUEUR_EST :
                                    g.drawString( j.nom, getWidth() - cardsW - BORDER, oy - 10);
                                    if (( j == regle.getQuiAPris())&& ( regle.getCouleurAtout() != null))
                                            paintChoosedColor(g, getWidth() - cardsW * 2, oy + cardsH, regle.getCouleurAtout().toInt() );
                                    
                                    j.dessineMain(g, getWidth() - cardsW - BORDER, oy, 3, dy,  (!(j instanceof JoueurHumain)) && cartesTournees, this);
                                    break;
                            case RegleBelote.JOUEUR_SUD :
                                    if ( (j == regle.getQuiAPris()) && ( regle.getCouleurAtout() != null)) {
                                            Image img = imgCouleurs[regle.getCouleurAtout().toInt()];
                                            paintChoosedColor(g, ox + cardsW,getHeight() -BORDER - cardsH - img.getHeight(this), regle.getCouleurAtout().toInt() );
                                    }
                                    j.dessineMain(g, ox, getHeight() - cardsH - BORDER, dx, 3,  (!(j instanceof JoueurHumain)) && cartesTournees, this);
                                    break;
                            case RegleBelote.JOUEUR_OUEST :
                                    g.drawString( j.nom, 5, oy - 10);
                                    if ( (j == regle.getQuiAPris()) && ( regle.getCouleurAtout() != null))
                                            paintChoosedColor(g, cardsW + BORDER, oy + cardsH, regle.getCouleurAtout().toInt() );
                                    
                                    j.dessineMain(g, BORDER/2, oy, 3, dy,  (!(j instanceof JoueurHumain)) && cartesTournees, this);
                                    break;
                        }
                    j = j.getSuivant();
                }

            int ox = getWidth() / 2;
            int oy = getHeight() / 2;
            // affiche le tas du milieu ?
            if ( regle.getTapis().size()>0) {
                //GCarte c;// = regle.getTapis().get(0).getRepresentationGraphique();

                if ( regle.getCouleurAtout() != null) {
                    Image img = imgCouleurs[regle.getCouleurDemandee().toInt()];
                    int dx1=ox-(int)(img.getWidth(this)/4), dy1=oy-(int)(img.getHeight(this)/4);
                    g.drawImage(img, dx1, dy1, dx1+(int)(img.getWidth(this)/2), dy1+(int)(img.getHeight(this)/2),
                            0, 0, img.getWidth(this), img.getHeight(this),  this);
                } else {
                    g.setColor( new Color(100, 255, 100));
                    if ( regle.getJoueurCourrant() instanceof JoueurHumain ) {
                        if ( regle.getEtatDuJeu() == RegleBelote.ETAT_ATOUT2)
                            g.drawString("??", ox-15, oy+5);
                        else g.drawString("?", ox-5, oy+5);
                    }
                }

                j = regle.getJoueurQuiCommence();
                for ( int i = 0; i < regle.getTapis().size(); i++) {
                    GCarte c = regle.getTapis().get(i).getRepresentationGraphique();
                    boolean win = (afficheCarteGagnante||((!regle.confirmPlis)&&(regle.getTapis().size()==4))) &&
                                    ((regle.getEtatDuJeu() == RegleBelote.ETAT_FINTOUR) || (regle.getEtatDuJeu() == RegleBelote.ETAT_JOUE)) &&
                                    (c.carte == regle.getAnalyseur().meilleurCarte(regle.getTapis()));

                    switch ( j.getOrdre()) {
                        case RegleBelote.JOUEUR_NORD:
                                if ( win) {
                                    g.setColor(Color.YELLOW);
                                    g.drawRoundRect(ox-cardsW/2-5, oy-(int)(cardsH*1.25)-5, cardsW + 10, cardsH + 10, 5, 5);
                                }
                                c.paint( g, ox-cardsW/2, oy-(int)(cardsH*1.25), false);
                                break;
                        case RegleBelote.JOUEUR_EST:
                                c.paint( g, ox+cardsW/3, oy-cardsH/2, false);
                                if ( win) {
                                    g.setColor(Color.YELLOW);
                                    g.drawRoundRect(ox+cardsW/3-5, oy-cardsH/2-5, cardsW + 10, cardsH + 10, 5, 5);
                                }
                                break;
                        case RegleBelote.JOUEUR_SUD:
                                c.paint( g, ox-cardsW/2, oy+(int)(cardsH*0.25), false);
                                if ( win) {
                                    g.setColor(Color.YELLOW);
                                    g.drawRoundRect(ox-cardsW/2-5, oy+(int)(cardsH*0.25)-5,cardsW + 10, cardsH + 10, 5, 5);
                                }
                                break;
                        case RegleBelote.JOUEUR_OUEST:
                                c.paint( g, ox-(int)(cardsW*1.33), oy-cardsH/2, false);
                               if ( win) {
                                    g.setColor(Color.YELLOW);
                                    g.drawRoundRect(ox-(int)(cardsW*1.33)-5, oy-cardsH/2-5,cardsW + 10, cardsH + 10, 5, 5);
                               }
                               break;
                    }
                    j = j.getSuivant();
                }
            }

            // Affiche les plis en fin de partie ?
            if ( (regle.getEtatDuJeu() == RegleBelote.ETAT_FINPARTIE) &&
                    (regle.getQuiAPris()!=null) ) {
                regle.getQuiAPris().dessinePlis2(g, 10, 30, 25, 3, this);
                regle.getQuiAPris().getSuivant().getSuivant().dessinePlis2(g, 10, (int)(cardsH*0.75), 25, 3, this);

                regle.getQuiAPris().getSuivant().dessinePlis2(g, 10, getHeight()/2, 25, 3, this);
                regle.getQuiAPris().getPrecedent().dessinePlis2(g, 10, getHeight()/2 + (int)(cardsH*0.75), 25, 3, this);
            }

            // Affiche le jeu de cartes pour couper ou pour faire beau
            if ( (regle.getEtatDuJeu() == RegleBelote.ETAT_COUPEJEU) ||
                 (regle.getEtatDuJeu() == RegleBelote.ETAT_RIEN)) {

                regle.getJeu().paint( g, 10, 10, (int)((getWidth()-cardsW)/33), (int)((getHeight()-cardsH)/33),
                                        cartesTournees);
            }

        peutVerouillerPartie = true;
    }


    /** Attend que l'utilisateur clique avec la souris sur le tapis */
    @Override
    public void waitClick() {
        Cursor c = getCursor();
        waitClick = true;
        while ( waitClick && (partieEnCours!=null)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            try { Thread.sleep(50); } catch (InterruptedException ex) { }
        }
        setCursor(c);
    }

    /** Termine sans confirmation la partie en cours */
    @Override
    public void endOfGame() {

        partieVerrouillee = -5;
        demandeUneNouvelleCarte = false;
        partieVerrouillee = 0;
        partieEnCours = null;
        jMenuItemNouveau.setText("Nouvelle partie ...");
        repaint();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /** Demande la fin de la partie (locale) en cours */
    @Override
    public boolean verifyEndOfGame() {
        if ( partieEnCours != null ) {
            int res = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment abandonner cette partie ?", "Fin de la partie ?", JOptionPane.YES_NO_OPTION);
            if ( res == JOptionPane.YES_OPTION) {
                demandeUneNouvelleCarte = false;
                regle.demandeLaFinDeLaPartie(false);
                return true;
            }
            else return false;
        }
        return true;
    }

    /** Commence une nouvelle partie (éventuellement demande la fin de la partie courante) */
    public boolean nouvellePartie( RegleBelote avec_cette_regle) {

        if ( ! verifyEndOfGame()) return false;

        if ( avec_cette_regle != null ) {
            regle = avec_cette_regle;
        } else {
            PileDeCarte p = decoreJeuDeCarte(PileDeCarte.getJeuBelote());
            p.melange();
            regle = new RegleBelote( p,
                            new JoueurHumain(null, "SUD", RegleBelote.JOUEUR_SUD, this));
        }
        regle.setGraphicListener(this);

        if ( avec_cette_regle == null) {
            int i = 0;
            do {
                String res = (String)JOptionPane.showInputDialog(this, "Manche en combien de points ?\n(162 points mini)\n", "1000");
                if ( res == null) return false;

                try {
                    i = Integer.valueOf(res);
                } catch ( Exception ex) { }

            } while ( i < 162);
            RegleBelote.POINTS_MANCHE = i;
        } else
            RegleBelote.POINTS_MANCHE = 1000;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        partieEnCours = new Thread( regle);
        partieEnCours.start();
        partieVerrouillee = 0;
        jMenuItemNouveau.setText("Arrêter la partie");
        return true;
    }

    /** Décore les carte de la pile p pour qu'il puisse être affiché avec ce tapis */
    public PileDeCarte decoreJeuDeCarte( PileDeCarte p) {

        int W = imgCartes.getWidth(null)/13;
        int H = imgCartes.getHeight(null)/5;

        for ( Carte c : p)
            c.setRepresentationGraphique( new GCarte( this, c, imgCartes, W, H));

        return p;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if ( demandeUneNouvelleCarte ) {
            carteSelectionnee = pileDansLaquelleChoisir.quelleCarteEstALaPosition(e.getX(), e.getY(), false);
            demandeUneNouvelleCarte = false;
        }
        waitClick = false;
    }

    @Override
    public void mousePressed(MouseEvent e) { mouseMoved(e); }

    @Override
    public void mouseReleased(MouseEvent e) { mouseClicked(e); }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) { mouseMoved(e); }

    @Override
    public void mouseMoved(MouseEvent e) {
        if ( demandeUneNouvelleCarte ) {
            Carte c = pileDansLaquelleChoisir.quelleCarteEstALaPosition(e.getX(), e.getY(), false);
            if ( c != null) {
                if ( !handup || ! choixOK) {
                    setCursor(CURSOR_HAND_UP);
                    handup = true;
                }
            } else {
                if ( handup || ! choixOK) {
                    setCursor(CURSOR_HAND_DOWN);
                    handup = false;
                }
            }
        }
    }

    /** Spécifie le JLabel qui doit afficher l'état de la partie en cours */
    public void setLabelEtat(JLabel jLabelEtat) {
        labelEtat = jLabelEtat;
    }

    /** Spécifie le label qui doit afficher le joueur qui distribue */
    public void setLabelDistribution( JLabel jlabelDistribution) {
        labelDistribution = jlabelDistribution;
    }


    @Override
    public Component getComponent() {
        return this;
    }

    /** Demande à l'utilisateur de choisir une carte parmi celles de la 'pile' */
    public Carte joueCarte( PileDeCarte pile) {
        Carte c;

        handup = false;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        pileDansLaquelleChoisir = pile;
        demandeUneNouvelleCarte = true;
        regle.getGraphicListener().unlockRead();
        while ( demandeUneNouvelleCarte)
            try {
            Thread.sleep(100);
        } catch (InterruptedException ex) { }

        c = carteSelectionnee;
        carteSelectionnee = null;
        regle.getGraphicListener().lockRead();

        return c;
    }

    /** Mise à jour du nouvel état de la partie */
    @Override
    public void setStatus(String status) {

        if ( ! regle.getEndOfGame() )
            labelDistribution.setText("Distribution: " + regle.getQuiDistribue());
        else
            labelDistribution.setText("");
        labelEtat.setText(status);
        repaint();
    }

    /** Termmine proprement la parte */
    void dispose() {
        if ( partieEnCours != null )
            while ( partieEnCours.isAlive()) regle.demandeLaFinDeLaPartie(false);
    }

    /** Autorise la lecture des données de la partie */
    @Override
    public void unlockRead() {
        partieVerrouillee--;
    }

    /** Interdit la lecture des données de la partie (bloque le rafraichissement de ce composant) */
    @Override
    public void lockRead() {
        if ( partieVerrouillee <= 0)
            while ( ! peutVerouillerPartie) try {
                Thread.sleep(10);
            } catch (InterruptedException ex) { }
        
        partieVerrouillee++;
    }

    private int partieVerrouillee = 0;
    private boolean peutVerouillerPartie = true;
    private boolean cartesTournees = true;

    /** Pour afficher obligatoirement les faces de toutes les cartes de la partie */
    void setCartesTournees(boolean selected) {
        cartesTournees = selected;
        repaint();
    }

    /** Pour entourer la carte gagnante dans le pli en cours */
    void setAfficheCarteGagnante(boolean selected) {
        afficheCarteGagnante = selected;
        repaint();
    }


    /*@Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {

    System.err.println("imageUpdate: " + img + " " + infoflags);

    if ( (img == imgCartes) && ((infoflags & ImageObserver.ALLBITS)!=0)) {
    try {
    Thread.sleep(100);
    } catch (InterruptedException ex) {
    Logger.getLogger(TapisDeBelote.class.getName()).log(Level.SEVERE, null, ex);
    }
    return true;
    }
    return true;

    }*/

    /** Pour jouer avec la souris ou par l'intermédiaire de fenêtres de dialogues */
    void setPlayWithMouse(boolean selected) {
        playWithMouse = selected;
    }

    /** Spécifie au tapis quel JMenuItem il devra modifier pour qu'il corresponde
     * au fait qu'il y ait une partie en cours ou non */
    void setJMenuItemStart(JMenuItem jMenuItemNouveau0) {
        jMenuItemNouveau = jMenuItemNouveau0;
    }

    /** Retourne la règle en cours d'execution 'sur' ce tapis */
    public RegleBelote getRegle() {
        return regle;
    }

}
