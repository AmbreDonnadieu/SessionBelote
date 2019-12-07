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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

public class StatistiquesBelote implements javax.swing.table.TableModel, TableCellRenderer {

    class UnePartie {
        JoueurBelote qui_prend, qui_belote;
        boolean partie_gagnee;
        int sesPoints, leurPoints, totalNord, totalEst;

        UnePartie(JoueurBelote joueurQuiPrend, boolean gagne, JoueurBelote beloteEtRe, int ses_points, int leur_points, int total_nord, int total_est ) {
            qui_belote = beloteEtRe;
            qui_prend = joueurQuiPrend;
            partie_gagnee = gagne;
            sesPoints = ses_points;
            leurPoints = leur_points;
            totalNord = total_nord;
            totalEst = total_est;
        }
    }

    Color blue, dark_blue;
    ArrayList<UnePartie> parties;
    JTable component;
    ArrayList<TableModelListener> listeners = null;
    javax.swing.JLabel text;
    public static final String[] noms_cols = { "Qui a pris", "Son score", "Autre score", "Belote ?", "Total N/S", "Total E/O" };
    public int nbManchesN = 0, nbManchesE = 0;

    public StatistiquesBelote() {
        parties = new ArrayList<UnePartie>();
        text = new JLabel();
        text.setOpaque(true);
        component = null;
        blue = new Color( 0xe1, 0xe1, 0xff);
        dark_blue = new Color( 0x75, 0x75, 0xdf);
        listeners = new ArrayList<TableModelListener>();
    }

    public void setParent( JTable parent) {
        component = parent;

        // Pour scroller en fin da table quand celle-ci change de taille ( ajout d'une nouvelle ligne)
        component.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                component.scrollRectToVisible(component.getCellRect(component.getRowCount()-1, 0, true));    }
            });
    }

    void ajoutManche(JoueurBelote j) {
        if ( (j.getOrdre() == RegleBelote.JOUEUR_OUEST) || (j.getOrdre()==RegleBelote.JOUEUR_EST)) nbManchesE++;
        else nbManchesN++;
        
        parties.add(new UnePartie(null, false, null, 0, 0, 0, 0));
        for ( TableModelListener l : listeners)
            l.tableChanged( new TableModelEvent(this));
    }

    public void ajoutPartie(JoueurBelote joueurQuiPrend, boolean gagne, JoueurBelote beloteEtRe, int sesPoints, int leurPoints) {
        int totalN = 0, totalE = 0;

        if ( parties.size() != 0) {
            UnePartie p = parties.get(parties.size()-1);
            totalN = p.totalNord;
            totalE = p.totalEst;
        }

        if ((joueurQuiPrend.getOrdre() == RegleBelote.JOUEUR_NORD) || (joueurQuiPrend.getOrdre() == RegleBelote.JOUEUR_SUD)) {
            if (sesPoints >0) totalN += sesPoints;
            totalE += leurPoints;
        } else {
            if (sesPoints >0) totalE += sesPoints;
            totalN += leurPoints;
        }

        parties.add(new UnePartie(joueurQuiPrend, gagne, beloteEtRe, sesPoints, leurPoints, totalN, totalE));
        for ( TableModelListener l : listeners)
            l.tableChanged( new TableModelEvent(this));
        
    }

    @Override
    public int getRowCount() {
        return parties.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return noms_cols[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        UnePartie p = parties.get(rowIndex);
        if ( p.qui_prend == null) return "";

         switch ( columnIndex) {
            case 0: return p.qui_prend.nom;
            case 1: if ( p.sesPoints >= 0) return "" + p.sesPoints; else return "(" + -p.sesPoints + ")";
            case 2: return "" + p.leurPoints;
            case 3: if ( p.qui_belote != null) return "" + p.qui_belote.nom; else return "";
            case 4: return "" + p.totalNord;
            case 5: return "" + p.totalEst;
            default: return "???";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        text.setText(value.toString());
        text.setHorizontalAlignment(SwingConstants.CENTER);

        JoueurBelote j = parties.get(row).qui_prend;
        if ( j == null) text.setBackground(Color.DARK_GRAY);
        else if ( ! parties.get(row).partie_gagnee) {
            //text.setForeground(Color.WHITE);
            if ( (j.getOrdre()==1) || (j.getOrdre()==3)) text.setBackground(Color.LIGHT_GRAY);
            else text.setBackground(dark_blue);
        }
        else {
            //text.setForeground(Color.BLACK);
            if ( (j.getOrdre()==1) || (j.getOrdre()==3)) text.setBackground(Color.WHITE);
            else text.setBackground(blue);
        }
        return text;
    }

}
