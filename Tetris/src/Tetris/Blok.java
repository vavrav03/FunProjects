/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tetris;

import java.awt.Color;

public enum Blok {
   I_BLOK(new int[][] { { 0, 0, 0, 0 },
         { 1, 1, 1, 1 },
         { 0, 0, 0, 0 },
         { 0, 0, 0, 0 } }, Color.BLUE.brighter()),
   J_BLOK(new int[][] { { 1, 0, 0 },
         { 1, 1, 1 },
         { 0, 0, 0 } }, Color.BLUE.darker()),
   L_BLOK(new int[][] { { 0, 0, 1 },
         { 1, 1, 1 },
         { 0, 0, 0 } }, Color.ORANGE),
   O_BLOK(new int[][] { { 1, 1 },
         { 1, 1 } }, Color.YELLOW),
   S_BLOK(new int[][] { { 0, 1, 1 },
         { 1, 1, 0 },
         { 0, 0, 0 } }, Color.GREEN),
   T_BLOK(new int[][] { { 0, 1, 0 },
         { 1, 1, 1 },
         { 0, 0, 0 } }, new Color(128, 0, 128)),
   Z_BLOK(new int[][] { { 1, 1, 0 },
         { 0, 1, 1 },
         { 0, 0, 0 } }, Color.CYAN);

   private int[][] tvar;
   private Color barva;

   private Blok(int[][] tvar, Color barva) {
      this.tvar = tvar;
      this.barva = barva;
   }

   public int[][] getTvar() {
      return tvar;
   }

   public int[][] getOtoceny(int[][] tvar, boolean poSmeru) {
      int poSmeruInt = poSmeru ? tvar[0].length - 1 : 0;
      int protiSmeruInt = poSmeru ? 0 : tvar[0].length - 1;
      int[][] otoceny = new int[tvar.length][tvar[0].length];
      for (int i = 0; i < tvar.length; i++) {
         for (int j = 0; j < tvar[i].length; j++) {
            otoceny[i][j] = tvar[Math.abs(poSmeruInt - j)][Math.abs(protiSmeruInt - i)];
         }
      }
      return otoceny;
   }

   public Color getBarva() {
      return barva;
   }
}