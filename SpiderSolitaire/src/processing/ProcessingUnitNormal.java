package processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import processing.Card.CardType;
import processing.MoveManager.CardMove;
import processing.MoveManager.Move;
import processing.MoveManager.NewDeckMove;

/**
 * Základní implementace pravidel Spider solitaire
 */
public class ProcessingUnitNormal implements ProcessingUnit {

   private Column[] playingField;

   private List<Deck> newDecks; //nejdříve se bere z konce listu
   private List<CardType> wonDecks = new ArrayList<CardType>();

   List<Move> possibleMoves = new ArrayList<Move>(); //tahy, které je možné vykonat před následujícím tahem
   // private int wonDecks;
   // private int pocetTahu;

   public static ProcessingUnit newGame(int numberOfColors) {
      int numberOfDecksOfSameType;
      switch (numberOfColors) {
         case 1:
            numberOfDecksOfSameType = 8;
            break;
         case 2:
            numberOfDecksOfSameType = 4;
            break;
         case 4:
            numberOfDecksOfSameType = 2;
            break;
         default:
            throw new IllegalArgumentException("Počet různých barev může být pouze 1, 2, nebo 4");
      }
      return new ProcessingUnitNormal(numberOfColors, numberOfDecksOfSameType);
   }

   public static ProcessingUnit nactiHru(File file) {
      StoringManager um = new StoringManager();
      return um.readState(file);
   }

   /**
    * konstruktor určený pro inicializaci hry do již rozehraného stavu
    * @param playingField
    * @param newDecks
    * @param futureMoves
    * @param lastMoves
    */
   public ProcessingUnitNormal(Column[] playingField, List<Deck> newDecks, Deque<Move> futureMoves,
         Deque<Move> lastMoves) {
      this.playingField = playingField;
      this.newDecks = newDecks;
      while (!lastMoves.isEmpty()) {
         Move move = lastMoves.pop();
         if (move instanceof NewDeckMove) {
            this.tryPullDeck();
         } else {
            CardMove pMove = (CardMove) move;
            this.tryMove(pMove.getFrom(), pMove.getFromIndexBottom(), pMove.getThere());
         }
      }
      MoveManager.tm.setFutureMoves(futureMoves);
   }

   /**
    * Konstruktor určený pro vytvoření nové hry
    * @param numberOfColors
    * @param numberOfDecksOfSameType
    */
   public ProcessingUnitNormal(int numberOfColors, int numberOfDecksOfSameType) {
      this.playingField = new Column[NUMBER_OF_DECKS_ON_BOARD];
      this.newDecks = new ArrayList<Deck>();
      List<Card> shuffledCards = new ArrayList<Card>(8 * NUMBER_OF_CARDS_IN_DECK);
      for (int f = 0; f < numberOfDecksOfSameType; f++) {
         for (int i = 1; i <= NUMBER_OF_CARDS_IN_DECK; i++) {
            for (int j = 0; j < numberOfColors; j++) {
               shuffledCards.add(new Card(i, Card.CardType.values()[j]));
            }
         }
      }
      int counter = 0;
      Collections.shuffle(shuffledCards);
      for (int i = 0; i < NUMBER_OF_NEW_DECKS; i++) {
         newDecks.add(new Deck(
               (Card[]) new ArrayList<Card>(shuffledCards.subList(counter, counter + 10)).toArray(new Card[10])));
         counter += 10;
      }
      for (int i = 0; i < 4; i++) {
         this.playingField[i] = new Column(new ArrayList<Card>(shuffledCards.subList(counter, counter + 6)));
         counter += 6;
      }
      for (int i = 4; i < 10; i++) {
         this.playingField[i] = new Column(new ArrayList<Card>(shuffledCards.subList(counter, counter + 5))); //Musí se to obalit new ArrayList, protože jinak by všechny sloupce měli tu samou instanci pole karet. To dělá bordel při mazání. (ConcurrentException)
         counter += 5;
      }
      findPossibleMoves();
   }

   /**
    * Metoda zkusí vykonat předaný tah. Nemusí se to však podařit, jelikož probíhá kontrola
    * @param move Libovolný type tahu
    */
   private void tryMove(Move move) {
      if (move instanceof NewDeckMove) {
         this.tryPullDeck();
      } else if (move instanceof CardMove) {
         CardMove pohybKartouTah = (CardMove) move;
         this.tryMove(pohybKartouTah.getFrom(), pohybKartouTah.getFromIndexBottom(), pohybKartouTah.getThere());
      }
   }

   @Override
   public boolean tryMove(int from, int fromIndexBottom, int there) {
      if (isPossibleMove(from, fromIndexBottom, there)) {
         int lastHiddenCardIndexFrom = this.playingField[from].getLastHiddenCardIndex();
         int lastHiddenCardIndexThere = this.playingField[there].getLastHiddenCardIndex();
         CardType movedCardType = this.playingField[from].getCard(fromIndexBottom).getType();
         int thereIndexBottom = this.playingField[there].getSize(); // tento index zaujme spodní přesouvaná card
         this.moveCards(from, fromIndexBottom, there);
         if (this.getColumn(there).tryWinDeck()) {
            wonDecks.add(movedCardType);
         } else {
            movedCardType = null;
         }
         boolean uncoveredFrom = this.playingField[from].getLastHiddenCardIndex() != lastHiddenCardIndexFrom;
         boolean uncoveredThere = this.playingField[there].getLastHiddenCardIndex() != lastHiddenCardIndexThere;
         MoveManager.tm.writeMove(
               MoveManager.tm.createMove(from, fromIndexBottom, there, thereIndexBottom, uncoveredFrom, uncoveredThere,
                     movedCardType));
         findPossibleMoves();
         return true;
      }
      System.out.println("impossible");
      return false;
   }

   /**
    * Metoda slučuje funkcionalitu isCardMovable a jeMoznePolozeni - kontroluje tedy všechny možné věci, které by se při přesouvání cards mohly pokazit
    * @param from
    * @param fromIndexBottom
    * @param there
    * @return true, pokud je možné tah vykonat
    */
   private boolean isPossibleMove(int from, int fromIndexBottom, int there) {
      if (!this.isCardMovable(from, fromIndexBottom)) {
         return false;
      }
      if (!this.jeMoznePolozeni(from, fromIndexBottom, there)) {
         return false;
      }
      return true;
   }

   @Override
   public boolean isCardMovable(int from, int fromIndexBottom) {
      return this.playingField[from].isMovable(fromIndexBottom);
   }

   /**
    * 
    * @return true, pokud je číslo spodní přesouvané cards o 1 menší než číslo cards, na kterou je přesouváno nebo je pole prázdné. 
    */
   private boolean jeMoznePolozeni(int from, int fromIndexBottom, int there) {
      return this.playingField[there].isEmpty()
            || playingField[from].getCard(fromIndexBottom).getNumber() == this.playingField[there].getTopCard().getNumber()
                  - 1;
   }

   /**
    * Metoda přesune kartu dle parametrů bez jakékoliv kontroly a zapisování
    * @param from
    * @param fromIndexBottom
    * @param there
    */
   private void moveCards(int from, int fromIndexBottom, int there) {
      List<Card> movedCards = this.playingField[from].getAndRemoveCards(fromIndexBottom);
      this.playingField[there].insertCards(movedCards);
   }

   @Override
   public boolean tryPullDeck() {
      if (canIPullNewDeck()) {
         for (int i = 0; i < NUMBER_OF_DECKS_ON_BOARD; i++) {
            this.playingField[i].pushCardOnTop(this.newDecks.get(newDecks.size() - 1).getCard(i));
         }
         newDecks.remove(newDecks.size() - 1);
         MoveManager.tm.writeMove(MoveManager.tm.pullNewDeckMove());
         findPossibleMoves();
         return true;
      }
      return false;
   }

   @Override
   public int numberOfRemainingDecks() {
      return newDecks.size();
   }

   private boolean canIPullNewDeck() {
      if (newDecks.size() == 0) {
         return false;
      }
      for (int i = 0; i < NUMBER_OF_DECKS_ON_BOARD; i++) {
         if (playingField[i].isEmpty()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public Move undo() {
      Move lastMove = MoveManager.tm.removeLastMove();
      if (lastMove == null) {
         return null;
      }
      MoveManager.tm.addFutureMove(lastMove);
      if (lastMove instanceof NewDeckMove) {
         Card[] cardsThereDecks = new Card[NUMBER_OF_DECKS_ON_BOARD];
         for (int i = 0; i < cardsThereDecks.length; i++) {
            cardsThereDecks[i] = playingField[i].removeTopCard();
         }
         newDecks.add(new Deck(cardsThereDecks));
         return lastMove;
      } else if (lastMove instanceof CardMove) {
         CardMove move = (CardMove) lastMove;
         if (move.wasUncoveredFrom()) {
            playingField[move.getFrom()].incrementHiddenCardIndex(); //odkrývalo se z from, jelikož tahy jsou v pořadí, v jakém byly zahrány, nikoliv v jakém probíhá zpětný tah
         }
         if (move.wasUncoveredThere()) {
            playingField[move.getThere()].incrementHiddenCardIndex();
         }
         if (move.getWonType() != null) {
            wonDecks.remove(wonDecks.size() - 1);
            getWonCardsBack(move.getThere(), move.getWonType());
         }
         this.moveCards(move.getThere(), move.getThereIndexBottom(), move.getFrom());
         return lastMove;
      }
      return null;
   }

   @Override
   public Move redo() {
      Move move = MoveManager.tm.getFirstFutureMove();
      if (move == null) {
         return null;
      }
      tryMove(move);
      return move;
   }

   @Override
   public List<Move> getPossibleMoves() {
      return this.possibleMoves;
   }

   private void findPossibleMoves() {
      this.possibleMoves.clear();
      for (int i = 0; i < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; i++) {
         int ppIndex = this.getColumn(i).findLastPossibleMovableIndex();
         if (ppIndex == -1) {
            continue;
         }
         Card ppCard = this.getColumn(i).getCard(ppIndex);
         for (int j = 0; j < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; j++) {
            if (i == j) {
               continue;
            }
            Card vjCard = this.getColumn(j).getTopCard();
            if (vjCard == null) {
               //Pokud je column prázdný, budeme považovat za dobrý tah pouze přesunutí nejspodnější přesunutelné cards
               if(ppIndex == 0){
                  continue; //přesun z prázdného na prázdné pole
               }
               possibleMoves
                     .add(MoveManager.tm.createMove(i, ppIndex, j, this.getColumn(j).getSize(), false, false, null)); //poslední tři argumenty k ničemu nejsou
               continue;

            }
            if (ppCard.getNumber() >= vjCard.getNumber() - 1
                  && this.getColumn(i).getTopCard().getNumber() < vjCard.getNumber()) {
               int wantedCard = ppIndex + ppCard.getNumber() - (vjCard.getNumber() - 1); //Card ze sloupce i, která je přesunutelná na column j
               int ppThereIndex = this.getColumn(j).findLastPossibleMovableIndex();
               if((this.getColumn(j).getSize() - ppThereIndex) <= (wantedCard - ppIndex)){
                  continue; //tah není výhodný (přetažením by byl počet přesunutelných karet na there sloupci menší nebo stejný než na od sloupci)
               }
               possibleMoves.add(MoveManager.tm.createMove(i, wantedCard, j, this.getColumn(j).getSize(), false,
                     false, null));
            }
         }
      }
      if (possibleMoves.size() == 0 && !newDecks.isEmpty()) {
         this.possibleMoves.add(new NewDeckMove());
      }
   }

   @Override
   public Column getColumn(int column) {
      return playingField[column];
   }

   @Override
   public List<CardType> getWonCards() {
      return wonDecks;
   }

   private void getWonCardsBack(int column, CardType type) {
      for (int i = ProcessingUnit.NUMBER_OF_CARDS_IN_DECK; i >= 1; i--) {
         this.playingField[column].pushCardOnTop(new Card(i, type));
      }
   }

   @Override
   public List<Deck> getNewDecks() {
      return newDecks;
   }

   @Override
   public Deque<Move> getFutureMoves() {
      return MoveManager.tm.getFutureMoves();
   }

   @Override
   public Deque<Move> getPastMoves() {
      return MoveManager.tm.getPastMoves();
   }

   public String textRepresentation() {
      StringBuilder s = new StringBuilder();
      int maximum = -1;
      for (int i = 0; i < NUMBER_OF_DECKS_ON_BOARD; i++) {
         maximum = Math.max(maximum, playingField[i].getSize());
         s.append(i + "\t");
      }
      s.append('\n');
      for (int i = 0; i < maximum; i++) {
         for (int j = 0; j < NUMBER_OF_DECKS_ON_BOARD; j++) {
            if (i < playingField[j].getSize()) {
               if (i <= playingField[j].getLastHiddenCardIndex()) {
                  s.append("...");
               } else {
                  s.append(playingField[j].getCard(i).toString());
               }
               s.append('\t');
            } else {
               s.append('\t');
            }
         }
         s.append('\n');
      }
      return s.toString();
   }
}
