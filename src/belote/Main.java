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

import graphisme.FenetreDeJeuDeBelote;


public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

      /*  java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {*/
                new FenetreDeJeuDeBelote().setVisible(true);
    //        }
   //     });
    }

}
