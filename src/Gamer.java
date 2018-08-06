import java.util.*;

/**
 * Created by aCat on 2018-03-24.
 */
public class Gamer
{

    public static final int INITIAL_HAND_SIZE = 4;
    public static final int MAX_CARDS_IN_HAND = 8; // was 10
    public static final int SECOND_PLAYER_CARD_BONUS = 1;
    public static final int SECOND_PLAYER_MAX_CARD_BONUS = 0;

    public static final int MAX_MANA = 12;
    public static final int INITIAL_HEALTH = 30;

    public static final int MAX_CREATURES_IN_LINE = 6; // was 8

    public static final int PLAYER_TURNLIMIT = 50;

    int id;
    public ArrayList<Card> hand; //updated everyturn
    public ArrayList<Card> deck; //updated everyturn
    public ArrayList<CreatureOnBoard> board; //updated everyturn
    public int handSize;
    public int health; //updated everyturn
    public int maxMana; //updated everyturn
    public int currentMana; //dynamic
    public int nextTurnDraw; //dynamic

    public ArrayList<Integer> runes = new ArrayList<Integer>() {{ add(5);add(10);add(15);add(20);add(25); }};
    public int handLimit;

    public Gamer(int id, ArrayList<Card> deck)
    {
        this.id = id;
        this.hand = new ArrayList<>();
        this.deck = new ArrayList<>(deck);
        this.board = new ArrayList<>();
        this.health = INITIAL_HEALTH;
        this.maxMana = 0;
        this.currentMana = 0;
        this.nextTurnDraw = 1;

        handLimit = MAX_CARDS_IN_HAND + (id==0 ? 0 : SECOND_PLAYER_MAX_CARD_BONUS);
        DrawCards(INITIAL_HAND_SIZE + (id==0 ? 0 : SECOND_PLAYER_CARD_BONUS), 0);
    }

    public Gamer(){

    }

    private void suicideRunes()
    {
        if (!runes.isEmpty()) // first rune gone
        {
            Integer r = runes.remove(runes.size() - 1);
            health = r;
        }
        else // final run gone - suicide
        {
            health = 0;
        }
    }

    public void DrawCards(int n, int playerturn)
    {
        for (int i=0; i<n; i++)
        {
            if (deck.isEmpty() || playerturn>=PLAYER_TURNLIMIT)
            {
                suicideRunes();
                continue;
            }

            if (hand.size()==handLimit)
            {
                break; // additional draws are simply wasted
            }

            Card c = deck.remove(0);
            hand.add(c);
        }

    }


    public void ModifyHealth(int mod)
    {
        health += mod;

        if (mod >= 0)
            return;

        for (int r=runes.size()-1; r >=0; r--) // rune checking;
        {
            if (health <= runes.get(r))
            {
                nextTurnDraw += 1;
                runes.remove(r);
            }
        }
    }

    public int nextRune()
    {
        if (runes.isEmpty()) return 0;
        return runes.get(runes.size()-1);
    }

    public void removeFromBoard(int creatureIndex) {
        board.remove(creatureIndex);
    }

    // todo
    public String toString()
    {
        return super.toString();
    }

    public String getPlayerInput() {
        StringBuilder s = new StringBuilder();
        s.append(health).append(" ");
        s.append(maxMana).append(" ");
        s.append(deck.size()).append(" ");
        s.append(nextRune());
        return s.toString();
    }
}
