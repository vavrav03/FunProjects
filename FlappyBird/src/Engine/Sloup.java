/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Engine;

public class Sloup {

   private int mezera;
   private int vzdalenostSloupu;
   private int sirka;
   private int engineSirkaTolerace;

   public Sloup(int mezera, int vzdalenostSloupu, int sirka) {
      this.mezera = mezera;
      this.vzdalenostSloupu = vzdalenostSloupu;
      this.sirka = sirka;
      this.engineSirkaTolerace = sirka / 16;
   }

   public int getMezera() {
      return mezera;
   }

   public int getVzdalenostSloupu() {
      return vzdalenostSloupu;
   }

   public int getSirka() {
      return sirka;
   }

   public int getEngineSirkaTolerace() {
      return engineSirkaTolerace;
   }

}
