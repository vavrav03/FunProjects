/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tetris;

import java.util.Random;

public class Logika {

   private Blok[] prvniBloky;
   private int[][] aktualniTvar;
   private Random random;
   private boolean prohrano;
   private int skore;
   private int rychlost;
   private int[] poloha;
   private int level;

   public Logika() {
      random = new Random();
      prvniBloky = new Blok[2];
      prvniBloky[1] = vyberNahodny();
      rychlost = 1000;
      skore = 0;
      level = 1;
   }

   public Blok[] getPrvniBloky() {
      return prvniBloky;
   }

   public void novyBlok() {
      prvniBloky[0] = prvniBloky[1];
      prvniBloky[1] = vyberNahodny();
      aktualniTvar = prvniBloky[0].getTvar();
   }

   public boolean isProhrano() {
      return prohrano;
   }

   public int getSkore() {
      return skore;
   }

   public void setSkore(int skore) {
      this.skore = skore;
   }

   public final Blok vyberNahodny() {
      return Blok.values()[random.nextInt(Blok.values().length)];
   }

   public int getRychlost() {
      return rychlost;
   }

   public void setRychlost(int rychlost) {
      this.rychlost = rychlost;
   }

   public int[] getPoloha() {
      return poloha;
   }

   public void setPoloha(int[] poloha) {
      this.poloha = poloha;
   }

   public void otocTvar(boolean poSmeru) {
      aktualniTvar = prvniBloky[0].getOtoceny(aktualniTvar, poSmeru);
   }

   public int[][] getAktualniTvar() {
      return aktualniTvar;
   }

   public int getLevel() {
      return level;
   }

   public void setLevel(int level) {
      this.level = level;
   }
}