/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Hra extends JPanel implements KeyListener {

   public static void main(String[] args) {
      Hra g = new Hra();
   }

   private Policko[][] policka;
   private JPanel dalsiObrazecPanel;
   private Logika logika;
   private final Color volno;
   private JLabel[] informace;
   private static final int[] skorePole = { 100, 400, 900, 2000 };

   public Hra() {
      logika = new Logika();
      volno = Color.BLACK;
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
      this.add(vytvorTabulkuVlevo(), gbc);
      gbc.weightx = 4;
      gbc.gridx = 1;
      this.add(vytvorHraciPole(), gbc);
      vytvorJFrame();
      hrat();
   }

   public class Policko extends JLabel {

      private Color color;
      private boolean napevno;

      public Policko() {
         this.setOpaque(true);
         this.setBackground(volno);
         this.color = volno;
         this.napevno = false;
      }

      @Override
      public void setBackground(Color color) {
         this.color = color;
         super.setBackground(color);
      }

      public boolean jeVolno() {
         return color.equals(Color.BLACK);
      }

      public boolean isNapevno() {
         return napevno;
      }

      public void setNapevno(boolean napevno) {
         this.napevno = napevno;
      }
   }

   private void hrat() {
      zobrazDalsi();
      while (true) {
         hraciMechanismus();
         for (int i = 0; i < policka.length; i++) {
            for (int j = 0; j < policka[i].length; j++) {
               policka[i][j].setBackground(Color.RED);
               policka[i][j].setNapevno(true);
            }
         }
      }
   }

   private void hraciMechanismus() {
      while (true) {
         try {
            Thread.sleep(logika.getRychlost());
            if (!posun(logika.getAktualniTvar(), logika.getAktualniTvar(), 0, 1)) {
               for (int i = 0; i < logika.getAktualniTvar().length; i++) {
                  for (int j = 0; j < logika.getAktualniTvar()[i].length; j++) {
                     if (logika.getAktualniTvar()[i][j] != 0) {
                        if (logika.getPoloha()[0] + i <= 0) {
                           return;
                        }
                        policka[i + logika.getPoloha()[0]][j + logika.getPoloha()[1]].setNapevno(true);
                     }
                  }
               }
               vymazRadek(radekNaVymaz());
               zobrazDalsi();
            }
         } catch (InterruptedException ex) {
         }
      }
   }

   private void zobrazDalsi() {
      dalsiObrazecPanel.removeAll();
      logika.novyBlok();
      int[][] policko = logika.getPrvniBloky()[1].getTvar();
      dalsiObrazecPanel.setLayout(new GridLayout(policko.length, policko[0].length, 2, 2));
      for (int i = 0; i < policko.length; i++) {
         for (int j = 0; j < policko[i].length; j++) {
            Policko novePolicko = new Policko();
            novePolicko.setBackground(policko[i][j] == 0 ? volno : logika.getPrvniBloky()[1].getBarva());
            dalsiObrazecPanel.add(novePolicko);
         }
      }
      dalsiObrazecPanel.revalidate();
      dalsiObrazecPanel.repaint();
      logika.setPoloha(new int[] { 0, policka[0].length / 2 - logika.getAktualniTvar().length / 2 });
      vyplnAktualnimBlokem(logika.getAktualniTvar());
   }

   private void vymazRadek(ArrayList<Integer> mazaneRadky) {
      if (!mazaneRadky.isEmpty()) {
         int indexVSkorePoli = 1;
         if (logika.getLevel() >= 2 && logika.getLevel() <= 3) {
            indexVSkorePoli = 2;
         } else if (logika.getLevel() >= 4 && logika.getLevel() <= 5) {
            indexVSkorePoli = 3;
         } else if (logika.getLevel() >= 6 && logika.getLevel() <= 7) {
            indexVSkorePoli = 4;
         } else if (logika.getLevel() >= 8) {
            indexVSkorePoli = 5;
         }
         logika.setSkore(logika.getSkore() + skorePole[mazaneRadky.size() - 1] * indexVSkorePoli);
         informace[1].setText("<html>SKORE: <br>" + logika.getSkore() + "</html>");
         for (int mazanyRadek : mazaneRadky) {
            for (int i = mazanyRadek - 1; i >= 0; i--) {
               for (int j = 0; j < policka[0].length; j++) {
                  policka[i + 1][j].setNapevno(policka[i][j].isNapevno());
                  policka[i + 1][j].setBackground(policka[i][j].getBackground());
               }
            }
         }
      }
   }

   private ArrayList<Integer> radekNaVymaz() {
      ArrayList<Integer> mazaneRadky = new ArrayList();
      for (int i = logika.getPoloha()[0]; i < policka.length; i++) {
         if (radekJePlny(i)) {
            mazaneRadky.add(i);
         }
      }
      return mazaneRadky;
   }

   private boolean radekJePlny(int radek) {
      for (int i = 0; i < policka[0].length; i++) {
         if (!policka[radek][i].isNapevno()) {
            return false;
         }
      }
      return true;
   }

   private void vyplnAktualnimBlokem(int[][] tvar) {
      for (int i = 0; i < tvar.length; i++) {
         for (int j = 0; j < tvar[i].length; j++) {
            try {
               if (!policka[i + logika.getPoloha()[0]][j + logika.getPoloha()[1]].isNapevno()) {
                  policka[i + logika.getPoloha()[0]][j + logika.getPoloha()[1]]
                        .setBackground(tvar[i][j] == 0 ? volno : logika.getPrvniBloky()[0].getBarva());
               }
            } catch (Exception e) {
            }
         }
      }
   }

   private boolean posun(int[][] tvar, int[][] druhyTvar, int doStran, int dolu) {
      if (jeValidni(tvar, druhyTvar, logika.getPoloha(),
            new int[] { logika.getPoloha()[0] + dolu, logika.getPoloha()[1] + doStran })) {
         vyplnAktualnimBlokem(new int[tvar.length][tvar[0].length]);
         logika.setPoloha(new int[] { logika.getPoloha()[0] + dolu, logika.getPoloha()[1] + doStran });
         vyplnAktualnimBlokem(druhyTvar);
         return true;
      }
      return false;
   }

   private boolean jeValidni(int[][] tvar, int[][] druhyTvar, int[] prvniPozice, int[] druhaPozice) {
      ArrayList<Integer> prvniBody = new ArrayList();
      ArrayList<Integer> druheBody = new ArrayList();
      for (int i = 0; i < tvar.length; i++) {
         for (int j = 0; j < tvar[0].length; j++) {
            if (tvar[i][j] != 0) {
               prvniBody.add((i + prvniPozice[0]) * policka.length + j + prvniPozice[1]);
            }
            if (druhyTvar[i][j] != 0) {
               druheBody.add((i + druhaPozice[0]) * policka.length + j + druhaPozice[1]);
            }
         }
      }
      druheBody.removeAll(prvniBody);
      try {
         for (int bod : druheBody) {
            if (!policka[bod / policka.length][bod % policka.length].jeVolno()
                  && policka[bod / policka.length][bod % policka.length].isNapevno()) {
               return false;
            }
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         return false;
      }
      return true;
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
         posun(logika.getAktualniTvar(), logika.getAktualniTvar(), -1, 0);
      } else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
         posun(logika.getAktualniTvar(), logika.getAktualniTvar(), 1, 0);
      } else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
         posun(logika.getAktualniTvar(), logika.getAktualniTvar(), 0, 1);
      } else if (e.getKeyCode() == KeyEvent.VK_Q || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
         int[][] prvniTvar = logika.getAktualniTvar();
         logika.otocTvar(false);
         if (!posun(prvniTvar, logika.getAktualniTvar(), 0, 0)) {
            logika.otocTvar(true);
         }
      } else if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
         int[][] prvniTvar = logika.getAktualniTvar();
         logika.otocTvar(true);
         if (!posun(prvniTvar, logika.getAktualniTvar(), 0, 0)) {
            logika.otocTvar(false);
         }
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {

   }

   private JPanel vytvorHraciPole() {
      JPanel hraciPole = new JPanel();
      hraciPole.setPreferredSize(this.getPreferredSize());
      hraciPole.setBackground(Color.GRAY);
      this.policka = new Policko[20][10];
      hraciPole.setLayout(new GridLayout(policka.length, policka[0].length, 2, 2));
      for (int i = 0; i < policka.length; i++) {
         for (int j = 0; j < policka[i].length; j++) {
            policka[i][j] = new Policko();
            hraciPole.add(policka[i][j]);
         }
      }
      return hraciPole;
   }

   private JPanel vytvorTabulkuVlevo() {
      JPanel vlevoTabule = new JPanel();
      vlevoTabule.setPreferredSize(this.getPreferredSize());
      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 2.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 10, 10);
      vlevoTabule.setLayout(new GridBagLayout());
      JButton novaHra = new JButton("<html>N<br>O<br>V<br>Á<br><br>H<br>R<br>A</html>");
      novaHra.setPreferredSize(this.getPreferredSize());
      novaHra.setFont(new Font("Helvetica", Font.BOLD, 27));
      novaHra.setBackground(Color.GRAY);
      novaHra.setForeground(Color.BLACK);
      novaHra.addActionListener((ActionEvent e) -> {
         logika.setRychlost(1000 - logika.getLevel() * 85);
         for (int i = 0; i < policka.length; i++) {
            for (int j = 0; j < policka[i].length; j++) {
               policka[i][j].setBackground(volno);
               policka[i][j].setNapevno(false);
            }
         }
         logika.setSkore(0);
         informace[1].setText("SKÓRE: " + logika.getSkore());
         logika.novyBlok();
         zobrazDalsi();
      });
      novaHra.setFocusable(false);
      vlevoTabule.add(novaHra, gbc);

      gbc.weighty = 0.6;
      gbc.gridy = 1;
      dalsiObrazecPanel = new JPanel();
      dalsiObrazecPanel.setBackground(volno);
      dalsiObrazecPanel.setPreferredSize(this.getPreferredSize());
      vlevoTabule.add(dalsiObrazecPanel, gbc);

      gbc.weighty = 0.5;
      informace = new JLabel[2];
      for (int i = 0; i < informace.length; i++) {
         informace[i] = new JLabel();
         informace[i].setOpaque(true);
         informace[i].setPreferredSize(this.getPreferredSize());
         informace[i].setFont(new Font("Helvetica", Font.BOLD, 18));
         informace[i].setForeground(Color.BLACK);
         informace[i].setVerticalAlignment(SwingUtilities.CENTER);
         informace[i].setHorizontalAlignment(SwingUtilities.CENTER);
         informace[i].setFocusable(false);
         informace[i].setHorizontalAlignment(JLabel.CENTER);
         informace[i].setVerticalAlignment(JLabel.CENTER);
         informace[i].setHorizontalTextPosition(JLabel.CENTER);
         informace[i].setHorizontalAlignment(JLabel.CENTER);
         gbc.gridy = i + 2;
         vlevoTabule.add(informace[i], gbc);
      }
      informace[0].setText("LEVEL: " + logika.getLevel());
      informace[1].setText("SKÓRE: " + logika.getSkore());
      JButton[] plusMinusLevel = new JButton[2];

      for (int i = 0; i < plusMinusLevel.length; i++) {
         plusMinusLevel[i] = new JButton();
         plusMinusLevel[i].setFont(new Font("Helvetica", Font.BOLD, 20));
         plusMinusLevel[i].setPreferredSize(this.getPreferredSize());
         plusMinusLevel[i].setMargin(new Insets(0, 0, 0, 0));
         plusMinusLevel[i].setFocusable(false);
         gbc.gridy = 4 + i;
         vlevoTabule.add(plusMinusLevel[i], gbc);
      }
      plusMinusLevel[0].setText("+LEVEL");
      plusMinusLevel[0].setBackground(Color.GREEN.darker());
      plusMinusLevel[0].addActionListener((ActionEvent e) -> {
         if (logika.getLevel() < 10) {
            logika.setLevel(logika.getLevel() + 1);
            informace[0].setText("LEVEL: " + logika.getLevel());
         }
      });
      plusMinusLevel[1].setText("-LEVEL");
      plusMinusLevel[1].setBackground(Color.RED.darker());
      plusMinusLevel[1].addActionListener((ActionEvent e) -> {
         if (logika.getLevel() > 1) {
            logika.setLevel(logika.getLevel() - 1);
            informace[0].setText("LEVEL: " + logika.getLevel());
         }
      });
      return vlevoTabule;
   }

   private void vytvorJFrame() {
      JFrame frame = new JFrame();
      frame.setLayout(new GridLayout());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setMinimumSize(new Dimension(500, 800));
      frame.getContentPane().add(this);
      frame.setVisible(true);
      frame.addKeyListener(this);
      frame.setFocusable(true);
   }
}