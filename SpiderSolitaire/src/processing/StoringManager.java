package processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import processing.Card.CardType;
import processing.MoveManager.CardMove;
import processing.MoveManager.Move;
import processing.MoveManager.NewDeckMove;

/**
 * Třída má na starosti ukládání stavu hry do souboru a čtení z něho.
 */
public class StoringManager {
   public static final String STORED_GAMES_PATH = "saved_games";

   private static final String PLAYING_FIELD = "PLAYING_FIELD";
   private static final String NEW_DECKS = "NEW_DECKS";
   private static final String PAST_MOVES = "PAST_MOVES";
   private static final String FUTURE_MOVES = "FUTURE_MOVES";

   public StoringManager() {
   }

   public void storeState(ProcessingUnit rj, File file) {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
         int numberOfMovesBack = 0;
         while (rj.undo() != null) {
            numberOfMovesBack++;
         }
         bw.write(PLAYING_FIELD);
         bw.newLine();
         for (int i = 0; i < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; i++) {
            for (int j = 0; j < rj.getColumn(i).getSize(); j++) {
               Card card = rj.getColumn(i).getCard(j);
               bw.write(encodeCard(card));
               if (j != rj.getColumn(i).getSize() - 1) {
                  bw.write(",");
               }
            }
            bw.newLine();
         }
         bw.write(NEW_DECKS);
         bw.newLine();
         for (int i = 0; i < rj.getNewDecks().size(); i++) {
            for (int j = 0; j < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; j++) {
               Card card = rj.getNewDecks().get(i).getCard(j);
               bw.write(encodeCard(card));
               if (j != ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD - 1) {
                  bw.write(",");
               }
            }
            bw.newLine();
         }
         for (int i = 0; i < numberOfMovesBack; i++) {
            rj.redo();
         }
         bw.write(FUTURE_MOVES);
         bw.newLine();
         bw.write(String.valueOf(rj.getFutureMoves().size())); //bez String.valueOf to má jiný význam
         bw.newLine();
         Deque<Move> futureMoves = new LinkedList<Move>(rj.getFutureMoves());
         while(!futureMoves.isEmpty()) {
            bw.write(this.encodeMove(futureMoves.pollFirst()));
            bw.newLine();
         }
         bw.write(PAST_MOVES);
         bw.newLine();
         bw.write(String.valueOf(rj.getPastMoves().size()));
         bw.newLine();
         Deque<Move> lastMoves = new LinkedList<Move>(rj.getPastMoves());
         while (!lastMoves.isEmpty()) {
            bw.write(this.encodeMove(lastMoves.pollLast()));
            bw.newLine();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Vlastní výjimka vyvolaná, pokud je nalezena chyba ve formátu souboru
    */
   private class WrongFormatException extends RuntimeException {

   }

   public ProcessingUnit readState(File file) {
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
         Column[] createdPlayingField = new Column[ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD];
         if (!br.readLine().equals(PLAYING_FIELD)) {
            throw new WrongFormatException();
         }
         for(int i = 0; i < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; i++){
            String columnString = br.readLine();
            String[] columnData = columnString.split(",");
            ArrayList<Card> cards = new ArrayList<Card>();
            for(int j = 0; j < columnData.length; j++){
               cards.add(decodeCards(columnData[j]));
            }
            createdPlayingField[i] = new Column(cards);
         }

         List<Deck> createdNewDecks = new ArrayList<Deck>();
         if (!br.readLine().equals(NEW_DECKS)) {
            throw new WrongFormatException();
         }
         for(int i = 0; i < ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD; i++){
            Card[] cards = new Card[ProcessingUnit.NUMBER_OF_DECKS_ON_BOARD];
            String deckString = br.readLine();
            String deckData[] = deckString.split(",");
            for(int j = 0; j < cards.length; j++){
               cards[j] = decodeCards(deckData[j]);
            }
            createdNewDecks.add(new Deck(cards));
         }
         if (!br.readLine().equals(FUTURE_MOVES)) {
            throw new WrongFormatException();
         }
         int numberOfFutureMoves = Integer.parseInt(br.readLine());
         Deque<Move> futureMoves = new LinkedList<Move>();
         for(int i = 0; i < numberOfFutureMoves; i++){
            Move move = decodeMove(br.readLine());
            futureMoves.add(move);
         }
         if (!br.readLine().equals(PAST_MOVES)) {
            throw new WrongFormatException();
         }
         int numberOfPastMoves = Integer.parseInt(br.readLine());
         Deque<Move> lastMoves = new LinkedList<Move>();
         for(int i = 0; i < numberOfPastMoves; i++){
            Move move = decodeMove(br.readLine());
            lastMoves.add(move);
         }
         ProcessingUnit newPU = new ProcessingUnitNormal(createdPlayingField, createdNewDecks, futureMoves, lastMoves);
         
         return newPU;
      } catch (IOException e) {

      }
      throw new RuntimeException("Něco se pokazilo");
   }

   private String encodeCard(Card card) {
      return card.getNumber() + "_" + card.getType();
   }

   private Card decodeCards(String cardString){
      System.out.println(cardString);
      String[] data = cardString.split("_");
      int number = Integer.parseInt(data[0]);
      CardType type = CardType.valueOf(data[1]);
      return new Card(number, type);
   }

   private Move decodeMove(String s) {
      if (s.equals("NS")) {
         return new NewDeckMove();
      }
      String[] data = s.split("\\|");
      if (data.length != 2) {
         throw new IllegalArgumentException("Data jsou ve špatném formátu");
      }
      String[] moveInfo = data[0].split(",");
      String[] nextInfo = data[1].split(",");

      String[] from = moveInfo[0].split(":");
      String[] there = moveInfo[1].split(":");
      
      boolean uncoveredFrom = false;
      boolean uncoveredThere = false;
      CardType wonType = null;
      if(nextInfo.length == 0){
      } else if(nextInfo.length == 1){
         uncoveredFrom = nextInfo[0].equals("OO");
      } else if(nextInfo.length == 2){
         uncoveredFrom = nextInfo[0].equals("OO");
         uncoveredFrom = nextInfo[1].equals("OT");
      } else if(nextInfo.length == 3){
         uncoveredFrom = nextInfo[0].equals("OO");
         uncoveredFrom = nextInfo[1].equals("OT");
         wonType = CardType.valueOf(nextInfo[2]);
      }
      
      return new CardMove(Integer.parseInt(from[0]), Integer.parseInt(from[1]), Integer.parseInt(there[0]),
            Integer.parseInt(there[1]), uncoveredFrom, uncoveredThere, wonType);
   }

   private String encodeMove(Move move) {
      if (move instanceof NewDeckMove) {
         return "NS";
      } else if (move instanceof CardMove) {
         CardMove pMove = (CardMove) move;
         return pMove.getFrom() + ":" + pMove.getFromIndexBottom() + "," + pMove.getThere() + ":" + (pMove.getThereIndexBottom())
               + "|" + (pMove.wasUncoveredFrom() ? "OO" : "") + "," +(pMove.wasUncoveredThere() ? "OT" : "") + ","
               + (pMove.getWonType() != null ? pMove.getWonType().name() : "");

      } else {
         throw new IllegalArgumentException("");
      }
   }
}
