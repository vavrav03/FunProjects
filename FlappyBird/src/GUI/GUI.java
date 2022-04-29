/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Engine.Engine;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class GUI extends JPanel implements KeyListener {

   protected Engine engine;

   public void setEngine(Engine engine) {
      this.engine = engine;
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   public abstract void zobrazSkore();
}
