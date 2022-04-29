/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Engine;

public class Bird {

   private int y;
   private int g;
   private int rychlost;
   private int rychlostSkoku;
   private int velikost;
   private long posledniCas;
   private int birdPolomerNaDruhou;
   private int posouvaciKonstanta;

   public Bird(int y, int g, int vyskaPtaka, int rychlostSkoku, int posouvaciKonstanta) {
      this.y = y;
      this.g = g;
      this.rychlostSkoku = rychlostSkoku;
      this.velikost = vyskaPtaka;
      this.birdPolomerNaDruhou = (int) Math.pow(velikost / 2, 2);
      this.rychlost = rychlostSkoku;
      this.posouvaciKonstanta = posouvaciKonstanta;
   }

   public Bird(int vyskaLetu) {
      reset(vyskaLetu);
   }

   public void reset(int vyskaLetu) {
      this.y = vyskaLetu / 2;
      this.g = 2;
      this.velikost = 4000;
      this.birdPolomerNaDruhou = (int) Math.pow(velikost / 2, 2);
      this.rychlostSkoku = 600;
      this.posouvaciKonstanta = 250;
   }

   public int getY() {
      return y;
   }

   public int getVelikost() {
      return velikost;
   }

   public int getPosouvaciKonstanta() {
      return posouvaciKonstanta;
   }

   public int getBirdPolomerNaDruhou() {
      return birdPolomerNaDruhou;
   }

   public void start() {
      this.posledniCas = System.currentTimeMillis();
      skok();
   }

   public void skok() {
      rychlost = rychlostSkoku;
      dopredu();
   }

   public void dopredu() {
      this.y += rychlost;
      long cas = System.currentTimeMillis();
      rychlost -= g * (cas - posledniCas);
      this.posledniCas = cas;
   }
}
