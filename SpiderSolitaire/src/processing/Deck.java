package processing;

/**
 * Třída reprezentující jednu z 5 nových sad karet
 */
public class Deck {
   private Card[] cards;

   public Deck(Card[] cards) {
      this.cards = cards;
   }

   public Card getCard(int index) {
      return cards[index];
   }
}