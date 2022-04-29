package processing;

import java.util.Deque;
import java.util.LinkedList;

import processing.Card.CardType;

/**
 * Třída zodpovídá za zakódování tahů do objektů, se kterými ostatní třídy mohou jednoduše pracovat. Udržuje také frontu minulých a budoucích tahů.
 */
public class MoveManager {

   private Deque<Move> lastMoves;
   private Deque<Move> futureMoves;

   public static MoveManager tm = new MoveManager();

   private MoveManager() {
      this.lastMoves = new LinkedList<Move>();
      this.futureMoves = new LinkedList<Move>();
   }

   public CardMove createMove(int od, int fromIndexBottom, int there, int thereIndexBottom, boolean uncoveredFrom, boolean uncoveredThere,
         CardType wonType) {
      return new CardMove(od, fromIndexBottom, there, thereIndexBottom, uncoveredFrom, uncoveredThere, wonType);
   }

   public NewDeckMove pullNewDeckMove() {
      return new NewDeckMove();
   }

   public void writeMove(Move move) {
      lastMoves.push(move);
      if (lastMoves.peek().equals(futureMoves.peek())) {
         futureMoves.poll();
      } else {
         futureMoves.clear();
      }
   }

   public Move getFirstFutureMove() {
      return futureMoves.peek();
   }

   public void addFutureMove(Move move) {
      futureMoves.push(move);
   }

   /**
    * Smaže a vrátí poslední tah
    * @return poslední tah
    */
   public Move removeLastMove() {
      return lastMoves.poll();
   }

   public Deque<Move> getPastMoves(){
      return lastMoves;
   }

   public Deque<Move> getFutureMoves(){
      return futureMoves;
   }

   /**
    * Metoda se hodí při inicializaci MoveManageru při čtení stavu ze souborů
    * @param futureMoves fronta budoucích tahů, která nahradí aktuální frontu budoucích tahů. 
    */
   public void setFutureMoves(Deque<Move> futureMoves){
      this.futureMoves = futureMoves;
   }

   public static interface Move {

   }

   public static class NewDeckMove implements Move {

      @Override
      public boolean equals(Object move) {
         return move instanceof NewDeckMove;
      }
   }

   /**
    * Metoda zakóduje vykonaný tah jako string ve formátu:
    * "od:fromIndexBottom,there:thereIndexBottom"
    * 
    * @field from            index sloupce, ze kterého se přesouvá
    * @field fromIndexBottom  index spodní přesouvané cards v rámci sloupce, ze
    *                      kterého se přesouvá (0 = card, na které leží všechny
    *                      ostatní)
    * @field there           index sloupce, do kterého se přesouvá
    * @field thereIndexBottom index spodní přesouvané cards v rámci sloupce, do
    *                      kterého se přesouvá (0 = card, na které leží všechny
    *                      ostatní)
    * @field odkryto       Pokud je true, došlo při tahu k odkrytí cards
    * @field wonType      CardType, který byl v tahu odložen. Pokud žádný odložen nebyl, je null 
    */
   public static class CardMove implements Move {
      private int from;
      private int fromIndexBottom;
      private int there;
      private int thereIndexBottom;
      private boolean uncoveredFrom;
      private boolean uncoveredThere;
      private CardType wonType;

      public CardMove(int from, int fromIndexBottom, int there, int thereIndexBottom, boolean uncoveredFrom, boolean uncoveredThere, CardType wonType) {
         this.from = from;
         this.fromIndexBottom = fromIndexBottom;
         this.there = there;
         this.thereIndexBottom = thereIndexBottom;
         this.uncoveredFrom = uncoveredFrom;
         this.uncoveredThere = uncoveredThere;
         this.wonType = wonType;
      }

      public int getFrom() {
         return from;
      }

      public int getFromIndexBottom() {
         return fromIndexBottom;
      }

      public int getThere() {
         return there;
      }

      public int getThereIndexBottom() {
         return thereIndexBottom;
      }

      public boolean wasUncoveredFrom() {
         return uncoveredFrom;
      }

      public boolean wasUncoveredThere(){
         return uncoveredThere;
      }

      public CardType getWonType() {
         return wonType;
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + from;
         result = prime * result + fromIndexBottom;
         result = prime * result + (uncoveredFrom ? 1231 : 1237);
         result = prime * result + (uncoveredThere ? 1231 : 1237);
         result = prime * result + ((wonType == null) ? 0 : wonType.hashCode());
         result = prime * result + there;
         result = prime * result + thereIndexBottom;
         return result;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         CardMove other = (CardMove) obj;
         if (from != other.from)
            return false;
         if (fromIndexBottom != other.fromIndexBottom)
            return false;
         if (uncoveredFrom != other.uncoveredFrom)
            return false;
         if (uncoveredThere != other.uncoveredThere)
            return false;
         if (wonType != other.wonType)
            return false;
         if (there != other.there)
            return false;
         if (thereIndexBottom != other.thereIndexBottom)
            return false;
         return true;
      }

   }
}
