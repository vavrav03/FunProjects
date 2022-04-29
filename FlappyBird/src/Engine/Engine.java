/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Engine;

import GUI.GUI;
import GUI.Grafika;
import java.util.Arrays;
import java.util.Random;

public class Engine {

   private int prvniSloupVzdalenost;
   private int skore;
   private int vyska;
   private int[] sloupy;
   private Sloup sloup;
   private Bird bird;
   private GUI g;
   private int vyskaZeme;
   private Random random = new Random();
   private boolean muzuPridatSkore = true;
   private boolean hrajeSe;
   private boolean prohral = false;

   public static void main(String[] args) {
      Engine engine = Engine.spustHru();
   }

   public Engine(Sloup sloup, int prvniSloupVzdalenost, int vyska, int vyskaZeme, Bird bird, GUI g) {
      this.sloup = sloup;
      this.prvniSloupVzdalenost = prvniSloupVzdalenost;
      this.bird = bird;
      this.vyska = vyska;
      this.vyskaZeme = vyskaZeme;
      this.g = g;
      this.g.setEngine(this);
      sloupy = new int[10];
      for (int i = 0; i < sloupy.length; i++) {
         sloupy[i] = novySloupVyska();
      }
      Thread t = new Thread(() -> {
         hrat();
      });
      t.start();
   }

   public static Engine spustHru() {
      Grafika g = new Grafika();
      g.vlozNaJFrame();
      int vyska = 50_000;
      Engine e = new Engine(new Sloup(13_000, 30_000, 5000), 40_000, vyska, 8000, new Bird(vyska), g);
      g.setPomerVzdalenosti((double) g.getHeight() / e.vyska);
      return e;
   }

   public int getSkore() {
      return skore;
   }

   public int[] getSloupy() {
      return sloupy;
   }

   public Sloup getSloup() {
      return sloup;
   }

   public int getPrvniSloupVzdalenost() {
      return prvniSloupVzdalenost;
   }

   public Bird getBird() {
      return bird;
   }

   public int getVyskaZeme() {
      return vyskaZeme;
   }

   private void novySloup() {
      muzuPridatSkore = true;
      prvniSloupVzdalenost += sloup.getVzdalenostSloupu();
      for (int i = 1; i < sloupy.length; i++) {
         sloupy[i - 1] = sloupy[i];
      }
      sloupy[sloupy.length - 1] = novySloupVyska();
   }

   private int novySloupVyska() {
      return random.nextInt(vyska - vyskaZeme - sloup.getMezera()) + vyskaZeme;
   }

   public void hrat() {
      while (true) {
         try {
            Thread.sleep(14);
            if (hrajeSe) {
               if (bird.getY() - bird.getVelikost() <= vyskaZeme || narazilDoSloupu()) {
                  hrajeSe = false;
                  prohral = true;
               }
               prvniSloupVzdalenost -= bird.getPosouvaciKonstanta();
               if (prvniSloupVzdalenost <= sloup.getSirka() && muzuPridatSkore) {
                  skore++;
                  g.zobrazSkore();
                  muzuPridatSkore = false;
               }
               if (prvniSloupVzdalenost < -sloup.getSirka()) {
                  novySloup();
               }
               bird.dopredu();
            }
         } catch (InterruptedException ex) {
         }
         g.repaint();
      }
   }

   public void skok() {
      if (prohral) {
         return;
      }
      if (!hrajeSe) {
         hrajeSe = true;
         bird.start();
      }
      bird.skok();
   }

   public void reset() {
      skore = 0;
      prohral = false;
      bird.reset(vyska);
      this.prvniSloupVzdalenost = 40_000;
      g.zobrazSkore();
      skok();
   }

   private boolean narazilDoSloupu() {
      if (prvniSloupVzdalenost < 0) {
         return false;
      }
      int prvniX = prvniSloupVzdalenost - sloup.getSirka();
      if (prvniX + sloup.getEngineSirkaTolerace() > bird.getVelikost()) {
         return false;
      }
      int birdStredX = bird.getVelikost() / 2;
      int birdStredY = bird.getY() - bird.getVelikost() / 2;

      int horniY = sloupy[0] + sloup.getMezera();
      if (birdStredX + sloup.getSirka() < prvniSloupVzdalenost && bird.getY() - bird.getVelikost() / 2 < horniY
            && bird.getY() - bird.getVelikost() / 2 > sloupy[0]) { // přední rohy sloupů
         if (Math.pow(prvniX - birdStredX, 2) + Math.pow(horniY - birdStredY, 2) <= bird.getBirdPolomerNaDruhou()) {
            return true;
         }
         if (Math.pow(prvniX - birdStredX, 2) + Math.pow(sloupy[0] - birdStredY, 2) <= bird.getBirdPolomerNaDruhou()) {
            return true;
         }
      } else if (prvniSloupVzdalenost < bird.getVelikost() / 2) { // zadní rohy sloupů
         if (Math.pow(prvniSloupVzdalenost - birdStredX, 2) + Math.pow(horniY - birdStredY, 2) <= bird
               .getBirdPolomerNaDruhou()) {
            return true;
         }
         if (Math.pow(prvniSloupVzdalenost - birdStredX, 2) + Math.pow(sloupy[0] - birdStredY, 2) <= bird
               .getBirdPolomerNaDruhou()) {
            return true;
         }
      } else { // sloupy
         if (bird.getY() > horniY) {
            return true;
         }
         if (bird.getY() - bird.getVelikost() < sloupy[0]) {
            return true;
         }
      }
      return false;
   }

   public int getY() {
      return bird.getY();
   }
}
