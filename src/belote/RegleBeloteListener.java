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

/**
 * Pour suivre une partie de belote (qui à fait quoi)
 * @author Clément
 */
public interface RegleBeloteListener {

    /** Reçoit un évenement survenu pendant la partie.
     *  En fonction du type d'évenement, vInt, vColor ou vCard sont renseignés */
    public void newBeloteEvent( BeloteEvent e);
}
