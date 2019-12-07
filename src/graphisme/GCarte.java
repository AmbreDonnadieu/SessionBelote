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

import cartes.Carte;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;


/**
 * Gère l'affichage graphique d'une carte
 * @author Clément
 */
public class GCarte {

    Carte carte;
    Image image;//, imgCartes;
    public static Image imgCartes, imgBack = null;
    public static int gWidth, gHeight;
    int x, y;
    public static int owidth, oheight;


    public GCarte( Component comp, Carte c, Image imgCartes0, int width0, int height0) {

        owidth = width0;
        oheight = height0;
        imgCartes = imgCartes0;
        carte = c;        
    }

    /** Ancienne version
    public GCarte( Carte c, Image imgCartes0 ) {
        carte = c;
        width = 128 / 2; height = 208 / 2;
        imgCartes = imgCartes0; imgCartes.getHeight(null);
   /*
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int sx = c.getValeur().toInt();
        if ( sx > 12) sx--;
        sx = 10 + sx * 150;
        int sy = c.getCouleur().toInt() * 210 + 10;
        
        image.getGraphics().drawImage(imgCartes, 0, 0, width, height, sx, sy, sx + 128, sy + 207, null);

    * /

    }
     */
/*
    public void chargeImages(Component comp) {

        Graphics2D g2d;

        if ( imgBack == null) {
            imgBack = new BufferedImage(gWidth, gHeight, BufferedImage.TYPE_INT_ARGB);
         //   imgBack = comp.createImage(width, height);
            if ( imgBack != null)
                imgBack.getGraphics().drawImage(imgCartes, 0, 0, gWidth, gHeight, owidth * 2, oheight * 4,
                    owidth * 3, oheight * 5, null);
        }
        if ( image == null) {
            int v = (carte.getValeur().toInt())>12?(carte.getValeur().toInt())-1:(carte.getValeur().toInt());
            int xx0 = owidth * ((v-1)%13);
            int yy0 = oheight * carte.getCouleur().toInt();
            //image = comp.createImage(width, height);
            image = new BufferedImage(gWidth, gHeight, BufferedImage.TYPE_INT_ARGB);
            
            g2d = (Graphics2D)image.getGraphics();
            g2d.setComposite(AlphaComposite.Src);
            g2d.drawImage(imgCartes,
                0, 0, gWidth, gHeight, xx0, yy0, xx0+owidth, yy0+oheight, null);
            
        }
    }
*/
    public void paint( Graphics g, int x0, int y0, boolean turned /* , Component comp */) {
        x = x0;
        y = y0;
 ((Graphics2D)g).setRenderingHints(new RenderingHints(
             RenderingHints.KEY_RENDERING,
             RenderingHints.VALUE_RENDER_QUALITY));
 /*
    ((Graphics2D)g).setRenderingHints(new RenderingHints(
             RenderingHints.KEY_TEXT_ANTIALIASING,
             RenderingHints.VALUE_TEXT_ANTIALIAS_ON));*/
 //       if ( image == null) return;

        /* Version avec plusieurs tailles d'images 
        if ( turned) {
            g.drawImage(imgCartes, x, y, x+width, y+height, width*2, height*4, width*3, height*5, null);
        } else {
            int v = (carte.getValeur().toInt())>12?(carte.getValeur().toInt())-1:(carte.getValeur().toInt());
            int xx0 = width * ((v-1)%13);
            int yy0 = height * carte.getCouleur().toInt();
            g.drawImage(imgCartes, x, y, x+width, y+height, xx0, yy0, xx0+width, yy0+height, null);
        }
*/

        /* Version avec une seule image et la taille change par le tapis */
        if ( turned) {
            g.drawImage(imgCartes, x, y, x+gWidth, y+gHeight, owidth*2, oheight*4, owidth*3, oheight*5, null);
        } else {
            int v = (carte.getValeur().toInt())>12?(carte.getValeur().toInt())-1:(carte.getValeur().toInt());
            int xx0 = owidth * ((v-1)%13);
            int yy0 = oheight * carte.getCouleur().toInt();
            ((Graphics2D) g).drawImage(imgCartes, x, y, x+gWidth, y+gHeight, xx0, yy0, xx0+owidth, yy0+oheight, null);
        }
 /*
        if ( turned ) g.drawImage(imgBack, x, y, null);
        else g.drawImage(image, x, y, null);
     //   g.setColor(Color.BLACK);
      //  g.drawRect(x, y, width, height);
  *
  */
    }

    /*
    public void oldpaint( Graphics g, int x0, int y0, boolean turned) {

        setLocation(x0, y0);

        if ( turned ) {
            
            g.setColor(Color.blue);
            g.fillRoundRect(x0, y0, width, height, 3, 3);
            g.setColor( Color.BLACK);
            g.drawRoundRect(x0, y0, width, height, 3, 3);

        } else {

            int sx = carte.getValeur().toInt() - 1;
            if ( (sx+1) == ValeureCarte.CARD_AS.toInt() ) sx = 0;
            else if ( sx > 11) sx--; // on saute le cavalier
            sx = 10 + sx * 150;
            int sy = carte.getCouleur().toInt() * 220 + 11;

            g.drawImage(imgCartes, x0, y0, x0 + width, y0 + height, sx, sy, sx + 128, sy + 207, null);
        }

      /*  g.setColor( Color.BLACK);
        g.drawRect(x0, y0, width, height);*/
 //   }

    @Override
    public String toString() {
        return "G:" + carte.toString();
    }

    public boolean contains(int x0, int y0) {
        return (x<= x0) && (y<=y0)&&((x+gWidth)>=x0)&&((y+gHeight)>=y0);
    }

}
