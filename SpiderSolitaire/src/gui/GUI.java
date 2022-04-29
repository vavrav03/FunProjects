package gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import processing.ProcessingUnit;
import processing.ProcessingUnitNormal;
import processing.StoringManager;

/** Účelem třídy je zajištění správného umístění panelu se hrou v JFramu Zároveň zodpovídá za věci mimo hrací plochu - týká se např. menu JFrame obsahuje GUI JPanel a ten obsahuje HraciPlochu. GUI JPanel je rostažen po celém JFramu a PlayingField je poté umístěna componentListenerem. Je to kvůli malování pozadí, kdyby GUI byl samotný JFrame, tak by to z nějakého důvodu nefungovalo.
 */
public class GUI extends JPanel {

   private Image background;
   private PlayingField hp;

   public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
         @Override
         public void run() {
            JFrame frame = new JFrame("Spider solitaire");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GUI g = new GUI(ProcessingUnitNormal.newGame(4));
            frame.add(g);
            frame.pack();
            frame.getContentPane().setLayout(new GridLayout());

            JMenuBar menuBar = g.createMenu(frame);

            frame.setJMenuBar(menuBar);
            frame.setVisible(true);
         }
      });
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PlayingField.BASE_WIDTH, PlayingField.BASE_HEIGHT);
   }

   public JMenuBar createMenu(JFrame frame) {
      JMenu currentGameMenu = new JMenu("Aktuální hra");
      JMenuItem restartItem = new JMenuItem(new AbstractAction("Restart") {
         public void actionPerformed(ActionEvent ae) {
            int dialogResult = JOptionPane.showConfirmDialog(null, "Chcete restartovat aktuální hru?", "Varování",
                  JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
               hp.resetGame();
            }
         }
      });
      currentGameMenu.add(restartItem);
      JMenuItem storeItem = new JMenuItem(new AbstractAction("Uložit") {
         public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(StoringManager.STORED_GAMES_PATH));
            chooser.setSelectedFile(new File(""));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            // chooser.setAcceptAllFileFilterUsed(false);
            chooser.showSaveDialog(frame);
            hp.storeState(chooser.getSelectedFile());
         }
      });
      currentGameMenu.add(storeItem);

      JMenu newGameMenu = new JMenu("Nová hra");
      JMenuItem oneColorItem = new JMenuItem(new AbstractAction("1 barva") {
         public void actionPerformed(ActionEvent ae) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                  "Chcete ukončit aktuální hru a začít novou s 1 barvou", "Varování",
                  JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
               hp.switchProcessingUnit(ProcessingUnitNormal.newGame(1));
            }
         }
      });
      newGameMenu.add(oneColorItem);
      JMenuItem twoColorsItem = new JMenuItem(new AbstractAction("2 barvy") {
         public void actionPerformed(ActionEvent ae) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                  "Chcete ukončit aktuální hru a začít novou s 2 colormi", "Varování",
                  JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
               hp.switchProcessingUnit(ProcessingUnitNormal.newGame(2));
            }
         }
      });
      newGameMenu.add(twoColorsItem);
      JMenuItem fourColorsItem = new JMenuItem(new AbstractAction("4 barvy") {
         public void actionPerformed(ActionEvent ae) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                  "Chcete ukončit aktuální hru a začít novou se 4 colormi?", "Varování",
                  JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
               hp.switchProcessingUnit(ProcessingUnitNormal.newGame(4));
            }
         }
      });
      newGameMenu.add(fourColorsItem);
      JMenuItem storedGame = new JMenuItem(new AbstractAction("Načíst uloženou hru") {
         public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(StoringManager.STORED_GAMES_PATH));
            chooser.setSelectedFile(new File(""));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            // chooser.setAcceptAllFileFilterUsed(false);
            chooser.showOpenDialog(frame);
            hp.switchProcessingUnit(ProcessingUnitNormal.nactiHru(chooser.getSelectedFile()));
         }
      });
      newGameMenu.add(storedGame);

      JMenu controlMenu = new JMenu("Ovládání");
      JMenuItem undoItem = new JMenuItem(new AbstractAction("Zpět (z)") {
         public void actionPerformed(ActionEvent ae) {
            hp.undo();
         }
      });
      controlMenu.add(undoItem);
      JMenuItem redoItem = new JMenuItem(new AbstractAction("Vpřed (v)") {
         public void actionPerformed(ActionEvent ae) {
            hp.redo();
         }
      });
      controlMenu.add(redoItem);
      JMenuItem napovedaItem = new JMenuItem(new AbstractAction("Nápověda (h)") {
         public void actionPerformed(ActionEvent ae) {
            hp.displayHint();

         }
      });
      controlMenu.add(napovedaItem);

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(currentGameMenu);
      menuBar.add(newGameMenu);
      menuBar.add(controlMenu);
      return menuBar;
   }

   public GUI(ProcessingUnit rj) {
      this.background = new ImageIcon("img" + File.separator + "background.jpg").getImage();
      this.hp = new PlayingField(rj);
      GUI.this.add(hp);
      GUI.this.setLayout(null);
      GUI.this.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            int setWidthOfBoard;
            if (GUI.this.getHeight() * PlayingField.BASE_SIZE_RATIO < GUI.this.getWidth()) {
               setWidthOfBoard = (int) (GUI.this.getHeight()
                     * PlayingField.BASE_SIZE_RATIO);
            } else {
               setWidthOfBoard = GUI.this.getWidth();
            }
            hp.setBounds((GUI.this.getWidth() - setWidthOfBoard) / 2,
                  0,
                  setWidthOfBoard,
                  GUI.this.getHeight());
            GUI.this.revalidate();
         }
      });
   }

   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(background, 0, 0, 1920, 1080, null);
   }
}