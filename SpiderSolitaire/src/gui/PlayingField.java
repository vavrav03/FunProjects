package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import processing.Card;
import processing.MoveManager;
import processing.ProcessingUnit;
import processing.StoringManager;
import processing.MoveManager.CardMove;
import processing.MoveManager.Move;
import processing.MoveManager.NewDeckMove;

public class PlayingField extends JPanel {

   private ProcessingUnit rj;
   private RatioHolder dp;
   private Image[][] cardImages; // jejich graficka reprezentace
   private Image cardBackImage;
   private int lastSize;

   // PROMĚNNÉ PRO MALOVÁNÍ (GUI je plně responzivní a je potřeba aktualizovat hodnoty těchto proměnných)
   public static final int BASE_HEIGHT = 720;
   public static final int BASE_WIDTH = 1280;
   public static final double BASE_SIZE_RATIO = BASE_WIDTH / (double) BASE_HEIGHT;
   public static final int BASE_PADDING = 40;
   private int currentPadding;

   public static final int BASE_HEIGHT_OF_CARD = 120; // 500x726
   public static final int BASE_WIDTH_OF_CARD = (int) (500.0 / 726.0 * BASE_HEIGHT_OF_CARD);
   public static final int BASE_RADIUS_OF_CARD_EDGE = 5;
   private int currentCardWidth;
   private int currentCardHeight;
   private int currentCardEdgeRadius;

   public static final int BASE_DISTANCE_OF_HIDDEN_CARDS = 10;
   public static final int BASE_DISTANCE_OF_VISIBLE_CARDS = 25;
   private int currentDistanceOfHiddenCards;
   private int currentDistanceBetweenColumns;
   private int currentEdgeColumnMargin;
   private int currentMaxHeightOfVisibleCards; // výška viditelných karet v pixelech - výška cards
   private int currentMaxHeightOfVisibleCardsInColumn;
   private int[] distanceOfVisibleCardsInColumn; // jako proměnná je to zde proto, že se používá v paintKarty i mousePressed
   private Point leftTopEdgeForNewSets;

   private int bottomChosenCardColumn = -1;
   private int bottomChosenCardColumnIndexBottom = -1;

   private int unsuccessfulIndex;
   private Move highlightedMove;

   private static final Color ORANGE = new Color(255, 139, 40);
   private static final Color LIGHT_BLUE = new Color(3, 169, 244);

   public PlayingField(ProcessingUnit rj) {
      this.dp = new RatioHolder();
      this.setOpaque(false);
      this.rj = rj;
      this.distanceOfVisibleCardsInColumn = new int[ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD];
      for (int i = 0; i < distanceOfVisibleCardsInColumn.length; i++) {
         distanceOfVisibleCardsInColumn[i] = BASE_DISTANCE_OF_VISIBLE_CARDS;
      }
      this.vytvorObrazkyKaret();
      MouseAdapter guiMouseAdapter = new GUIMouseAdapter();
      this.addMouseListener(guiMouseAdapter);
      this.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_Z) {
               PlayingField.this.undo();
            }

            if (e.getKeyCode() == KeyEvent.VK_V) {
               PlayingField.this.redo();
            }

            if (e.getKeyCode() == KeyEvent.VK_H) {
               PlayingField.this.displayHint();
            }
         }
      });
      this.setFocusable(true);
   }

   /**
    * Metoda incializuje pole obrázků karet a otočenou kartu
    */
   private void vytvorObrazkyKaret() {
      this.cardBackImage = new ImageIcon("img/card_back.png").getImage();
      this.cardImages = new Image[Card.CardType.values().length][ProcessingUnit.NUMBER_OF_CARDS_IN_DECK];
      File imageCardsFolder = new File("img/cards");
      for (File file : imageCardsFolder.listFiles()) {
         String[] fileName = file.getName().split("\\.")[0].split("_");
         // bez přípony - split nefunguje na tečce normální, jelikož má zvláštní význam - nutnost escapování
         if (fileName.length != 2) {
            continue; // soubor s jiným formátem (např. zadek cards)
         }
         int cardNumber = Integer.parseInt(fileName[0]);
         int cardTypeNumber = Card.CardType.valueOf(fileName[1]).getNumberTypuKarty();
         cardImages[cardTypeNumber][cardNumber - 1] = new ImageIcon(file.getAbsolutePath()).getImage();
      }
   }

   @Override
   public void paintComponent(Graphics g) {
      if (this.lastSize != this.getWidth()) {
         this.lastSize = this.getWidth();
         dp.setRatio(this.getWidth() / (double) BASE_WIDTH);
         reworkDimensions();
      }
      super.paintComponent(g);
      paintColumns(g);
      paintBottomRow(g);
      paintHighlightedMove(g);
   }

   /**
    * Metoda je volána pro přepočítání rozměrů pro malování, které se mohou změnit při změně sizei okna. Je volána tedy pouze, když se size okna změní, ne při každém běhu metody paintComponent, aby se ušetřil výkon.
    */
   private void reworkDimensions() {
      this.currentPadding = dp.sizeInRatio(BASE_PADDING);

      this.currentCardHeight = dp.sizeInRatio(BASE_HEIGHT_OF_CARD);
      this.currentCardWidth = dp.sizeInRatio(BASE_WIDTH_OF_CARD);
      this.currentCardEdgeRadius = dp.sizeInRatio(BASE_RADIUS_OF_CARD_EDGE);

      this.currentDistanceOfHiddenCards = dp.sizeInRatio(BASE_DISTANCE_OF_HIDDEN_CARDS);
      int a = (this.getWidth() - 2 * this.currentPadding - ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD * this.currentCardWidth);
      this.currentDistanceBetweenColumns = a / (ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD - 1);
      this.currentEdgeColumnMargin = (a - this.currentDistanceBetweenColumns * (ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD - 1))
            / 2;
      this.currentMaxHeightOfVisibleCards = dp
            .sizeInRatio(this.getHeight() - 3 * this.currentPadding - this.currentCardHeight);
      this.currentMaxHeightOfVisibleCardsInColumn = dp
            .sizeInRatio(BASE_DISTANCE_OF_VISIBLE_CARDS);
   }

   private void paintColumns(Graphics g) {
      int numberOfHiddenCards;
      int numberOfVisibleCards;
      int x = this.currentPadding + this.currentEdgeColumnMargin;
      for (int i = 0; i < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; i++) {
         int y = this.currentPadding;
         if (rj.getColumn(i).isEmpty()) {
            drawOutline(x, y, this.currentCardWidth, this.currentCardHeight, 2, g, 2, g.getColor());
         } else {
            numberOfHiddenCards = rj.getColumn(i).getLastHiddenCardIndex() + 1;
            if (numberOfHiddenCards < 0) {
               numberOfHiddenCards = 0;
            }
            numberOfVisibleCards = rj.getColumn(i).getSize() - numberOfHiddenCards;
            if (numberOfVisibleCards
                  * this.currentMaxHeightOfVisibleCardsInColumn > this.currentMaxHeightOfVisibleCards) {
               this.distanceOfVisibleCardsInColumn[i] = dp
                     .sizeInBaseRatio(this.currentMaxHeightOfVisibleCards
                           / (numberOfVisibleCards - 1));
            }
            for (int j = 0; j < numberOfHiddenCards; j++) {
               g.drawImage(cardBackImage, x, y, this.currentCardWidth, this.currentCardHeight, null);
               y += this.currentDistanceOfHiddenCards;
            }
            for (int j = numberOfHiddenCards; j < rj.getColumn(i).getSize(); j++) {
               Card logickaCard = rj.getColumn(i).getCard(j);
               g.drawImage(cardImages[logickaCard.getType().getNumberTypuKarty()][logickaCard.getNumber() - 1], x, y,
                     this.currentCardWidth, this.currentCardHeight, null);
               y += dp.sizeInRatio(this.distanceOfVisibleCardsInColumn[i]);
            }

         }
         x += this.currentCardWidth + this.currentDistanceBetweenColumns;
      }
      if (this.bottomChosenCardColumnIndexBottom != -1) {
         drawCardsOutline(this.bottomChosenCardColumn, this.bottomChosenCardColumnIndexBottom, g,
               3, LIGHT_BLUE);
      }
   }

   private void paintHighlightedMove(Graphics g) {
      if (highlightedMove == null) {
         return;
      }
      if (this.highlightedMove instanceof NewDeckMove) {
         drawOutline(leftTopEdgeForNewSets.x, leftTopEdgeForNewSets.y, this.currentCardWidth,
               this.currentCardHeight, 2, g, 2, LIGHT_BLUE);
      } else {
         CardMove pt = (CardMove) this.highlightedMove;
         if (pt.getWonType() != null) {

         }
         this.drawCardsOutline(pt.getFrom(), pt.getFromIndexBottom(), g, 3, ORANGE);
         this.drawCardsOutline(pt.getThere(), pt.getThereIndexBottom() > 0 ? pt.getThereIndexBottom() - 1 : 0, g, 3,
               LIGHT_BLUE);
      }
   }

   /**
    * obtáhne barvou všechny cards, které se nacházejí na předané kartě včetně jí
    * @param column index sloupce spodní cards
    * @param indexBottom index cards v rámci sloupce
    * @param color color, kterou budou cards obtaženy
    */
   private void drawCardsOutline(int column, int indexBottom, Graphics g, int thickness, Color color) {
      drawOutline(
            xLeftPosCard(column),
            yTopPosCard(column, indexBottom),
            this.currentCardWidth,
            this.currentCardHeight
                  + (rj.getColumn(column).getSize() - indexBottom - 1)
                        * dp.sizeInRatio(
                              this.distanceOfVisibleCardsInColumn[column]),
            currentCardEdgeRadius, g, dp.sizeInRatio(thickness), color);

   }

   //další metody (xLeftPosCard, yTopPosCard, yTopPosFirstUncoveredCard) vrací souřadnice karet na hrací ploše na základě hodnot použitých v RidiciJednotce. Jsou potřeba pro správné zobrazení obrysů karet při nápovědě, zpět, vpřed a označení. Popř. také dělají opačný proces (columnKarty, indexBottomKarty)
   private int xLeftPosCard(int column) {
      return this.currentPadding + this.currentEdgeColumnMargin
            + (column) * this.currentCardWidth + column * this.currentDistanceBetweenColumns;
   }

   private int columnKarty(int x) {
      return (x - this.currentPadding - this.currentEdgeColumnMargin)
            / (this.currentCardWidth + this.currentDistanceBetweenColumns); // levý okraj cards v našem sloupci = padding+krajniColumnOdsazeni+k*sirkaKarty+k*mezeraMeziSloupci, odtud rovnice
   }

   private int yTopPosCard(int column, int indexBottom) {
      int yTopPosFirstUncoveredCard = yTopPosFirstUncoveredCard(column);
      return yTopPosFirstUncoveredCard
            + ((indexBottom - (rj.getColumn(column).getLastHiddenCardIndex() + 1))
                  * dp.sizeInRatio(this.distanceOfVisibleCardsInColumn[column]));
   }

   private int yTopPosFirstUncoveredCard(int column) {
      return this.currentPadding
            + (rj.getColumn(column).getLastHiddenCardIndex() + 1) * this.currentDistanceOfHiddenCards;
   }

   private int indexBottomKarty(int column, int y) {
      return (y - yTopPosFirstUncoveredCard(column)
            + (rj.getColumn(column).getLastHiddenCardIndex() + 1)
                  * dp.sizeInRatio(this.distanceOfVisibleCardsInColumn[column]))
            / dp.sizeInRatio(this.distanceOfVisibleCardsInColumn[column]); // yHorniKarty = yTopPosFirstUncoveredCard + (index - pocetSkrytych) * vzdalenostNeskrytych[column]
   }

   private void drawOutline(int x, int y, int sirka, int vyska, int polomer, Graphics g, int thickness, Color color) {
      Graphics2D g2 = (Graphics2D) g;
      Stroke oldStroke = g2.getStroke();
      Color oldColor = g2.getColor();
      g2.setStroke(new BasicStroke(thickness));
      g2.setColor(color);
      g.drawRoundRect(x, y, sirka, vyska, polomer, polomer);
      g2.setStroke(oldStroke);
      g2.setColor(oldColor);
   }

   private void paintBottomRow(Graphics g) {
      paintWonDecks(g);
      paintNewDecks(g);
   }

   private void paintWonDecks(Graphics g) {
      int x = this.currentPadding;
      int y = this.getHeight() - this.currentPadding - this.currentCardHeight;
      for (int i = 0; i < rj.getWonCards().size(); i++) {
         g.drawImage(cardImages[rj.getWonCards().get(i).getNumberTypuKarty()][ProcessingUnit.NUMBER_OF_CARDS_IN_DECK - 1], x,
               y, this.currentCardWidth, this.currentCardHeight, null);
         x += this.currentMaxHeightOfVisibleCardsInColumn;
      }
   }

   private void paintNewDecks(Graphics g) {
      int x = this.getWidth() - this.currentPadding - this.currentCardWidth;
      int y = this.getHeight() - this.currentPadding - this.currentCardHeight;
      for (int i = 0; i < rj.numberOfRemainingDecks(); i++) {
         g.drawImage(cardBackImage, x, y, this.currentCardWidth, this.currentCardHeight, null);
         x -= this.currentDistanceOfHiddenCards;
      }
      this.leftTopEdgeForNewSets = new Point(x + this.currentDistanceOfHiddenCards, y); //musíme přičíst vzdálenost skrytých karet, protože při posledním projití cyklu jme ji odečetli.
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(BASE_WIDTH, BASE_HEIGHT);
   }

   private void nullifyInfoAboutChosenCard() {
      this.bottomChosenCardColumn = -1;
      this.bottomChosenCardColumnIndexBottom = -1;
   }

   private class GUIMouseAdapter extends MouseAdapter {

      @Override
      public void mousePressed(MouseEvent e) {
         PlayingField hp = PlayingField.this;
         int x = (int) e.getX();
         int y = (int) e.getY();
         if (clickedOnNewDeck(x, y, hp)) {
            return;
         }
         int firstColumn = hp.bottomChosenCardColumn;
         int firstIndexBottom = hp.bottomChosenCardColumnIndexBottom;
         if (kliknulNaKartu(x, y, hp)) {
            if (firstColumn != -1) {
               if (rj.tryMove(firstColumn, firstIndexBottom, hp.bottomChosenCardColumn)) {
                  nullifyInfo();
                  nullifyInfoAboutChosenCard();
                  repaint();
                  checkEndgame();
                  return;
               }
               nullifyInfoAboutChosenCard();
            }
         } else {
            nullifyInfoAboutChosenCard();
         }
         nullifyInfo();
         repaint();
      }

      private boolean clickedOnNewDeck(int x, int y, PlayingField hp) {
         if (x >= leftTopEdgeForNewSets.getX()
               && x <= leftTopEdgeForNewSets.getX() + hp.currentCardWidth
               && y >= leftTopEdgeForNewSets.getY()
               && y <= leftTopEdgeForNewSets.getY() + hp.currentCardHeight) { // kontrola nové sady
            hp.pullNewDeckGUI();
            return true;
         }
         return false;
      }

      private boolean kliknulNaKartu(int x, int y, PlayingField hp) {
         if (x < hp.currentPadding + hp.currentEdgeColumnMargin
               || x > hp.getWidth() - hp.currentPadding - hp.currentEdgeColumnMargin) {
            return false; // zde nic být nemůže a hodnoty v tomto rozsahu by překážely při výpočtu sloupce. Je tedy nutné je eliminovat
         }
         int column = columnKarty(x);
         int xLeftPosWantedCard = xLeftPosCard(column);
         if (x < xLeftPosWantedCard + hp.currentCardWidth) { // je možné, že jsme klikli do mezery napravo od sloupce. Tato podmínka to vylučuje.
            int yTopPosFirstUncoveredCard = yTopPosFirstUncoveredCard(column);
            if (y < yTopPosFirstUncoveredCard) { // uživatel klikl na skrytou kartu, nebo mimo padding
               return false;
            }
            int indexBottom = indexBottomKarty(column, y);
            if (indexBottom >= rj.getColumn(column).getSize()) {
               indexBottom = rj.getColumn(column).getSize() - 1;
            }
            if (rj.getColumn(column).isEmpty()) {
               indexBottom = 0;
            }
            int yTopPosWantedCard = yTopPosCard(column, indexBottom);
            if (y < yTopPosWantedCard + hp.currentCardHeight
                  && rj.getColumn(column).isMovable(indexBottom)) {

               hp.bottomChosenCardColumn = column;
               hp.bottomChosenCardColumnIndexBottom = indexBottom;
               return true;
            }
         }
         return false;
      }
   }

   private void pullNewDeckGUI() {
      if (this.rj.tryPullDeck()) {
         nullifyInfo();
         this.repaint();
         checkEndgame();
      }
   }

   public void undo() {
      Move undo = this.rj.undo();
      if (undo instanceof NewDeckMove) {
         this.highlightedMove = undo;
      } else {
         CardMove pUndo = (CardMove) undo;
         this.highlightedMove = MoveManager.tm.createMove(pUndo.getThere(), pUndo.getThereIndexBottom(), pUndo.getFrom(),
               pUndo.getFromIndexBottom() + 1, pUndo.wasUncoveredFrom(), pUndo.wasUncoveredThere(), pUndo.getWonType());
      }
      this.repaint();
   }

   public void redo() {
      Move redo = this.rj.redo();
      if (redo instanceof NewDeckMove) {
         this.highlightedMove = redo;
      } else {
         CardMove pRedo = (CardMove) redo;
         this.highlightedMove = MoveManager.tm.createMove(pRedo.getFrom(), pRedo.getFromIndexBottom(), pRedo.getThere(),
               pRedo.getThereIndexBottom() + 1, pRedo.wasUncoveredFrom(), pRedo.wasUncoveredThere(), pRedo.getWonType());
      }
      this.repaint();
   }

   public void displayHint() {
      unsuccessfulIndex %= rj.getPossibleMoves().size();
      if (rj.getPossibleMoves().size() == 0) {
         return;
      }
      this.highlightedMove = this.rj.getPossibleMoves().get(unsuccessfulIndex);
      this.repaint();
      unsuccessfulIndex++;
   }

   public void switchProcessingUnit(ProcessingUnit rj) {
      this.rj = rj;
      this.nullifyInfoAboutChosenCard();
      this.nullifyInfo();
      this.repaint();
   }

   public void storeState(File file) {
      StoringManager um = new StoringManager();
      um.storeState(this.rj, file);
   }

   public void resetGame() {
      while (rj.undo() != null) {

      }
      this.nullifyInfoAboutChosenCard();
      this.nullifyInfo();
      this.repaint();
   }

   private void nullifyInfo() {
      this.unsuccessfulIndex = 0;
      this.highlightedMove = null;
   }

   private void checkEndgame() {
      if (this.rj.getPossibleMoves().size() == 0) {
         if (this.rj.getWonCards().size() == 8) {
            JOptionPane.showConfirmDialog(null, "Vyhráli jste", "Gratulace",
                  JOptionPane.OK_OPTION);
         } else {
            JOptionPane.showConfirmDialog(null,
                  "Prohráli jste, můžete se zkusit o pár tahů vrátit a průběh hry zvrátit", "Konec hry",
                  JOptionPane.OK_OPTION);
         }

      }
   }
}