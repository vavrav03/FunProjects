package processing;

import java.util.ArrayList;
import java.util.List;

/**
 * Třída reprezentující jeden column karet na hrací ploše
 */
public class Column {

   private List<Card> cards;
   private int lastHiddenCard;

   public Column(int lastHiddenCard, List<Card> cards) {
      this.lastHiddenCard = lastHiddenCard;
      this.cards = cards;
   }

   public Column(List<Card> cards) {
      this.cards = cards;
      this.setLastHiddenCardIndex(cards.size() - 2);
   }

   public boolean isEmpty() {
      return cards.size() == 0;
   }

   public int getSize() {
      return cards.size();
   }

   public Card getCard(int index) {
      return cards.get(index);
   }

   /**
    * 
    * @return index první cards, která leží na první zakryté kartě. Pokud je -1, nenachází se
    */
   public int getLastHiddenCardIndex() {
      return lastHiddenCard;
   }

   /**
    * Metoda zamezí, aby se posledním skrytým indexem stala -2 v případě, že size je 0  a odečítá se -2 (v některé z metod)
    * @param lastHiddenCard
    */
   private void setLastHiddenCardIndex(int lastHiddenCard) {
      this.lastHiddenCard = lastHiddenCard;
      if (this.lastHiddenCard < -1) {
         this.lastHiddenCard = -1;
      }
   }

   public void incrementHiddenCardIndex() {
      this.lastHiddenCard++;
   }

   /**
    * 
    * @return Card s nejvyšším indexem (neleží na ní žádná jiná card). Pokud je column prázdný, je vrácen null.
    */
   public Card getTopCard() {
      if (this.isEmpty()) {
         return null;
      }
      return cards.get(cards.size() - 1);
   }

   /**
    * 
    * @return Smaže a vrátí kartu s nejvyšším indexem (neleží na ní žádná jiná card). Hodí se pro zpět. Pokud je column prázdný, je vrácen null.
    */
   public Card removeTopCard() {
      if (this.isEmpty()) {
         return null;
      }
      return cards.remove(cards.size() - 1);
   }

   /**
    * Metoda zkontroluje, zda je možné přesunout kartu na indexu beginningIndex a všechny cards nad ní
    * @param beginningIndex počáteční index (0 = card, na které leží všechny ostatní)
    * @return true = kartu je možné přenést
    */
   public boolean isMovable(int beginningIndex) {
      return beginningIndex >= this.findLastPossibleMovableIndex();
   }

   /**
    * 
   Deck p5 = new Deck(...);
    * @return nejnižší index cards, kterou je možné přesunout. Pokud je column prázdný, vrací -1
    */
   public int findLastPossibleMovableIndex(){
      int index = this.getSize() - 1;
      while(index >= 1){
         if(index == this.lastHiddenCard + 1){
            return index;
         }
         if (cards.get(index).getNumber() != cards.get(index - 1).getNumber() - 1) {
            return index;
         }
         if (cards.get(index).getType() != cards.get(index - 1).getType()) {
            return index;
         }
         index--;
      }
      return index;
   }

   /**
    * Metoda smaže všechny cards od počátečního indexu a výše. Vrátí je jako sublist. Slouží pro přesouvání karet mezi sloupci a k odkládání
    * @param beginningIndex počáteční index (0 = card, na které leží všechny ostatní)
    * @return true = cards byly smazány a vráceny
    */
   public List<Card> getAndRemoveCards(int beginningIndex) {
      List<Card> movedCards = new ArrayList<Card>(this.cards.subList(beginningIndex, cards.size()));
      cards.removeAll(movedCards);

      if (this.lastHiddenCard > this.cards.size() - 2) {
         this.setLastHiddenCardIndex(this.cards.size() - 2);
      }
      return movedCards;
   }

   /**
    * Metoda vloží na vrch sloupce list karet předaný v argumentu
    * @param movedCards
    */
   public void insertCards(List<Card> movedCards) {
      this.cards.addAll(movedCards);
   }

   /**
    * Metoda vloží jednu kartu na tento column
    * @param card 
    */
   public void pushCardOnTop(Card card) {
      this.cards.add(card);
   }

   /**
    * Metoda zkontroluje, zda je možné odložit balíček v tomto sloupci (platí přitom, odložitelný balíček musí být na vrchu ostatních karet)
    * @return true, pokud byl balíček odložen
    */
   public boolean tryWinDeck() {
      if (this.getSize() < ProcessingUnit.NUMBER_OF_CARDS_IN_DECK) {
         return false;
      }
      if (isMovable(this.getSize() - ProcessingUnit.NUMBER_OF_CARDS_IN_DECK)) {
         this.getAndRemoveCards(this.getSize() - ProcessingUnit.NUMBER_OF_CARDS_IN_DECK);
         return true;
      }
      return false;
   }
}
