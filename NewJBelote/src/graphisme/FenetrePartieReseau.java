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


import belote.BeloteEvent;
import belote.AnalyseurJeuTemp;
import belote.AnalyseurListener;
import belote.joueur.IJoueurBelote;
import belote.joueur.JoueurHumain;
import belote.joueur.JoueurIA;
import belote.joueur.JoueurReseau;
import cartes.PileDeCarte;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
//import sun.misc.Regexp;

/**
 * Gestionnaire de parties réseau
 * @author Clément
 */
public class FenetrePartieReseau extends javax.swing.JFrame implements TableModel, AnalyseurListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* Action des joueurs envoyés et reÃ§us par le server */
    public static final int T_COUPE  = 1;
    public static final int T_ATOUT1 = 2;
    public static final int T_ATOUT2 = 3;
    public static final int T_CARTE  = 4;
    /** Pour envoyer et reÃ§evoir, par le serveur, le jeu de cartes identique */
    public static final int T_JEU    = 5;

    public static final String[] action_joueurs = { "RIEN", "COUPE", "AJOUT1", "AJOUT2", "CARTE" };

    
    boolean pasDePartie, partieACreer, partieEnCours, majPartie = true, destroyThread;
    protected String urlServeur, passGame, nomDePartie;
    String idJoueurHumain = null;
    int posJoueurHumain = -1; // L'endroit ou le joueur humain est assis

    private String justeLes20premiers(String str) {
        if ( str.length() > 20) return str.substring(0,20);
        else return str;
    }

    private void finDePArtieImpromptue(String error) {
        SwingUtilities.invokeLater(new Runnable() {
                    String j;
                    public Runnable setJ( String j0) { j = j0; return this; }
                    @Override public void run() {
                        JOptionPane.showMessageDialog(null, "La partie réseau Ã  été annulée cÃ´té serveur",
                                "Erreur réseau ("+j+")", JOptionPane.ERROR_MESSAGE); }}.setJ(error));
        detruitPartieReseau();
        tapis.getAnalyseur().demandeLaFinDeLaPartie(false);
    }

    class Joueur {

        IJoueurBelote joueur;    // Je joueur de belote associé Ã  l'ID sur le serveur
        String id, nom;
        String value;
        String synchro;

        public Joueur( String id, String nom) {
            this.id = id;
            this.nom = nom;
        }
    }
    Joueur[] joueurs = { null, null, null, null };
    boolean majJoueursReseau;

    TapisDeBelote tapis;

    /** Creates new form PartieReseauFrame */
    public FenetrePartieReseau( TapisDeBelote tapis0, FenetreDeChat fenetreDeChat0) {
        fenetreDeChat = fenetreDeChat0;
        tapis = tapis0;
        initComponents();
        setLocationRelativeTo( tapis);
        startMajJoueursReseau();
    }

    @Override
    public void dispose() {
        fenetreDeChat.addChat("QUIT");
        destroyThread = true;

        // On quitte la partie réseau proprement
        if ( ! jButtonOpenGame.getText().equals("Ouvrir"))
            jButtonOpenGameActionPerformed(null);

        super.dispose();
    }

    int dansMAJReseau = 0;

    /** Crée un thread qui appel la méthode majJoueursReseau() toute les secondes */
    void startMajJoueursReseau() {
        majJoueursReseau = true;
        new Thread(new Runnable() {
            @Override public void run() {
                while ( ! destroyThread ) {
                    if (! partieEnCours || majPartie) {
                        SwingUtilities.invokeLater( new Runnable() {
                            @Override public void run() {
                                if ( dansMAJReseau == 0)majJoueursReseau(); }});
                    }
                    try { Thread.sleep(4000); } catch (InterruptedException ex) { }
                }
            }
        }).start();
    }

    /** Renvoie le composant graphique TapisDeBelote pour débloquer son rafraichissement */
    public TapisDeBelote getTapis() {
        return tapis;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldGameName = new javax.swing.JTextField();
        jButtonNewGame = new javax.swing.JButton();
        jButtonUseGame = new javax.swing.JButton();
        jComboBoxPosition = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldPseudo = new javax.swing.JTextField();
        jButtonStartGame = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldURL = new javax.swing.JTextField();
        jButtonConnect = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabelTestResult = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButtonOpenGame = new javax.swing.JButton();
        jToggleButtonMAJ = new javax.swing.JToggleButton();

        setTitle("Gestionnaire de partie en réseau");

        jLabel1.setText("Participants : (0 avec 2 et 1 avec 3)");

        jTable1.setModel(this);
        jTable1.setToolTipText("Liste des participants Ã  cette partie");
        jTable1.setEnabled(false);
        jTable1.setFocusable(false);
        jScrollPane1.setViewportView(jTable1);

        jLabel2.setText("Nom de la partie :");

        jTextFieldGameName.setToolTipText("Saisissez un nom de partie (20 caractÃ¨res max)");
        jTextFieldGameName.setEnabled(false);
        jTextFieldGameName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldGameNameActionPerformed(evt);
            }
        });
        jTextFieldGameName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldGameNameKeyTyped(evt);
            }
        });

        jButtonNewGame.setText("Créer");
        jButtonNewGame.setToolTipText("Créer une nouvelle partie");
        jButtonNewGame.setEnabled(false);
        jButtonNewGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewGameActionPerformed(evt);
            }
        });

        jButtonUseGame.setText("Participer");
        jButtonUseGame.setToolTipText("Entrez dans la partie !");
        jButtonUseGame.setEnabled(false);
        jButtonUseGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUseGameActionPerformed(evt);
            }
        });

        jComboBoxPosition.setModel(
            new javax.swing.DefaultComboBoxModel(
                new String[] { "0", "1", "2", "3" }));
        jComboBoxPosition.setToolTipText("La position autour de la table");

        jLabel3.setText("Participer avec le pseudo :");

        jTextFieldPseudo.setToolTipText("Votre pseudo pour participer");
        jTextFieldPseudo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPseudoActionPerformed(evt);
            }
        });
        jTextFieldPseudo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldPseudoKeyTyped(evt);
            }
        });

        jButtonStartGame.setText("Commencer");
        jButtonStartGame.setToolTipText("Démarrer la partie (automatique pour les clients)");
        jButtonStartGame.setEnabled(false);
        jButtonStartGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartGameActionPerformed(evt);
            }
        });

        jLabel4.setText("URL du serveur de parties en réseau :");

        jTextFieldURL.setText("http://opentom.free.fr/belote/serveur_belote.php");
        jTextFieldURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldURLActionPerformed(evt);
            }
        });

        jButtonConnect.setText("Connecter");
        jButtonConnect.setToolTipText("Pour se connecter au serveur");
        jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectActionPerformed(evt);
            }
        });

        jLabel5.setText("Résultat :");

        jLabelTestResult.setText("aucun");

        jLabel6.setText("En position :");

        jButtonOpenGame.setText("Chercher");
        jButtonOpenGame.setToolTipText("Ouvrir une partie existante");
        jButtonOpenGame.setEnabled(false);
        jButtonOpenGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenGameActionPerformed(evt);
            }
        });

        jToggleButtonMAJ.setText("Mise Ã  jour");
        jToggleButtonMAJ.setToolTipText("Désactivez la mise Ã  jour automatique de la partie si cela sature le serveur");
        jToggleButtonMAJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonMAJActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButtonMAJ))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonConnect)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelTestResult))
                    .addComponent(jLabel2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTextFieldGameName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOpenGame)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonNewGame))
                    .addComponent(jTextFieldURL)
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPseudo))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonUseGame)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonStartGame)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonConnect)
                    .addComponent(jLabelTestResult)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldGameName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonNewGame)
                    .addComponent(jButtonOpenGame))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jToggleButtonMAJ))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldPseudo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jComboBoxPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonUseGame)
                    .addComponent(jButtonStartGame))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonNewGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewGameActionPerformed
        try { nomDePartie = URLEncoder.encode(justeLes20premiers(jTextFieldGameName.getText()), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            jTextFieldGameName.setText("");
            return;
        }
        if ( nomDePartie.equals("")) {
            nomDePartie = null;
            return;
        }

        String r;
        try { r = ditAuServeur("action=NEW&game="+nomDePartie);
        } catch ( RuntimeException e) {
            nomDePartie = null;
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ( r.startsWith("ERROR")) {
            nomDePartie = null;
            JOptionPane.showMessageDialog(this, "Le serveur Ã  répondu :\n"+r, "Impossible de créer une partie", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if ( r.startsWith("OK")) {
            listener.tableChanged(null);
            passGame = r.split(":")[1];
            jButtonOpenGame.setText("Fermer");
            try { jTextFieldGameName.setText(URLDecoder.decode(nomDePartie, "UTF-8"));
            } catch (UnsupportedEncodingException ex) { }
            jButtonNewGame.setEnabled(false);
            jTextFieldGameName.setEnabled(false);
        } else {
            passGame = nomDePartie = idJoueurHumain = null;
            jButtonOpenGame.setText("Ouvrir");
            jButtonNewGame.setEnabled(true);
            jTextFieldGameName.setEnabled(true);
        }
        jTextFieldPseudoKeyTyped(null);
        jButtonStartGame.setEnabled(passGame!=null);
    }//GEN-LAST:event_jButtonNewGameActionPerformed

    private void jButtonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectActionPerformed
        
        urlServeur = jTextFieldURL.getText();
        String r = ditAuServeur( "action=VERSION");

        if ( r != null) {
            jLabelTestResult.setText(r);
            jButtonOpenGame.setEnabled(true);

        } else {
            jLabelTestResult.setText( "Erreur");
            urlServeur = null;
        }
        jTextFieldGameName.setEnabled(urlServeur!=null);
    }//GEN-LAST:event_jButtonConnectActionPerformed

    private void jTextFieldURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldURLActionPerformed
        jButtonConnectActionPerformed(null);
    }//GEN-LAST:event_jTextFieldURLActionPerformed

    private void jButtonOpenGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenGameActionPerformed
        if ( urlServeur != null) {
            if ( ! jButtonOpenGame.getText().equals("Fermer") && (nomDePartie ==null))
            try {
                String nom = URLEncoder.encode(justeLes20premiers(jTextFieldGameName.getText()),"UTF-8");
                if ( nom.equals("")) {
                    String parties[] = recupereNomsPartiesReseau();
                    if ( parties.length == 0) {
                        JOptionPane.showConfirmDialog(this, "Il n'y a aucune partie disponible sur le serveur.", 
                                "Liste des parties réseau", JOptionPane.CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        jButtonOpenGame.setEnabled(true);
                        jButtonOpenGame.setText("Ouvrir");
                        return;
                    }

                    String r=  (String)JOptionPane.showInputDialog(this, "Choisissez la partie :",
                                    "Ouverture d'une partie existante",
                                    JOptionPane.QUESTION_MESSAGE, null,
                                     parties, null);
                    
                    if ( r != null) nom = URLEncoder.encode(r.split(":")[0],"UTF-8");
                    else return;

                }

                jButtonOpenGame.setEnabled(false);
                jButtonOpenGame.setText("...");
                String r = ditAuServeur("action=OPEN&game="+nom);
                jButtonOpenGame.setEnabled(true);

                if ( r.startsWith("OK")) {
                    if ( passGame != null) detruitPartieReseau();
                    jTextFieldGameName.setText(URLDecoder.decode(nomDePartie = nom,"UTF-8"));
                    jButtonStartGame.setEnabled(false);
                    jTable1.setEnabled(true);
                    jButtonOpenGame.setText("Fermer");
                    jButtonNewGame.setEnabled(false);
                    jTextFieldGameName.setEnabled(false);
                    jButtonConnect.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Le serveur Ã  répondu :\n"+r,"Impossible d'ouvrir la partie", JOptionPane.ERROR_MESSAGE);
                    if ( jTextFieldGameName.getText().equals(""))
                        jButtonOpenGame.setText("Chercher");
                    else
                        jButtonOpenGame.setText("Ouvrir");
                    nomDePartie = null;
                }

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FenetrePartieReseau.class.getName()).log(Level.SEVERE, null, ex);
            }
            else { // Quitte ou détruit cette partie

                if ( (nomDePartie!=null) && ! tapis.verifyEndOfGame()) return;

                if ( passGame == null) {
                    ditAuServeur("action=QUIT&game=" + nomDePartie + "&client=" + idJoueurHumain);
                } else
                    detruitPartieReseau();

                partieEnCours = false;
                nomDePartie = passGame =null;
                for ( int i = 0; i< 4; i++) joueurs[i] = null;
                jButtonOpenGame.setText("Ouvrir");
                jButtonNewGame.setEnabled(true);
                jTextFieldGameName.setEnabled(true);
                jButtonConnect.setEnabled(true);
                jButtonStartGame.setText("Commencer");
                tapis.endOfGame();
            }
        }
        jTextFieldPseudoKeyTyped(null);
    }//GEN-LAST:event_jButtonOpenGameActionPerformed

    private void jButtonUseGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUseGameActionPerformed
        if ( nomDePartie != null ) {
            if ( idJoueurHumain != null)
                ditAuServeur("action=QUIT&game="+nomDePartie+"&client="+idJoueurHumain);

            jButtonUseGame.setEnabled(false);
            int pos = jComboBoxPosition.getSelectedIndex();
            try {
                idJoueurHumain = placeParticipantReseauEn(pos, URLEncoder.encode(justeLes20premiers(jTextFieldPseudo.getText()), "UTF-8"));
                jButtonUseGame.setEnabled(true);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                jTextFieldPseudo.setText("");
            }
        }
    }//GEN-LAST:event_jButtonUseGameActionPerformed

    private void jButtonStartGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartGameActionPerformed
        if ( (nomDePartie!=null) && ((passGame!=null) || (posJoueurHumain != -1))) {

            majJoueursReseau = false;
            majJoueursReseau();
            partieEnCours = true; pasDePartie = false;
            IJoueurBelote j[] = { null, null, null, null };

            // Inscription des joueurs sur le serveur réseau
            for ( int i = 0 ; i < 4; i++) {
                int p = (i+4-posJoueurHumain+2)%4;
                if ( (passGame!=null) && (joueurs[i] == null)) {
                        String id = placeParticipantReseauEn(i, "Ordi_"+i);
                        if ( id == null) { detruitPartieReseau(); return; }
                        joueurs[i] = new Joueur(id,"Ordi_"+i);
                        j[p] = joueurs[i].joueur = new JoueurIA(joueurs[i].nom, p);

                } else if ( joueurs[i].id.equals(idJoueurHumain))
                        j[p] = joueurs[i].joueur = new JoueurHumain(tapis.getAnalyseur().getRegle(),
                        		joueurs[i].nom+"(vous)",
                        		p,
                        		tapis,
                        		tapis.getAnalyseur().getGraphicListener());
                else
                        j[p] = joueurs[i].joueur = new JoueurReseau(joueurs[i].nom, p, this);

            }

            jButtonStartGame.setText("En cours");
            jButtonStartGame.setEnabled(false);
            jButtonConnect.setEnabled(false);
            jButtonUseGame.setEnabled(false);

            // Création ou récupération du jeu de carte commun
            PileDeCarte p;
            if (passGame!=null) {
                p = PileDeCarte.getJeuBelote();
                p.melange();
                envoieLesCartesAuServeur( p);
            } else {
                p = recupereLesCartesDepuisLeServeur();
            }

            // Initialisation de la partie
            tapis.decoreJeuDeCarte(p);
            AnalyseurJeuTemp r = new AnalyseurJeuTemp(p, j, joueurs[AnalyseurJeuTemp.JOUEUR_EST].joueur.getOrdre());
            r.setBeloteListener(this);
            tapis.nouvellePartie( r);
            partieACreer = false;
        }
    }//GEN-LAST:event_jButtonStartGameActionPerformed

    private void jTextFieldPseudoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPseudoKeyTyped
        jButtonUseGame.setEnabled( (nomDePartie!=null) && jTextFieldPseudo.getText().length() >= 3);
    }//GEN-LAST:event_jTextFieldPseudoKeyTyped

    private void jTextFieldPseudoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPseudoActionPerformed
        if ( jTextFieldPseudo.getText().length() >= 3)
            jButtonUseGameActionPerformed(null);
    }//GEN-LAST:event_jTextFieldPseudoActionPerformed

    private void jToggleButtonMAJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonMAJActionPerformed
        majPartie = jToggleButtonMAJ.isSelected();
        if ( majJoueursReseau ) majJoueursReseau();
    }//GEN-LAST:event_jToggleButtonMAJActionPerformed

    private void jTextFieldGameNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldGameNameActionPerformed
        jButtonNewGameActionPerformed(null);
    }//GEN-LAST:event_jTextFieldGameNameActionPerformed

    private void jTextFieldGameNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldGameNameKeyTyped
        if ( jTextFieldGameName.getText().equals("")) {
            jButtonOpenGame.setText("Chercher");
            jButtonNewGame.setEnabled(false);
        } else {
            jButtonOpenGame.setText("Ouvrir");
            jButtonNewGame.setEnabled(true);
        }
    }//GEN-LAST:event_jTextFieldGameNameKeyTyped

    /** Mise Ã  jour Ã  partir du serveur de la table 'joueurs' */
    private void majJoueursReseau() {
        dansMAJReseau++;
        if (nomDePartie != null) {
            String r;
            do {
                r = ditAuServeurSansTrace("action=VIEW&game="+nomDePartie);
            } while ( (r==null) || r.startsWith("ERROR0") || r.startsWith("<!DOCTYPE"));
            if ( r.startsWith("ERROR")) {
                // Ferme la partie
                nomDePartie = null;
                jButtonOpenGameActionPerformed(null);
            } else {
                int jcourant = -1, pos = 0;
                posJoueurHumain = -1;
                for ( String l : r.split("\n")) {
                    if ( l.startsWith("J")) {
                        String[] t = l.split(":");
                        String id = t[1];
                        String nom = null;
                        if ( t.length >= 3) nom = t[2];

                        if ( id.equals(idJoueurHumain)) posJoueurHumain = pos;

                        if ( ! id.equals("-1")) {
                            if ( (joueurs[pos]== null) || ! partieEnCours)
                                joueurs[pos] = new Joueur( id, nom);
                            else {
                                if ( ! joueurs[pos].id.equals(id)) {
                                    joueurs[pos].id = id;
                                    joueurs[pos].nom = nom;
                                }
                            }
                            if ( joueurs[pos] != null) {
                                if ( t.length > 3 ) joueurs[pos].synchro = t[3] + ((jcourant==pos)?"(*)":"");
                                else joueurs[pos].synchro = ((jcourant==pos)?"(*)":"");
                                if ( t.length >= 5) joueurs[pos].value = t[4];
                                else joueurs[pos].value = null;
                            }
                            
                        } else if ( ! partieEnCours ) joueurs[pos] = null;
                                else if ( passGame != null) {
                                    // un joueur c'est barré, si je suis le propriétaire,
                                    // il faut le remplacer :-~
                                    r = ditAuServeur("action=USE&game="+nomDePartie+
                                            "&pass="+passGame+"&pos="+pos+"&pseudo=Ordi_"+joueurs[pos].nom);
                                    if ( r.startsWith("OK")) {
                                        joueurs[pos].id = r.split(":")[1];
                                        joueurs[pos].nom = "Ordi_"+joueurs[pos].nom;
                                        joueurs[pos].joueur = tapis.getAnalyseur().getJoueur(joueurs[pos].joueur.getOrdre()).createClone();
                                        joueurs[pos].joueur.setNom(joueurs[pos].nom);
                                        tapis.getAnalyseur().changeJoueurPar(joueurs[pos].joueur);
                                        // Il est forcément synchro !
                                        ditAuServeur("action=OK&game="+nomDePartie+"&client="+joueurs[pos].id);
                                    } else System.err.println("Impossible de remplacer le joueur");
                                }

                        pos++;

                    } else if ( l.startsWith("ETAT")) {
                        jcourant = Integer.valueOf(l.split(":")[1]);

                    } else if ( l.startsWith("S")) {
                        if ( joueurs[pos%4] != null)
                            joueurs[pos%4].synchro = l.split(":")[1];
                        pos++;
                    }
                }
                
                jButtonStartGame.setEnabled(!partieEnCours && (passGame!=null));

                // On démare la partie si le jeu de carte Ã  été envoyé
                if ( (! partieEnCours) && (! partieACreer) && (jcourant > 3) ) {
                    // Démarre la partie !
                    partieACreer = true;
                    jButtonStartGameActionPerformed(null);
                }
            }
        }
        listener.tableChanged(null);
        dansMAJReseau--;
    }

    /** Demande au serveur la liste des parties réseau */
    String[] recupereNomsPartiesReseau() {
        ArrayList<String> a = new ArrayList<String>();

        String r;
        do {
            r = ditAuServeur("action=LIST");
            if ( (r==null) || r.startsWith("<!DOCTYPE"))
                try { Thread.sleep(5000); } catch (InterruptedException ex) { }
        } while ((r==null) || r.startsWith("<!DOCTYPE"));

        if ( r.startsWith("ERROR")) {
            JOptionPane.showMessageDialog(this, "Le serveur Ã  répondu :\n"+r,
                    "Récupération des parties en réseau", JOptionPane.ERROR_MESSAGE);
            return a.toArray(new String[a.size()]);
        }
        
        for ( String s : r.split("\n"))
            if ( ! s.equals("")) a.add(s);

        return a.toArray(new String[a.size()]);

    }

    /** Utilise une place autour de la table a travers le réseau retourne l'ID du joueur placé */
    String placeParticipantReseauEn( int pos, String pseudo) {
        String r = ditAuServeur("action=USE&pos="+pos+"&game="+nomDePartie+"&pseudo="+pseudo);
        if ( r.startsWith("OK"))
            return r.split(":")[1];
        else
            return null;
    }

    private String envoieLesCartesAuServeur(PileDeCarte p ) {
        String keys[] = { "game", "action", "pos", "value", "pass" };
        String values[] = { jTextFieldGameName.getText(), "PUT", "4", p.encode(), passGame };
        String r;
        do {
            r = httpPost(jTextFieldURL.getText(), keys, values);
            if ( ! r.startsWith("OK"))
                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
        } while ( ! r.startsWith("OK"));
        return r;
    }

    private PileDeCarte recupereLesCartesDepuisLeServeur() {

        String r;
        do {
            r = ditAuServeur("game="+nomDePartie+"&action=GET&pos=4");
            if ( r.startsWith("ERROR"))
                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
        } while ( r.startsWith("ERROR"));
        return PileDeCarte.decode( r);
    }

    public String getActionOf(IJoueurBelote me, int phaseDeJeu) {
        String r;
        String[] l;
        int pos = getJoueurReseau(me);

        do {
            r = ditAuServeur("game="+nomDePartie+"&action=GET&pos="+pos);
            if ( r.startsWith("ERROR7") && (passGame!=null)) {
                // Le joueur est parti
                r = ditAuServeur("action=USE&game="+nomDePartie+
                                 "&pass="+passGame+"&pos="+pos+"&pseudo=Ordi_"+joueurs[pos].nom);
                if ( r.startsWith("OK")) {
                    joueurs[pos].id = r.split(":")[1];
                    joueurs[pos].nom = "Ordi_"+joueurs[pos].nom;
                    joueurs[pos].joueur = tapis.getAnalyseur().getJoueur(joueurs[pos].joueur.getOrdre()).createClone();
                    joueurs[pos].joueur.setNom(joueurs[pos].nom);
                    tapis.getAnalyseur().changeJoueurPar(joueurs[pos].joueur);
                    // Il est forcément synchro !
                    ditAuServeur("action=OK&game="+nomDePartie+"&client="+joueurs[pos].id);
                    return null;
                } else {
                    System.err.println("Impossible de remplacer le joueur");
                    tapis.getAnalyseur().demandeLaFinDeLaPartie(false);
                }
            } if ( r.startsWith("ERROR20")) {
                finDePArtieImpromptue(r);
                return null;
            }

            l = r.split("\\|");
            if ( (l.length==0) || (l[0].length() == 0) || r.startsWith("ERROR") )
                try { Thread.sleep(2000); } catch (InterruptedException ex) { }
        } while ( (pasDePartie || partieEnCours) && ((l.length==0) || (l[0].length() == 0) || l[0].startsWith("ERROR")));

        if ( ! partieEnCours ) return null;
        if ( l.length > 1 ) fenetreDeChat.addChat( "<" + me.getNom() + "> dit : " + l[1]);
        return l[0];
    }

   /** Ferme proprement la partie ouverte sur le serveur */
   private void detruitPartieReseau() {
       if ( passGame != null)
            ditAuServeur("action=DESTROY&pass="+passGame+"&game="+nomDePartie);

       partieEnCours = false;
       pasDePartie = true;
       passGame = nomDePartie = null;
    }
    
    /** Retourne la position du prochain joueur dans la partie réseau */
    int getProchainJoueurReseau() {
        String r;
        do {
            r = ditAuServeur("action=WHO&game="+nomDePartie);
            if ( ! r.startsWith("OK:"))
                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
        } while ( ! r.startsWith("OK:"));
        return Integer.valueOf(r.split(":")[1]);
    }

    /** Est appelé par RegleBelote pour informer de ce qui ce passe pendant le jeu */
    @Override
    public void newBeloteEvent( BeloteEvent e) {
        String r, value = "error";

        if ( e.type == BeloteEvent.EV_QUIT) {
            if ( nomDePartie != null)
                SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override public void run() {
                            jButtonOpenGameActionPerformed(null); } });
            return;
        }

        boolean avecHumain = false;
        for ( int i = 0; (i < 4); i++) {
            if ( joueurs[i] == null)
                joueurs[i] = null; // DEBUG
            if ( joueurs[i].joueur instanceof JoueurHumain)
                avecHumain = true;
        }
        
        if ( e.type == BeloteEvent.EV_SYNCHRO) {

            // Envoie la synchro pour tous les joueurs gérés
            for ( int i = 0; i < 4; i++)
                if ( ! (joueurs[i].joueur instanceof JoueurReseau)) {
                    // Valide le tour par le joueur local
                    do {
                        r = ditAuServeur("game="+nomDePartie+"&action=OK&client="+joueurs[i].id);
                        if ( ! r.startsWith("OK"))
                            try { Thread.sleep(1000); } catch (InterruptedException ex) { }
                    } while ( ! r.startsWith("OK"));
                }
                

            if ( passGame == null) {
                // Attend le NEXT du propriétaire de la partie si on a un joueur humain
                boolean wait = avecHumain;
                
                while( wait && (nomDePartie!=null) ) {
                    r = ditAuServeur("action=NEXT&game="+nomDePartie+"&client="+idJoueurHumain);
                    wait = ! r.startsWith("OK");
                    if ( wait)
                        try { Thread.sleep(1000); } catch (InterruptedException ex) { }
                }

            } else {
                // Attend que tout le monde soit synchro
                // Envoie le prochain joueurCourrant
                do {
                    r = ditAuServeur("game="+nomDePartie+"&action=NEXT&pass="+passGame+
                                 "&pos="+getJoueurReseau(e.from));
                    if ( ! r.startsWith("OK"))
                            try { Thread.sleep(1000); } catch (InterruptedException ex) { }
                } while ( (nomDePartie!=null) && ! r.startsWith("OK"));
            }

            return;
        }

        int i = getJoueurReseau( e.from);
        // Si c'est un coup d'un joueur réseau ne fait rien
        if ( joueurs[ i].joueur instanceof JoueurReseau)
            return;

        switch ( e.type) {
            case BeloteEvent.EV_COUPE:
                value = "" + e.value; break;
            case BeloteEvent.EV_ATOUT1:
                value = (e.value==0) ? "N" : "Y"; break;
            case BeloteEvent.EV_ATOUT2:
                value = (e.color==null) ? "N" : "" + e.color.toInt(); break;
            case BeloteEvent.EV_CARTE:
                value = "" + e.card.getValeur().toInt() + "," + e.card.getCouleur().toInt(); break;

        }
        if ( (i >= 0) && (i<4) ) {
            do {
                r = envoieActionDe( i, joueurs[i].id, value, joueurs[i].joueur);
                if ( ! r.startsWith("OK") )
                    try { Thread.sleep(1000); } catch (InterruptedException ex) { }
            } while ( (pasDePartie || partieEnCours) && ! r.startsWith("OK"));
        } else {
            finDePArtieImpromptue("J'ai disparu ?");
        }
    }

    String envoieActionDe( int pos, String idJoueur, String val, IJoueurBelote me) {
        String msg = null;
        if ( me instanceof JoueurHumain) {
            msg = fenetreDeChat.getChat();
            if ( (msg != null) && ! msg.endsWith("\n")) msg += "\n";
        }
        if ( msg == null) msg = "";
        else fenetreDeChat.addChat("LOCAL: " + msg);

        String keys[] = { "game", "action", "pos", "client", "value", "chat" };
        String values[] = { nomDePartie, "PUT", String.valueOf(pos), idJoueur, val, msg };
        String r;
        do {
            r = httpPost(urlServeur, keys, values);
            if ( r.startsWith("ERROR20")) { // La partie n'existe plus sur le serveur
                finDePArtieImpromptue(r);
                return null;
            }
            if ( ! r.startsWith("OK"))
                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
        } while ( ! r.startsWith("OK"));
        return r;
    }


    /** Retourne le joueurReseau correspondant au joueur belote */
    int getJoueurReseau( IJoueurBelote j) {
        for ( int i = 0; i < 4; i ++)
            if ( ( joueurs[i] != null ) && (joueurs[i].joueur == j))
                return i;

        // Au cas oÃ¹ il fÃ»t remplacé entre temps
        j = j.getSuivant().getPrecedent();
        for ( int i = 0; i < 4; i ++)
            if ( ( joueurs[i] != null ) && (joueurs[i].joueur == j))
                return i;

        // Pas de joueur !
        return -1;
    }

    /** Envoie une requÃªte GET au serveur avec l'URL:
     *      jTextFieldURL.getText() + "?" + args
     *
     *  Délais 5 secondes si trop de requÃªtes
     *  @return La valeur de retour du serveur
     */
    public String ditAuServeur( String args) {
        String r;
        do {
            r = httpGet(urlServeur + "?" + args);
            if ( (r.startsWith("ERROR99")) || r.startsWith("<!DOCTYPE"))
                try { Thread.sleep(5000); } catch (InterruptedException ex) { }
        } while ( (pasDePartie || partieEnCours) && (r.startsWith("ERROR99") || r.startsWith("<!DOCTYPE")));
        System.err.println("ditAuServeur(\""+args+"\")=>["+r+"]\n");
        return r;

    }

    public String ditAuServeurSansTrace( String args) {
        String r;
        do {
            r = httpGet(urlServeur + "?" + args);
            if ( (r.startsWith("ERROR99")) || r.startsWith("<!DOCTYPE"))
                try { Thread.sleep(5000); } catch (InterruptedException ex) { }
        } while ( (pasDePartie || partieEnCours) && (r.startsWith("ERROR99") || r.startsWith("<!DOCTYPE")));
        return r;
    }

  /*  private String postChat(String chat) {
        if ( chat == null) return null;
        String r;
        do {
            fenetreDeChat.addChat( "**[(local)]** : " + chat);
            String keys[] = { "game", "action", "client", "value" };
            String values[] = { nomDePartie, "CHAT", idJoueurHumain, chat };
            r = httpPost( urlServeur, keys, values);
        } while ( (pasDePartie || partieEnCours) && (r.startsWith("ERROR99") || r.startsWith("<!DOCTYPE")));
        return r;
    }
*/

    /** Effectue une requÃªte HTTP/GET et renvoie la réponse sous forme de String */
    public String httpGet(String url) {
        try {
            String source = "";

            BufferedReader in = new BufferedReader(new InputStreamReader(new java.net.URL(url).openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
                source += (source.equals("")?inputLine:"\n" + inputLine);
            in.close();
            return source;
        } catch (IOException ex) {
            if ( ex instanceof FileNotFoundException ) return "ERROR99:URL_NOT_FOUND";
            else {
                ex.printStackTrace();
                return "ERROR99:"+ex.toString();
            }
        }
    }

    public String httpPost(String adress,String keys[],String values[]) {
        String result = "";
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        try {
            //encodage des paramÃ¨tres de la requÃªte
            String data="";
            for(int i=0;i<keys.length;i++){
                if (i!=0) data += "&";
                data +=URLEncoder.encode(keys[i], "UTF-8")+"="+URLEncoder.encode(values[i], "UTF-8");
            }
            //création de la connection
            HttpURLConnection conn = (HttpURLConnection)new java.net.URL(adress).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            conn.setRequestProperty("Content-Language", "en-US");
            conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            //envoi de la requÃªte
            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data);
            writer.flush();
            //lecture de la réponse<br>
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ligne;
            while ((ligne = reader.readLine()) != null) result += ligne + "\n";
        
        } catch (Exception e) {
            e.printStackTrace();
            result = "ERROR99:"+e.toString();
        } finally {
            try{ writer.close(); }catch( Exception e ) { }
            try{ reader.close(); }catch( Exception e ) { }
        }
        System.out.println("httpPost,result=["+result+"]");
        return result;
    }


    /*
     * Partie relative Ã  la gestion de l'affichage du contenu du tableau
     *                                          des joueurs dans la Fenetre.
     *
     */

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( nomDePartie != null ) {
            switch ( columnIndex) {
                case 0: if ( joueurs[rowIndex] != null )
                            return "" + rowIndex + ": " + joueurs[rowIndex].nom + "(" + joueurs[rowIndex].id + ")";
                        else
                            return "" + rowIndex + ": Libre";

                case 1: if ( joueurs[rowIndex] != null )
                            if ( joueurs[rowIndex].id == null) return "null";
                            else if (joueurs[rowIndex].id.equals(idJoueurHumain)) return "Vous (local)";
                            else return "Joueur réseau";
                        else return "";
                case 2: if ( joueurs[rowIndex]==null) return "";
                        else if ( joueurs[rowIndex].value != null)
                                    return joueurs[rowIndex].synchro + "<" + joueurs[rowIndex].value + ">" ;
                        else if ( joueurs[rowIndex].synchro != null)
                                    return joueurs[rowIndex].synchro;
            }
        }

        return "";
    }

    @Override
    public int getRowCount() { return 4; }

    @Override
    public int getColumnCount() { return 3; }

    String col[] = { "Emplacements", "Type", "Etat" };

    @Override
    public String getColumnName(int columnIndex) { return col[columnIndex]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) { return String.class; }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { }

    @Override
    public void addTableModelListener(TableModelListener l) { listener = l; }

    @Override
    public void removeTableModelListener(TableModelListener l) { listener = null; }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JButton jButtonNewGame;
    private javax.swing.JButton jButtonOpenGame;
    private javax.swing.JButton jButtonStartGame;
    private javax.swing.JButton jButtonUseGame;
    private javax.swing.JComboBox jComboBoxPosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelTestResult;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldGameName;
    private javax.swing.JTextField jTextFieldPseudo;
    private javax.swing.JTextField jTextFieldURL;
    private javax.swing.JToggleButton jToggleButtonMAJ;
    // End of variables declaration//GEN-END:variables

    TableModelListener listener;
    FenetreDeChat fenetreDeChat;
}
