import java.util.List;

/**
 * Creature that is not a card anymore but it is placed on board.
 */
public class CreatureOnBoard {
    public int id;
    public int baseId;
    public int attack;
    public int defense;
    public int cost;
    public int myHealthChange;
    public int oppHealthChange;
    public int cardDraw;
    public Keywords keywords;

    public boolean canAttack;
    public boolean hasAttacked;
    public int lastTurnDefense;

    public Card baseCard;

    public CreatureOnBoard(CreatureOnBoard creature) {
        this.id = creature.id;
        this.baseId = creature.baseId;
        this.cost = creature.cost;
        this.attack = creature.attack;
        this.defense = creature.defense;
        this.keywords = new Keywords(creature.keywords);
        this.lastTurnDefense = creature.lastTurnDefense;
        baseCard = creature.baseCard;
        this.canAttack = creature.canAttack;
        this.hasAttacked = creature.hasAttacked;
    }

    /**
     * @param data "id baseId attack defense keywords"
     */
    public CreatureOnBoard(String data) {
        String[] creature = data.split(" ");
        this.id = Integer.parseInt(creature[0]);
        this.baseId = Integer.parseInt(creature[1]);
        this.attack = Integer.parseInt(creature[2]);
        this.defense = Integer.parseInt(creature[3]);
        this.keywords = new Keywords(creature[4]);
        this.canAttack = this.keywords.hasCharge;
        this.lastTurnDefense = this.defense;
    }

    public CreatureOnBoard(Card card) {
        this.id = card.id;
        this.baseId = card.baseId;
        this.attack = card.attack;
        this.defense = card.defense;
        this.keywords = new Keywords(card.keywords);
        this.canAttack = this.keywords.hasCharge;
        this.lastTurnDefense = card.defense;
        this.cost = card.cost;
        this.myHealthChange = card.myHealthChange;
        this.oppHealthChange = card.oppHealthChange;
        this.cardDraw = card.cardDraw;
        baseCard = card;
    }

    public String generateText() {
        List<String> keywords = this.keywords.getListOfKeywords();

        return String.join(", ", keywords);
    }

    public String toDescriptiveString() {
        StringBuilder sb = new StringBuilder();
        if (id >= 0) sb.append("id:").append(this.id).append(' ');
        sb.append("(").append(this.baseId).append(")").append(' ');

        sb.append("ATT:").append(this.attack).append(' ');
        sb.append("DEF:").append(this.defense).append(' ');
        sb.append(generateText());

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id).append(' ');
        sb.append(this.baseId).append(' ');
        sb.append(this.attack).append(' ');
        sb.append(this.defense).append(' ');
        sb.append(this.keywords);
        return sb.toString();
    }

    public String getAsInput(boolean isOpponentBoard) {
        int position = isOpponentBoard ? -1 : 1;
        StringBuilder s = new StringBuilder();
        s.append(baseId).append(" ");
        s.append(id).append(" ");
        s.append(position).append(" ");
        s.append(Card.Type.CREATURE.ordinal()).append(" ");
        s.append(cost).append(" ");
        s.append(attack).append(" ");
        s.append(defense).append(" ");
        s.append(keywords).append(" ");
        s.append(myHealthChange).append(" ");
        s.append(oppHealthChange).append(" ");
        s.append(cardDraw).append(" ");
        return s.toString();
    }

    public static double getValue(CreatureOnBoard card, int playerHP){
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
            value += drain + (30 - playerHP) / 30 ;
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
            value += guard * card.defense + (30 - playerHP) / 30;
        }
        if(card.cardDraw>0){
            value+= drawCards * card.cardDraw;
        }

        value /= card.cost;

        return value;

    }

}