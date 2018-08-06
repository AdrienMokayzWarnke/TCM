import java.util.Comparator;

public class cardCompator implements Comparator<Card> {

    @Override
    public int compare(Card o1, Card o2) {
        if(Card.getRawCardValue(o1)>Card.getRawCardValue(o2)){
            return -1;
        }else if (Card.getRawCardValue(o1)>Card.getRawCardValue(o2)){
            return 0;
        }else{
            return 1;
        }
    }
}