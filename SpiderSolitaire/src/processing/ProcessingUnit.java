package processing;

import java.util.Deque;
import java.util.List;

import processing.Card.CardType;
import processing.MoveManager.Move;

/**
 * Rozhraní pro řídící jednotky implementující pravidla Spider solitaire
 */
public interface ProcessingUnit {

   static int NUMBER_OF_DECKS_ON_BOARD=10;
   static int NUMBER_OF_NEW_DECKS=5;
   static int NUMBER_OF_CARDS_IN_DECK=13;

   /**
    * 
    * Metoda určená pro snadné zjištění GUI, zda může touto kartou hýbat
    * 
    * @param from číslo sloupce, ze kterého je card brána
    * @param fromIndexBottom index ve sloupci, ze kterého je card brána (card úplně na vespod - pod všemi ostatními kartherei má index)
    * @return true - s touto kartou je možné hýbat (neleží na ní žádná jiná card, která by pohyb znemožńovala)
    */
   boolean isCardMovable(int from, int fromIndexBottom);

   /**
    * Metoda zkusí vykonat přesunutí cards a v případě a vrátí informace o průběhu
    * @param from číslo sloupce, ze kterého je card brána
    * @param fromIndexBottom index ve sloupci, ze kterého je card brána (card úplně na vespod - pod všemi ostatními kartherei má index)
    * @param there číslo sloupce, na který je card pokládána
    * @return true, pokud se tah vykonat
    */
   boolean tryMove(int from, int fromIndexBottom, int there);

   /**
    * Metoda se pokusí vytáhnout novou sadu karet. 
    * @return true - nová sada byla vytažena. false - nová sada nebyla vytažena. Důdody: žádné sady, všechna políčka na hrací ploše nejsou zaplněna
    */
   boolean tryPullDeck();

   /**
    * 
    * @return počet sad, které je ještě možné vytáhnout
    */
   int numberOfRemainingDecks();

   /**
    * 
    * @return List všech možných tahů, které je možné v aktuálním kroku udělat
    */
   List<Move> getPossibleMoves();

   /**
    * 
    * @return Tah, který byl vrácen (má stále původní informace o pohybu, je nutné s tím počítat). Pokud nebyl proveden, je vrácen null.
    */
   Move undo();

   /**
    * 
    * @return Tah, který byl proveden příkazem vpřed. Pokud nebyl proveden, je vrácen null.
    */
   Move redo();

   /**
    * @param column index sloupci na hrací ploše (0-9)
    * @return reprezentace sloupce třídou Column
    */
   Column getColumn(int column);

   /**
    * 
    * @return List typeů aktuálně odložených balíčků v pořadí, v jakém byly uklizeny (0 - první, 8 - poslední)
    */
   List<CardType> getWonCards();

   /**
    * 
    * @return List všech zbývajících nových sad. Je potřeba ho zpřístupnit kvůli zápisu stavu hry do souboru 
    */
   List<Deck> getNewDecks();

   /**
    * 
    * @return Fronta všech budoucích tahů. Je potřeba ji zpřístupnit kvůli zápisu stavu hry do souboru 
    */
   Deque<Move> getFutureMoves();

   /**
    * 
    * @return Fronta všech minulých tahů. Je potřeba ji zpřístupnit kvůli zápisu stavu hry do souboru 
    */
   Deque<Move> getPastMoves();
}
