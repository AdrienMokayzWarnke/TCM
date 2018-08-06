import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kot on 2018-03-20.
 */
public class Card {


    private double getRealCost() {
        return Card.getRawCardValue(this);
    }

    public enum Type {
        CREATURE("creature"),
        ITEM_GREEN("itemGreen"),
        ITEM_RED("itemRed"),
        ITEM_BLUE("itemBlue");

        private String description;

        public static Type fromDescription(String description) {
            for (Type type : values()) {
                if (type.description.equals(description)) {
                    return type;
                }
            }
            return null;
        }

        Type(String description) {
            this.description = description;
        }

    }

    public double aggro;
    public double mid;
    public double control;
    public int id;
    public int baseId;
    public Type type;
    public int cost;
    public int attack;
    public int defense;
    public Keywords keywords;
    public int draftLocation;
    //TODO maybe myHealthChange, oppHealthChange, cardDraw should be moved into Summon class?
    public int myHealthChange;
    public int oppHealthChange;
    public int cardDraw;
    public String name;
    public String text;
    private String tooltipTextBase;
    public String comment;


    // todo copy constructor with id; ?
    // todo constructor with text (id-based)

    // copy constructor
    public Card(Card card) {
        this.id = card.id;
        this.baseId = card.baseId;
        this.name = card.name;
        this.type = card.type;
        this.cost = card.cost;
        this.attack = card.attack;
        this.defense = card.defense;
        this.keywords = new Keywords(card.keywords);
        this.myHealthChange = card.myHealthChange;
        this.oppHealthChange = card.oppHealthChange;
        this.cardDraw = card.cardDraw;
        this.comment = card.comment;
        this.tooltipTextBase = card.tooltipTextBase;
    }

    public void compare(){

    }
    // data = {baseId, name, type, cost, attack, defense, keywords, myHealthChange, oppHealthChange, cardDraw, comment}
    public Card(String[] data) {
        this.id = -1;
        this.baseId = Integer.parseInt(data[0]);
        this.name = data[1];
        this.type = Type.fromDescription(data[2]);
        this.cost = Integer.parseInt(data[3]);
        this.attack = Integer.parseInt(data[4]);
        this.defense = Integer.parseInt(data[5]);
        this.keywords = new Keywords(data[6]);
        this.myHealthChange = Integer.parseInt(data[7]);
        this.oppHealthChange = Integer.parseInt(data[8]);
        this.cardDraw = Integer.parseInt(data[9]);
        this.aggro = Double.parseDouble(data[10]);
        this.mid = Double.parseDouble(data[11]);
        this.control = Double.parseDouble(data[12]);

    }

    public String toStringWithoutId() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.baseId).append(' ');
        //sb.append(this.type.getDescription()).append(' ');
        sb.append(this.type.ordinal()).append(' '); // todo test is it ok? 0, 1, 2, 3
        sb.append(this.cost).append(' ');
        sb.append(this.attack).append(' ');
        sb.append(this.defense).append(' ');
        sb.append(this.keywords);
        sb.append(' ');
        sb.append(this.myHealthChange).append(' ');
        sb.append(this.oppHealthChange).append(' ');
        sb.append(this.cardDraw).append(' ');
        return sb.toString();
    }

    public String toString() {
        return this.id + " " + toStringWithoutId();
    }


    public static double getRawCardValue(Card card){
        double attack = 0.8;
        double breakthrough = 0.16116106;
        double charge = 0.25;
        double drain = 0.4656842;
        double drawCards = 1.510526458;
        double guard = 0.2301600155;
        double health = 0.468508454;
        double lethal = 0.125;//0.6501600155
        double ward = 0.368508454;

        double value = 0;
        if (card.cost < 3) {
            value = (card.attack * attack + card.defense * health + 1) / 2;
        } else {
            value = (card.attack * attack + card.defense * health+1)/1.6;
        }
        if (card.keywords.hasCharge) {
            value += charge * card.attack;
        }
        if (card.keywords.hasDrain) {
            value += drain;
        }
        if (card.keywords.hasLethal) {
            value += 0.4 + lethal * card.defense;
        }
        if (card.keywords.hasWard) {
            value += ward * card.attack;
        }
        if (card.keywords.hasBreakthrough) {
            value += breakthrough * card.attack;
        }
        if (card.keywords.hasGuard) {
            value += guard * card.defense;
        }
        if(card.cardDraw>0){
            value+= drawCards * card.cardDraw;
        }
        if(card.oppHealthChange>0){
            value += 1/30 * card.oppHealthChange;
        }
        if(card.myHealthChange>0){
            value += 1/30 * card.myHealthChange;
        }

        value /= card.cost;

        return value;

    }
}

