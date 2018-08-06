import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aCat on 2018-03-20.
 */
public class GameStatesManagersManager
{
    public int turn;
    public HashMap<Integer, Card> cardIdMap;
    public GameState originalState = new GameState();
    public Gamer player = originalState.currentPlayer;
    public Gamer enemyGamer = originalState.enemyPlayer;

    int MAX_CREATURES_IN_LINE = 6;
    int MAX_MANA = 12;

    public List<Action> computeLegalActionsForGameState(GameState gameState){
        List<Action> legals = new ArrayList<>();
        legals.addAll(computeLegalSummonsForPayer(gameState.currentPlayer));
        legals.addAll(computeLegalAttacksForPlayer(gameState.currentPlayer,gameState.enemyPlayer));
        legals.addAll(computeLegalItemsForPlayer(gameState.currentPlayer,gameState.enemyPlayer));
        //legals.add(Action.newPass()); on ne veut pas explorer l'action de passer son tour
        return legals;
    }

    public List<Action> computeLegalSummonsForPayer(Gamer gamer){
        ArrayList<Action> actions = new ArrayList<>();

        if (gamer.board.size() == MAX_CREATURES_IN_LINE)
            return actions;

        for (Card c:gamer.hand)
        {
            if (c.type != Card.Type.CREATURE || c.cost > gamer.currentMana)
                continue;
            actions.add(Action.newSummon(c.id));
        }

        return actions;
    }


    private List<Integer> computeLegalTargetsForPlayer(Gamer gamer){

        ArrayList<Integer> targets = new ArrayList<>();

        for (CreatureOnBoard c : gamer.board) // First priority - guards
            if (c.keywords.hasGuard)
                targets.add(c.id);

        if (targets.isEmpty()) // if no guards we can freely attack any creature plus face
        {
            targets.add(-1);
            for (CreatureOnBoard c : gamer.board)
                targets.add(c.id);
        }

        return targets;
    }

    public List<Action> computeLegalAttacksForPlayer(Gamer attacker, Gamer defender){

        List<Integer> targets = computeLegalTargetsForPlayer(defender);

        ArrayList<Action> actions = new ArrayList<>();

        for (CreatureOnBoard c:attacker.board)
        {
            if (!c.canAttack)
                continue;
            for (Integer tid: targets)
                actions.add(Action.newAttack(c.id, tid));
        }

        return actions;
    }


    public List<Action> computeLegalItemsForPlayer(Gamer user,Gamer enemy){


        ArrayList<Action> actions = new ArrayList<>();

        for (Card c:user.hand)
        {
            if (c.type == Card.Type.CREATURE || c.cost > user.currentMana)
                continue;

            if (c.type == Card.Type.ITEM_GREEN) // on friendly creatures
            {
                for (CreatureOnBoard cb : user.board)
                    actions.add(Action.newUse(c.id, cb.id));
            }
            else // red or blue item: on enemy creatures
            {
                for (CreatureOnBoard cb : enemy.board)
                    actions.add(Action.newUse(c.id, cb.id));
                if (c.type == Card.Type.ITEM_BLUE) // blue also on the player
                    actions.add(Action.newUse(c.id, -1));
            }
        }

        return actions;
    }

    public GameState AdvanceState(Action action,GameState gameState){

        GameState newGameState = new GameState(gameState);

        if (action.type == Action.Type.SUMMON) // SUMMON [id]
        {
            Card c = cardIdMap.get(action.arg1);

            newGameState.currentPlayer.hand.remove(c);
            newGameState.currentPlayer.currentMana -= c.cost;
            CreatureOnBoard creature = new CreatureOnBoard(c);
            newGameState.currentPlayer.board.add(creature);

            newGameState.currentPlayer.ModifyHealth(c.myHealthChange);
            newGameState.enemyPlayer.ModifyHealth(c.oppHealthChange);
            newGameState.currentPlayer.nextTurnDraw += c.cardDraw;

            action.result = new ActionResult(creature, null, false, false, c.myHealthChange, c.oppHealthChange);
        }
        else if (action.type == Action.Type.ATTACK) // ATTACK [id1] [id2]
        {
            int indexatt = -1;
            for (int i=0; i < newGameState.currentPlayer.board.size(); i++)
                if (newGameState.currentPlayer.board.get(i).id==action.arg1)
                    indexatt = i;
            CreatureOnBoard att = newGameState.currentPlayer.board.get(indexatt);

            int indexdef = -1;
            CreatureOnBoard def;
            ActionResult result = null;

            if (action.arg2 == -1) // attacking player
            {
                result = ResolveAttack(att);
            }
            else
            {
                for (int i=0; i < newGameState.enemyPlayer.board.size(); i++)
                    if (newGameState.enemyPlayer.board.get(i).id==action.arg2)
                        indexdef = i;
                def = newGameState.enemyPlayer.board.get(indexdef);

                result = ResolveAttack(att, def);

                if (result.defenderDied) {
                    newGameState.enemyPlayer.removeFromBoard(indexdef);

                }
                else
                    newGameState.enemyPlayer.board.set(indexdef, result.defender);
            }

            if (result.attackerDied)
                newGameState.currentPlayer.removeFromBoard(indexatt);
            else
                newGameState.currentPlayer.board.set(indexatt, result.attacker);

            newGameState.currentPlayer.ModifyHealth(result.attackerHealthChange);
            newGameState.enemyPlayer.ModifyHealth(result.defenderHealthChange);
            action.result = result;
        }
        else if (action.type == Action.Type.USE) // USE [id1] [id2]
        {
            Card item = cardIdMap.get(action.arg1);

            newGameState.currentPlayer.hand.remove(item);
            newGameState.currentPlayer.currentMana -= item.cost;

            if (item.type == Card.Type.ITEM_GREEN) // here we assume that green cards never remove friendly creatures!
            {
                int indextarg = -1;
                for (int i=0; i < newGameState.currentPlayer.board.size(); i++)
                    if (newGameState.currentPlayer.board.get(i).id==action.arg2)
                        indextarg = i;
                CreatureOnBoard targ = newGameState.currentPlayer.board.get(indextarg);

                ActionResult result = ResolveUse(item, targ);

                newGameState.currentPlayer.board.set(indextarg, result.defender);

                newGameState.currentPlayer.ModifyHealth(result.attackerHealthChange);
                newGameState.currentPlayer.ModifyHealth(result.defenderHealthChange);
                newGameState.currentPlayer.nextTurnDraw += item.cardDraw;
                action.result = result;
            }
            else // red and blue cards
            {
                int indextarg = -1;
                ActionResult result = null;

                if (action.arg2 == -1) // using on player
                {
                    result = ResolveUse(item);
                }
                else // using on creature
                {
                    for (int i=0; i < newGameState.enemyPlayer.board.size(); i++)
                        if (newGameState.enemyPlayer.board.get(i).id==action.arg2)
                            indextarg = i;
                    CreatureOnBoard targ = newGameState.enemyPlayer.board.get(indextarg);

                    result = ResolveUse(item, targ);

                    if (result.defenderDied)
                        newGameState.enemyPlayer.removeFromBoard(indextarg);
                    else
                        newGameState.enemyPlayer.board.set(indextarg, result.defender);
                }

                newGameState.currentPlayer.ModifyHealth(result.attackerHealthChange);
                newGameState.enemyPlayer.ModifyHealth(result.defenderHealthChange);
                newGameState.currentPlayer.nextTurnDraw += item.cardDraw;
                action.result = result;
            }
        }

        CheckWinConditionForGameState(newGameState);
        newGameState.parent = gameState;
        newGameState.actions.add(action);
        return newGameState;
    }

    public void CheckWinConditionForGameState(GameState gameState)
    {
        if (gameState.enemyPlayer.health <= 0) // first proper win
            gameState.isWinning = true;
        if (gameState.currentPlayer.health <= 0) // second self-kill
            gameState.isLoosing = true;
    }

    // when creature attacks creatures // run it ONLY on legal actions
    public static ActionResult ResolveAttack(CreatureOnBoard attacker, CreatureOnBoard defender)
    {
        if (!attacker.canAttack)
            return new ActionResult(false);

        CreatureOnBoard attackerAfter = new CreatureOnBoard(attacker);
        CreatureOnBoard defenderAfter = new CreatureOnBoard(defender);

        attackerAfter.canAttack = false;
        attackerAfter.hasAttacked = true;

        if (defender.keywords.hasWard) defenderAfter.keywords.hasWard = attacker.attack == 0;
        if (attacker.keywords.hasWard) attackerAfter.keywords.hasWard = defender.attack == 0;

        int damageGiven = defender.keywords.hasWard ? 0 : attacker.attack;
        int damageTaken = attacker.keywords.hasWard ? 0 : defender.attack;
        int healthGain = 0;
        int healthTaken = 0;

        // attacking
        if (damageGiven >= defender.defense) defenderAfter = null;
        if (attacker.keywords.hasBreakthrough && defenderAfter==null) healthTaken = defender.defense - damageGiven;
        if (attacker.keywords.hasLethal && damageGiven > 0) defenderAfter = null;
        if (attacker.keywords.hasDrain && damageGiven > 0) healthGain = attacker.attack;
        if (defenderAfter != null) defenderAfter.defense -= damageGiven;

        // defending
        if (damageTaken >= attacker.defense) attackerAfter = null;
        if (defender.keywords.hasLethal && damageTaken > 0) attackerAfter = null;
        if (attackerAfter != null) attackerAfter.defense -= damageTaken;
        ActionResult result = new ActionResult(attackerAfter == null ? attacker : attackerAfter, defenderAfter == null ? defender : defenderAfter, attackerAfter == null, defenderAfter == null, healthGain, healthTaken);
        result.attackerDefenseChange = -damageTaken;
        result.defenderDefenseChange = -damageGiven;
        return result;
    }

    // when creature attacks player // run it ONLY on legal actions
    public static ActionResult ResolveAttack(CreatureOnBoard attacker)
    {
        if (!attacker.canAttack)
            return new ActionResult(false);

        CreatureOnBoard attackerAfter = new CreatureOnBoard(attacker);

        attackerAfter.canAttack = false;
        attackerAfter.hasAttacked = true;

        int healthGain = attacker.keywords.hasDrain ? attacker.attack : 0;
        int healthTaken = -attacker.attack;

        ActionResult result = new ActionResult(attackerAfter, null, healthGain, healthTaken);
        result.defenderDefenseChange = healthTaken;
        return result;
    }

    // when item is used on a creature // run it ONLY on legal actions
    public static ActionResult ResolveUse(Card item, CreatureOnBoard target)
    {
        CreatureOnBoard targetAfter = new CreatureOnBoard(target);

        if (item.type==Card.Type.ITEM_GREEN) // add keywords
        {
            targetAfter.keywords.hasCharge       = target.keywords.hasCharge       || item.keywords.hasCharge;
            if (item.keywords.hasCharge)
                targetAfter.canAttack = !targetAfter.hasAttacked; // No Swift Strike hack
            targetAfter.keywords.hasBreakthrough = target.keywords.hasBreakthrough || item.keywords.hasBreakthrough;
            targetAfter.keywords.hasDrain        = target.keywords.hasDrain        || item.keywords.hasDrain;
            targetAfter.keywords.hasGuard        = target.keywords.hasGuard        || item.keywords.hasGuard;
            targetAfter.keywords.hasLethal       = target.keywords.hasLethal       || item.keywords.hasLethal;
            //targetAfter.keywords.hasRegenerate   = target.keywords.hasRegenerate   || item.keywords.hasRegenerate;
            targetAfter.keywords.hasWard         = target.keywords.hasWard         || item.keywords.hasWard;
        }
        else // Assumming ITEM_BLUE or ITEM_RED - remove keywords
        {
            targetAfter.keywords.hasCharge       = target.keywords.hasCharge       && !item.keywords.hasCharge;
            targetAfter.keywords.hasBreakthrough = target.keywords.hasBreakthrough && !item.keywords.hasBreakthrough;
            targetAfter.keywords.hasDrain        = target.keywords.hasDrain        && !item.keywords.hasDrain;
            targetAfter.keywords.hasGuard        = target.keywords.hasGuard        && !item.keywords.hasGuard;
            targetAfter.keywords.hasLethal       = target.keywords.hasLethal       && !item.keywords.hasLethal;
            //targetAfter.keywords.hasRegenerate   = target.keywords.hasRegenerate   && !item.keywords.hasRegenerate;
            targetAfter.keywords.hasWard         = target.keywords.hasWard         && !item.keywords.hasWard;
        }

        targetAfter.attack = Math.max(0, target.attack + item.attack);

        if (targetAfter.keywords.hasWard && item.defense < 0)
            targetAfter.keywords.hasWard = false;
        else
            targetAfter.defense += item.defense;
        if (targetAfter.defense <= 0) targetAfter = null;

        int itemgiverHealthChange = item.myHealthChange;
        int targetHealthChange = item.oppHealthChange;

        ActionResult result = new ActionResult(new CreatureOnBoard(item), targetAfter == null ? target : targetAfter, false, targetAfter == null, itemgiverHealthChange, targetHealthChange);
        result.defenderAttackChange  = item.attack;
        result.defenderDefenseChange = item.defense;
        return result;
    }

    // when item is used on a player // run it ONLY on legal actions
    public static ActionResult ResolveUse(Card item)
    {
        int itemgiverHealthChange = item.myHealthChange;
        int targetHealthChange = item.defense + item.oppHealthChange;

        return new ActionResult(null, null, itemgiverHealthChange, targetHealthChange);
    }


}
