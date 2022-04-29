/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Grafika extends GUI {

   private BufferedImage pozadi;
   private BufferedImage[] ptaci;
   private BufferedImage zem;
   private BufferedImage sloup;
   private BufferedImage sloupOtoceny;
   private JLabel label;
   private int pocitadloPosunPozadi;
   private int ptakPocitadlo;
   private double pomerStranZem;
   private double pomerStranPozadi;
   private double pomerVzdalenosti;

   public static void main(String[] args) {
      Grafika g = new Grafika();
      g.vlozNaJFrame();
   }

   public Grafika() {
      try {
         this.pozadi = ImageIO.read(new File("obrazky" + File.separator + "background-day.png"));
         this.ptaci = new BufferedImage[] {
               ImageIO.read(new File("obrazky" + File.separator + "yellowbird-upflap.png")),
               ImageIO.read(new File("obrazky" + File.separator + "yellowbird-midflap.png")),
               ImageIO.read(new File("obrazky" + File.separator + "yellowbird-downflap.png")) };
         this.zem = ImageIO.read(new File("obrazky" + File.separator + "base.png"));
         this.pomerStranPozadi = (double) pozadi.getWidth() / pozadi.getHeight();
         this.pomerStranZem = (double) zem.getWidth() / zem.getHeight();
         this.sloup = ImageIO.read(new File("obrazky" + File.separator + "pipe-green.png"));
         this.sloupOtoceny = ImageIO.read(new File("obrazky" + File.separator + "pipe-green-otoceny.png"));
         this.add(skoreLabel());
      } catch (IOException ex) {
      }
   }

   public void setPomerVzdalenosti(double pomerVzdalenosti) {
      this.pomerVzdalenosti = pomerVzdalenosti;
   }

   public void vlozNaJFrame() {
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(this);
      frame.setMinimumSize(new Dimension(pozadi.getWidth() + 18, pozadi.getHeight() + 47));
      frame.setVisible(true);
      frame.setTitle("FlappyBird");
      frame.addKeyListener(this);
   }

   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      double pomer = this.getHeight() / (double) pozadi.getHeight();
      int vyskaZeme = (int) (engine.getVyskaZeme() * pomerVzdalenosti * pomer);
      int prvniVzdalenost = (int) (engine.getPrvniSloupVzdalenost() * pomer * pomerVzdalenosti);
      paintPozadi(g2, pomer, vyskaZeme);
      paintSloupy(g2, pomer, prvniVzdalenost);
      paintZem(g2, pomer, vyskaZeme, prvniVzdalenost);
      paintPtak(g2, pomer);
   }

   private void paintPozadi(Graphics2D g, double pomer, int vyskaZeme) {
      int vyska = (int) (this.getHeight() - vyskaZeme);
      int sirka = (int) (pomerStranPozadi * vyska);
      pocitadloPosunPozadi++;
      int posunDoleva = (int) ((pocitadloPosunPozadi / 8 * vyska / (double) pozadi.getHeight()));
      if (posunDoleva >= sirka) {
         pocitadloPosunPozadi = 0;
      }
      for (int i = 0; i < 10; i++) {
         g.drawImage(pozadi, i * sirka - posunDoleva, 0, sirka, vyska, null);
      }
   }

   private void paintZem(Graphics2D g, double pomer, int vyskaZeme, int prvniVzdalenost) {
      int vyska = (int) (pomer * zem.getHeight());
      int sirka = (int) (pomerStranZem * vyska);
      if (prvniVzdalenost - sirka > 0) {
         g.drawImage(zem, prvniVzdalenost - 2 * sirka, (int) (this.getHeight() - vyskaZeme), sirka, vyska, null);
      }
      for (int i = -1; i < 6; i++) {
         g.drawImage(zem, prvniVzdalenost + i * sirka, (int) (this.getHeight() - vyskaZeme), sirka, vyska, null);
      }
   }

   private void paintPtak(Graphics2D g, double pomer) {
      int velikost = (int) (engine.getBird().getVelikost() * pomerVzdalenosti * pomer);
      ptakPocitadlo++;
      if (ptakPocitadlo == 23) {
         ptakPocitadlo = 0;
      }
      g.drawImage(ptaci[ptakPocitadlo / 8], (int) (50 * pomer),
            (int) (this.getHeight() - engine.getY() * pomerVzdalenosti * pomer), velikost, velikost, null);
   }

   private void paintSloupy(Graphics2D g, double pomer, int prvniVzdalenost) {
      int[] sloupy = engine.getSloupy();
      int vzdalenost = (int) (pomerVzdalenosti * pomer * engine.getSloup().getVzdalenostSloupu());
      int maxVyska = (int) (sloup.getHeight() * pomer);
      int mezera = (int) (engine.getSloup().getMezera() * pomer * pomerVzdalenosti);
      int sirka = (int) (sloup.getWidth() * pomer);
      int posunutaVyska;
      for (int i = 0; i < sloupy.length; i++) {
         posunutaVyska = this.getHeight() - (int) (sloupy[i] * pomer * pomerVzdalenosti);
         g.drawImage(sloupOtoceny, prvniVzdalenost + i * vzdalenost, posunutaVyska - maxVyska - mezera, sirka,
               (int) (maxVyska), null);
         g.drawImage(sloup, prvniVzdalenost + i * vzdalenost, posunutaVyska, sirka, (int) (maxVyska), null);
      }
   }

   private JLabel skoreLabel() {
      label = new JLabel();
      label.setFont(new Font("Calibri", Font.BOLD, 40));
      label.setForeground(Color.BLACK);
      label.setText("0");
      return label;
   }

   @Override
   public void zobrazSkore() {
      label.setText(String.valueOf(engine.getSkore()));
   }

   @Override
   public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
         engine.skok();
      } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
         engine.reset();
      }
   }
}
