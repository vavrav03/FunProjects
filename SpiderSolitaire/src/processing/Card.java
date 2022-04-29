package processing;

public class Card {

   public enum CardType {
      SPADES('♠', 0), HEARTS('♥', 1), CLUBS('♣', 2), DIAMONDS('♦', 3);

      private char displayChar;
      private int cardTypeNumber;

      private CardType(char displayChar, int cardTypeNumber) {
         this.displayChar = displayChar;
         this.cardTypeNumber = cardTypeNumber;
      }

      public int getNumberTypuKarty() {
         return cardTypeNumber;
      }
   }

   private int number;
   private CardType type;

   public Card(int number, CardType type) {
      if (number < 1 || number > 13) {
         throw new IllegalArgumentException("Číslo cards může být pouze od 1 do 13");
      }
      this.number = number;
      this.type = type;
   }

   public int getNumber() {
      return this.number;
   }

   public CardType getType() {
      return this.type;
   }

   public String toString() {
      StringBuilder s = new StringBuilder();
      if (this.number == 1) {
         s.append('A');
      } else if (this.number <= 10) {
         s.append(this.number);
      } else if (this.number == 11) {
         s.append('J');
      } else if (this.number == 12) {
         s.append('Q');
      } else if(this.number == 13){
         s.append('K');
      }
      s.append(this.type.displayChar);
      return s.toString();
   }
}
