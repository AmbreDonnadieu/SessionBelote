package belote.joueur;

import cartes.Carte;
import cartes.CouleurCarte;
import graphisme.FenetrePartieReseau;

public class JoueurReseau extends JoueurIA {
	
    FenetrePartieReseau reseau;

    public JoueurReseau(String nom0, int ordre0, FenetrePartieReseau reseau0) {
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
        
        for ( Carte c : carteEnMain)
            if ( (c.getCouleur().toInt()==color) && (c.getValeur().toInt()==face)) 
                r = c;

        carteEnMain.remove(r);
        return r;
    }

}
