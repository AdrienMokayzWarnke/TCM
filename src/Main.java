import java.util.*;
        import java.io.*;
        import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Main {

    public static final HashMap<Integer, Card> CARDSET = new HashMap<>();

    public static final int[][] idealCurveForMana = new int[12][12];

    public static final int[] myManaCurve = new int[12];

    public static double averageCost = 0;

    public static int turn = 0;

    public static double aggroTypeValue;
    public static double midTypeValue;
    public static double controlTypeValue;

    public static Gamer myPlayer = new Gamer(0, new ArrayList<Card>());
    public static Gamer enemyPlayer = new Gamer(1, new ArrayList<Card>());

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        ArrayList<Card> allCardsFromDraft = new ArrayList<>();
        ArrayList<Card> cardsToPick = new ArrayList<>();

        // game loop
        while (true) {
            if(turn==0){
                createCreatureFile();
                initIdealCurveForMana();
            }

            cardsToPick.clear(); //reset choices
            resetPlayers();
            for (int i = 0; i < 2; i++) {
                int playerHealth = in.nextInt(); /* Update points de vie et mana*/
                int playerMana = in.nextInt();
                int playerDeck = in.nextInt();
                int playerRune = in.nextInt();
                if (i == 0) {
                    myPlayer.health = playerHealth;
                    myPlayer.maxMana = playerMana;
                } else {
                    enemyPlayer.health = playerHealth;
                    enemyPlayer.maxMana = playerMana;
                }
            }
            int opponentHand = in.nextInt();
            enemyPlayer.handSize = opponentHand;
            int cardCount = in.nextInt();
            for (int i = 0; i < cardCount; i++) {
                int cardNumber = in.nextInt();
                int instanceId = in.nextInt();
                int location = in.nextInt();
                int cardType = in.nextInt();
                int cost = in.nextInt();
                int attack = in.nextInt();
                int defense = in.nextInt();
                String abilities = in.next();
                int myHealthChange = in.nextInt();
                int opponentHealthChange = in.nextInt();
                int cardDraw = in.nextInt();

                if (turn < 30) {
                    Card newCard = new Card(CARDSET.get(cardNumber));
                    newCard.draftLocation = i;
                    newCard.id = instanceId;
                    allCardsFromDraft.add(newCard);
                    cardsToPick.add(newCard);
                }else {
                    /*location : 0 : dans la main du joueur actif
                    1 : sur le plateau de jeu, du côté du joueur actif
                    -1 : sur le plateau de jeu, du côté de son adversaire*/
                    Card card = new Card(CARDSET.get(cardNumber));
                    card.id = instanceId;
                    if(location == 0){
                        myPlayer.hand.add(card);
                    }else if (location == 1){
                        Keywords keywords = new Keywords(abilities); //update les key words (silence... perte d'une ward
                        card.keywords = keywords;
                        card.attack = attack; //update attack
                        card.defense = defense; //update hp
                        card.myHealthChange = 0; //cri de guerre ne comptent plus maintenant
                        card.oppHealthChange = 0;
                        CreatureOnBoard summonedCreature = new CreatureOnBoard(card);
                        myPlayer.board.add(summonedCreature);
                    }else{
                        Keywords keywords = new Keywords(abilities); //update les key words (silence... perte d'une ward
                        card.keywords = keywords;
                        card.attack = attack; //update attack
                        card.defense = defense; //update hp
                        card.myHealthChange = 0; //cri de guerre ne comptent plus maintenant
                        card.oppHealthChange = 0;
                        CreatureOnBoard summonedCreature = new CreatureOnBoard(card);
                        enemyPlayer.board.add(summonedCreature);
                    }

                }

                turn++;
            }

            if (turn < 30) {
                Card chosenCard = PickCard(cardsToPick);
                myPlayer.deck.add(chosenCard);
                System.out.println("PICK " + chosenCard.draftLocation);
            }
        }
    }

    private static void resetPlayers() {

        myPlayer.hand.clear();
        enemyPlayer.hand.clear();

        myPlayer.board.clear();
        enemyPlayer.board.clear();

    }

    private static void initIdealCurveForMana() {

        for(int i = 0; i<3;i++){
            idealCurveForMana[i][0]=4;
            idealCurveForMana[i][1]=8;
            idealCurveForMana[i][2]=12;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=0;
            idealCurveForMana[i][5]=0;
            idealCurveForMana[i][6]=0;
            idealCurveForMana[i][7]=0;
            idealCurveForMana[i][8]=0;
            idealCurveForMana[i][9]=0;
            idealCurveForMana[i][10]=0;
            idealCurveForMana[i][11]=0;
        }

        for(int i = 3; i<6;i++){
            idealCurveForMana[i][0]=0;
            idealCurveForMana[i][1]=4;
            idealCurveForMana[i][2]=8;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=4;
            idealCurveForMana[i][5]=4;
            idealCurveForMana[i][6]=2;
            idealCurveForMana[i][7]=1;
            idealCurveForMana[i][8]=1;
            idealCurveForMana[i][9]=0;
            idealCurveForMana[i][10]=0;
            idealCurveForMana[i][11]=0;
        }

        for(int i = 6; i<12;i++){
            idealCurveForMana[i][0]=0;
            idealCurveForMana[i][1]=0;
            idealCurveForMana[i][2]=6;
            idealCurveForMana[i][3]=6;
            idealCurveForMana[i][4]=4;
            idealCurveForMana[i][5]=3;
            idealCurveForMana[i][6]=2;
            idealCurveForMana[i][7]=2;
            idealCurveForMana[i][8]=2;
            idealCurveForMana[i][9]=3;
            idealCurveForMana[i][10]=1;
            idealCurveForMana[i][11]=1;
        }
    }

    private static Card PickCard(ArrayList<Card> cardsToPick) {
        double value = -1;
        Card chosenCard = null;
        for(Card card : cardsToPick){
            double typeBonus =0;
            if(Math.max(Math.max(aggroTypeValue,midTypeValue),controlTypeValue)==aggroTypeValue){
                typeBonus = card.aggro;
            }else if (Math.max(Math.max(aggroTypeValue,midTypeValue),controlTypeValue)==midTypeValue){
                typeBonus = card.mid;
            }else{
                typeBonus = card.control;
            }
            if(card.type== Card.Type.CREATURE){
                typeBonus *= turn / 15;
            }
            double cardValue = Card.getRawCardValue(card) +
                    turn/15 *
                            ( 1 - 1 / (1 + idealCurveForMana[(int)averageCost][card.cost]/myManaCurve[card.cost]
                                    +  typeBonus
                ));
            if(cardValue>value){
                chosenCard = card;
            }
        }
        myManaCurve[chosenCard.cost]+=1;       //update mana curve
        averageCost += chosenCard.cost/turn+1; //update average deck cost
        aggroTypeValue+=chosenCard.aggro;      /* update deck type*/
        midTypeValue+=chosenCard.mid;
        controlTypeValue+=chosenCard.control;
        return chosenCard;
    }


    public static void createCreatureFile(){
        String myString = "1 ; Slimer ; creature ; 1 ; 2 ; 1 ; ------ ; 1 ; 0 ; 0 ; 0 ; 0.25 ; 0.3 ; Summon: You gain 1 health.\n" +
                "2 ; Scuttler ; creature ; 1 ; 1 ; 2 ; ------ ; 0 ; -1 ; 0 ; 0.3 ; 0.1 ; 0 ;Summon: Deal 1 damage to your opponent.\n" +
                "3 ; Beavrat ; creature ; 1 ; 2 ; 2 ; ------ ; 0 ; 0 ; 0 ; 1 ; 0.75 ; 0.75 ;\n" +
                "4 ; Plated Toad ; creature ; 2 ; 1 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0.2 ; 0.2 ; 0.2 ;\n" +
                "5 ; Grime Gnasher ; creature ; 2 ; 4 ; 1 ; ------ ; 0 ; 0 ; 0 ; 0.75 ; 0.2 ; 0 ;\n" +
                "6 ; Murgling ; creature ; 2 ; 3 ; 2 ; ------ ; 0 ; 0 ; 0 ; 0.5 ; 0.5 ; 0.4 ;\n" +
                "7 ; Rootkin Sapling ; creature ; 2 ; 2 ; 2 ; -----W ; 0 ; 0 ; 0 ; 1 ; 1 ; 0.75 ;\n" +
                "8 ; Psyshroom ; creature ; 2 ; 2 ; 3 ; ------ ; 0 ; 0 ; 0 ; 0.3 ; 0.2 ; 0.2 ;\n" +
                "9 ; Corrupted Beavrat ; creature ; 3 ; 3 ; 4 ; ------ ; 0 ; 0 ; 0 ; 0.6 ; 0.5 ; 0.4 ;\n" +
                "10 ; Carnivorous Bush ; creature ; 3 ; 3 ; 1 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.2 ;\n" +
                "11 ; Snowsaur ; creature ; 3 ; 5 ; 2 ; ------ ; 0 ; 0 ; 0 ; 0.65 ; 0.3 ; 0 ;\n" +
                "12 ; Woodshroom ; creature ; 3 ; 2 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0.2 ; 0.3 ; 0.6 ;\n" +
                "13 ; Swamp Terror ; creature ; 4 ; 5 ; 3 ; ------ ; 1 ; -1 ; 0 ; 0.6 ; 0.3 ; 0.2 ; Summon: You gain 1 health and deal\\n1 damage to your opponent.\n" +
                "14 ; Fanged Lunger ; creature ; 4 ; 9 ; 1 ; ------ ; 0 ; 0 ; 0 ; 0.5 ; 0.1 ; 0 ;\n" +
                "15 ; Pouncing Flailmouth ; creature ; 4 ; 4 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0.3 ; 0.6 ; 0.6;\n" +
                "16 ; Wrangler Fish ; creature ; 4 ; 6 ; 2 ; ------ ; 0 ; 0 ; 0 ; 0.55 ; 0.2 ; 0.1 ;\n" +
                "17 ; Ash Walker ; creature ; 4 ; 4 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0.3 ; 0.6 ; 0.6 ;\n" +
                "18 ; Acid Golem ; creature ; 4 ; 7 ; 4 ; ------ ; 0 ; 0 ; 0 ; 0.6 ; 0.7 ; 0.3 ;\n" +
                "19 ; Foulbeast ; creature ; 5 ; 5 ; 6 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.6 ; 0.4 ;\n" +
                "20 ; Hedge Demon ; creature ; 5 ; 8 ; 2 ; ------ ; 0 ; 0 ; 0 ; 0.5 ; 0.3 ; 0 ;\n" +
                "21 ; Crested Scuttler ; creature ; 5 ; 6 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0.2 ; 0.6 ; 0.4 ;\n" +
                "22 ; Sigbovak ; creature ; 6 ; 7 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0.3 ;\n" +
                "23 ; Titan Cave Hog ; creature ; 7 ; 8 ; 8 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.7 ;\n" +
                "24 ; Exploding Skitterbug ; creature ; 1 ; 1 ; 1 ; ------ ; 0 ; -1 ; 0 ; 0.3 ; 0.1 ; 0 ; Summon: Deal 1 damage to your opponent.\n" +
                "25 ; Spiney Chompleaf ; creature ; 2 ; 3 ; 1 ; ------ ; -2 ; -2 ; 0 ; 1 ; 0 ; 0 ; Summon: Deal 2 damage to each player.\n" +
                "26 ; Razor Crab ; creature ; 2 ; 3 ; 2 ; ------ ; 0 ; -1 ; 0 ; 0.7 ; 0.4 ; 0.3 ; Summon: Deal 1 damage to your opponent.\n" +
                "27 ; Nut Gatherer ; creature ; 2 ; 2 ; 2 ; ------ ; 2 ; 0 ; 0 ; 0 ; 0.3 ; 0.7 ;Summon: You gain 2 health.\n" +
                "28 ; Infested Toad ; creature ; 2 ; 1 ; 2 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.5 ; 1 ; Summon: Draw a card.\n" +
                "29 ; Steelplume Nestling ; creature ; 2 ; 2 ; 1 ; ------ ; 0 ; 0 ; 1 ; 0.5 ; 0.4 ; 0.4 ;Summon: Draw a card.\n" +
                "30 ; Venomous Bog Hopper ; creature ; 3 ; 4 ; 2 ; ------ ; 0 ; -2 ; 0 ; 1 ; 0.5 ; 0.2 ; Summon: Deal 2 damage to your opponent.\n" +
                "31 ; Woodland Hunter ; creature ; 3 ; 3 ; 1 ; ------ ; 0 ; -1 ; 0 ; 0.3 ; 0.1 ; 0 ; Summon: Deal 1 damage to your opponent.\n" +
                "32 ; Sandsplat ; creature ; 3 ; 3 ; 2 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.4 ; 0.6 ; Summon: Draw a card.\n" +
                "33 ; Chameleskulk ; creature ; 4 ; 4 ; 3 ; ------ ; 0 ; 0 ; 1 ; 0.3 ; 0.8 ; 0.5 ; Summon: Draw a card.\n" +
                "34 ; Eldritch Cyclops ; creature ; 5 ; 3 ; 5 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.5 ; 0.5 ; Summon: Draw a card.\n" +
                "35 ; Snail-eyed Hulker ; creature ; 6 ; 5 ; 2 ; B----- ; 0 ; 0 ; 1 ; 0 ; 0 ; 0 ; Summon: Draw a card.\n" +
                "36 ; Possessed Skull ; creature ; 6 ; 4 ; 4 ; ------ ; 0 ; 0 ; 2 ; 0.3 ; 1 ; 0.8 ; Summon: Draw two cards.\n" +
                "37 ; Eldritch Multiclops ; creature ; 6 ; 5 ; 7 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.6 ; 0.7 ; Summon: Draw a card.\n" +
                "38 ; Imp ; creature ; 1 ; 1 ; 3 ; --D--- ; 0 ; 0 ; 0 ; 0.2 ; 0.3 ; 0.6 ;\n" +
                "39 ; Voracious Imp ; creature ; 1 ; 2 ; 1 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "40 ; Rock Gobbler ; creature ; 3 ; 2 ; 3 ; --DG-- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.3;\n" +
                "41 ; Blizzard Demon ; creature ; 3 ; 2 ; 2 ; -CD--- ; 0 ; 0 ; 0 ; 0.5 ; 0.5 ; 0.4 ;\n" +
                "42 ; Flying Leech ; creature ; 4 ; 4 ; 2 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.1 ;\n" +
                "43 ; Screeching Nightmare ; creature ; 6 ; 5 ; 5 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0.4 ; 0.7 ;\n" +
                "44 ; Deathstalker ; creature ; 6 ; 3 ; 7 ; --D-L- ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0.7 ;\n" +
                "45 ; Night Howler ; creature ; 6 ; 6 ; 5 ; B-D--- ; -3 ; 0 ; 0 ; 0 ; 0.4 ; 0.7 ; Summon: You lose 3 health.\n" +
                "46 ; Soul Devourer ; creature ; 9 ; 7 ; 7 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.4 ;\n" +
                "47 ; Gnipper ; creature ; 2 ; 1 ; 5 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.1 ;\n" +
                "48 ; Venom Hedgehog ; creature ; 1 ; 1 ; 1 ; ----L- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.8 ;\n" +
                "49 ; Shiny Prowler ; creature ; 2 ; 1 ; 2 ; ---GL- ; 0 ; 0 ; 0 ; 0.4 ; 0.5 ; 0.8 ;\n" +
                "50 ; Puff Biter ; creature ; 3 ; 3 ; 2 ; ----L- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ;\n" +
                "51 ; Elite Bilespitter ; creature ; 4 ; 3 ; 5 ; ----L- ; 0 ; 0 ; 0 ; 0.2 ; 0.6 ; 0.6 ;\n" +
                "52 ; Bilespitter ; creature ; 4 ; 2 ; 4 ; ----L- ; 0 ; 0 ; 0 ; 0.1 ; 0.2 ; 0.2 ;\n" +
                "53 ; Possessed Abomination ; creature ; 4 ; 1 ; 1 ; -C--L- ; 0 ; 0 ; 0 ; 0.3 ; 0.5 ; 0.6 ;\n" +
                "54 ; Shadow Biter ; creature ; 3 ; 2 ; 2 ; ----L- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ;\n" +
                "55 ; Hermit Slime ; creature ; 2 ; 0 ; 5 ; ---G-- ; 0 ; 0 ; 0 ; 0.2 ; 0 ; 0.3 ;\n" +
                "56 ; Giant Louse ; creature ; 4 ; 2 ; 7 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0 ;\n" +
                "57 ; Dream-Eater ; creature ; 4 ; 1 ; 8 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.1 ;\n" +
                "58 ; Darkscale Predator ; creature ; 6 ; 5 ; 6 ; B----- ; 0 ; 0 ; 0 ; 0 ; 0.4 ; 0.3 ;\n" +
                "59 ; Sea Ghost ; creature ; 7 ; 7 ; 7 ; ------ ; 1 ; -1 ; 0 ; 0 ; 0.3 ; 0.5 ; Summon: You gain 1 health and deal\\n1 damage to your opponent.\n" +
                "60 ; Gritsuck Troll ; creature ; 7 ; 4 ; 8 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.3;\n" +
                "61 ; Alpha Troll ; creature ; 9 ; 10 ; 10 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.1;\n" +
                "62 ; Mutant Troll ; creature ; 12 ; 12 ; 12 ; B--G-- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.4;\n" +
                "63 ; Rootkin Drone ; creature ; 2 ; 0 ; 4 ; ---G-W ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ; 0.5;\n" +
                "64 ; Coppershell Tortoise ; creature ; 2 ; 1 ; 1 ; ---G-W ; 0 ; 0 ; 0 ; 0.3 ; 0.3 ; 0.3 ;\n" +
                "65 ; Steelplume Defender ; creature ; 2 ; 2 ; 2 ; -----W ; 0 ; 0 ; 0 ; 1 ; 1 ; 1 ;\n" +
                "66 ; Staring Wickerbeast ; creature ; 5 ; 5 ; 1 ; -----W ; 0 ; 0 ; 0 ; 0.1 ; 0.3 ; 0 ;\n" +
                "67 ; Flailing Hammerhead ; creature ; 6 ; 5 ; 5 ; -----W ; 0 ; -2 ; 0 ; 0.1 ; 0.8 ; 0.2 ; Summon: Deal 2 damage to your opponent.\n" +
                "68 ; Giant Squid ; creature ; 6 ; 7 ; 5 ; -----W ; 0 ; 0 ; 0 ; 0.1 ; 0.7 ; 0.2 ;\n" +
                "69 ; Charging Boarhound ; creature ; 3 ; 4 ; 4 ; B----- ; 0 ; 0 ; 0 ; 0.8 ; 1 ; 0.5 ;\n" +
                "70 ; Murglord ; creature ; 4 ; 6 ; 3 ; B----- ; 0 ; 0 ; 0 ; 0.2 ; 0.2 ; 0 ;\n" +
                "71 ; Flying Murgling ; creature ; 4 ; 3 ; 2 ; BC---- ; 0 ; 0 ; 0 ; 0.5 ; 0.2 ; 0 ;\n" +
                "72 ; Shuffling Nightmare ; creature ; 4 ; 5 ; 3 ; B----- ; 0 ; 0 ; 0 ; 0.2 ; 0.2 ; 0 ;\n" +
                "73 ; Bog Bounder ; creature ; 4 ; 4 ; 4 ; B----- ; 4 ; 0 ; 0 ; 0 ; 0.5 ; 0.8 ; Summon: You gain 4 health.\n" +
                "74 ; Crusher ; creature ; 5 ; 5 ; 4 ; B--G-- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ;\n" +
                "75 ; Titan Prowler ; creature ; 5 ; 6 ; 5 ; B----- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.2 ;\n" +
                "76 ; Crested Chomper ; creature ; 6 ; 5 ; 5 ; B-D--- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.3 ;\n" +
                "77 ; Lumbering Giant ; creature ; 7 ; 7 ; 7 ; B----- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.1 ;\n" +
                "78 ; Shambler ; creature ; 8 ; 5 ; 5 ; B----- ; 0 ; -5 ; 0 ; 0.7 ; 0.3 ; 0 ; Summon: Deal 5 damage to your opponent.\n" +
                "79 ; Scarlet Colossus ; creature ; 8 ; 8 ; 8 ; B----- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.2;\n" +
                "80 ; Corpse Guzzler ; creature ; 8 ; 8 ; 8 ; B--G-- ; 0 ; 0 ; 1 ; 0 ; 0.5 ; 1 ;Summon: Draw a card.\n" +
                "81 ; Flying Corpse Guzzler ; creature ; 9 ; 6 ; 6 ; BC---- ; 0 ; 0 ; 0 ; 0.2 ; 0.5 ; 0.1 ;\n" +
                "82 ; Slithering Nightmare ; creature ; 7 ; 5 ; 5 ; B-D--W ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0.2 ;\n" +
                "83 ; Restless Owl ; creature ; 0 ; 1 ; 1 ; -C---- ; 0 ; 0 ; 0 ; 0.5 ; 0 ; 0 ;\n" +
                "84 ; Fighter Tick ; creature ; 2 ; 1 ; 1 ; -CD--W ; 0 ; 0 ; 0 ; 0.7 ; 0.6 ; 0.5 ;\n" +
                "85 ; Heartless Crow ; creature ; 3 ; 2 ; 3 ; -C---- ; 0 ; 0 ; 0 ; 0.4 ; 0.5 ; 0 ;\n" +
                "86 ; Crazed Nose-pincher ; creature ; 3 ; 1 ; 5 ; -C---- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "87 ; Bloat Demon ; creature ; 4 ; 2 ; 5 ; -C-G-- ; 0 ; 0 ; 0 ; 0.1 ; 0.2 ; 0.2 ;\n" +
                "88 ; Abyss Nightmare ; creature ; 5 ; 4 ; 4 ; -C---- ; 0 ; 0 ; 0 ; 0.7 ; 1 ; 0.4 ;\n" +
                "89 ; Boombeak ; creature ; 5 ; 4 ; 1 ; -C---- ; 2 ; 0 ; 0 ; 0.2 ; 0.1 ; 0.2 ; Summon: You gain 2 health.\n" +
                "90 ; Eldritch Swooper ; creature ; 8 ; 5 ; 5 ; -C---- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "91 ; Flumpy ; creature ; 0 ; 1 ; 2 ; ---G-- ; 0 ; 1 ; 0 ; 0.1 ; 0.2 ; 0.4 ; Summon: Your opponent gains 1 health.\n" +
                "92 ; Wurm ; creature ; 1 ; 0 ; 1 ; ---G-- ; 2 ; 0 ; 0 ; 0 ; 0 ; 0.3 ; Summon: You gain 2 health.\n" +
                "93 ; Spinekid ; creature ; 1 ; 2 ; 1 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "94 ; Rootkin Defender ; creature ; 2 ; 1 ; 4 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.2 ;\n" +
                "95 ; Wildum ; creature ; 2 ; 2 ; 3 ; ---G-- ; 0 ; 0 ; 0 ; 0.3 ; 0.3 ; 0.3 ;\n" +
                "96 ; Prairie Protector ; creature ; 2 ; 3 ; 2 ; ---G-- ; 0 ; 0 ; 0 ; 0.5 ; 0.4 ; 0.2 ;\n" +
                "97 ; Turta ; creature ; 3 ; 3 ; 3 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.2 ;\n" +
                "98 ; Lilly Hopper ; creature ; 3 ; 2 ; 4 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.5 ;\n" +
                "99 ; Cave Crab ; creature ; 3 ; 2 ; 5 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.15 ; 0.7 ;\n" +
                "100 ; Stalagopod ; creature ; 3 ; 1 ; 6 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "101 ; Engulfer ; creature ; 4 ; 3 ; 4 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.1 ;\n" +
                "102 ; Mole Demon ; creature ; 4 ; 3 ; 3 ; ---G-- ; 0 ; -1 ; 0 ; 0.1 ; 0 ; 0 ; Summon: Deal 1 damage to your opponent.\n" +
                "103 ; Mutating Rootkin ; creature ; 4 ; 3 ; 6 ; ---G-- ; 0 ; 0 ; 0 ; 0.2 ; 0.8 ; 1 ;\n" +
                "104 ; Deepwater Shellcrab ; creature ; 4 ; 4 ; 4 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "105 ; King Shellcrab ; creature ; 5 ; 4 ; 6 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.7 ;\n" +
                "106 ; Far-reaching Nightmare ; creature ; 5 ; 5 ; 5 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.2 ;\n" +
                "107 ; Worker Shellcrab ; creature ; 5 ; 3 ; 3 ; ---G-- ; 3 ; 0 ; 0 ; 0 ; 0 ; 0.2 ; Summon: You gain 3 health.\n" +
                "108 ; Rootkin Elder ; creature ; 5 ; 2 ; 6 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "109 ; Elder Engulfer ; creature ; 5 ; 5 ; 6 ; ------ ; 0 ; 0 ; 0 ; 0.1 ; 0.6 ; 0.4 ;\n" +
                "110 ; Gargoyle ; creature ; 5 ; 0 ; 9 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ;\n" +
                "111 ; Turta Knight ; creature ; 6 ; 6 ; 6 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 0.7 ;\n" +
                "112 ; Rootkin Leader ; creature ; 6 ; 4 ; 7 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0.7 ;\n" +
                "113 ; Tamed Bilespitter ; creature ; 6 ; 2 ; 4 ; ---G-- ; 4 ; 0 ; 0 ; 0 ; 0 ; 0.1 ; Summon: You gain 4 health.\n" +
                "114 ; Gargantua ; creature ; 7 ; 7 ; 7 ; ---G-- ; 0 ; 0 ; 0 ; 0 ; 0.1 ; 0.3 ;\n" +
                "115 ; Rootkin Warchief ; creature ; 8 ; 5 ; 5 ; ---G-W ; 0 ; 0 ; 0 ; 0 ; 0 ; 0.1 ;\n" +
                "116 ; Emperor Nightmare ; creature ; 12 ; 8 ; 8 ; BCDGLW ; 0 ; 0 ; 0 ; 0 ; 0 ; 1 ;\n" +
                "117 ; Protein ; itemGreen ; 1 ; 1 ; 1 ; B----- ; 0 ; 0 ; 0 ; 0.2 ; 0.3 ; 0 ; Give a friendly creature +1/+1 and Breakthrough.\n" +
                "118 ; Royal Helm ; itemGreen ; 0 ; 0 ; 3 ; ------ ; 0 ; 0 ; 0 ; 0.1 ; 0.5 ; 0.3 ; Give a friendly creature +0/+3.\n" +
                "119 ; Serrated Shield ; itemGreen ; 1 ; 1 ; 2 ; ------ ; 0 ; 0 ; 0 ; 0.3 ; 0.4 ; 0.3 ; Give a friendly creature +1/+2.\n" +
                "120 ; Venomfruit ; itemGreen ; 2 ; 1 ; 0 ; ----L- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.4 ; Give a friendly creature +1/+0 and Lethal.\n" +
                "121 ; Enchanted Hat ; itemGreen ; 2 ; 0 ; 3 ; ------ ; 0 ; 0 ; 1 ; 0 ; 1 ; 0.5 ;Give a friendly creature +0/+3.\\nDraw a card.\n" +
                "122 ; Bolstering Bread ; itemGreen ; 2 ; 1 ; 3 ; ---G-- ; 0 ; 0 ; 0 ; 0.1 ; 0.4 ; 0.1 ; Give a friendly creature +1/+3 and Guard.\n" +
                "123 ; Wristguards ; itemGreen ; 2 ; 4 ; 0 ; ------ ; 0 ; 0 ; 0 ; 1 ; 0.4 ; 0 ; Give a friendly creature +4/+0.\n" +
                "124 ; Blood Grapes ; itemGreen ; 3 ; 2 ; 1 ; --D--- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ; Give a friendly creature +2/+1 and Drain.\n" +
                "125 ; Healthy Veggies ; itemGreen ; 3 ; 1 ; 4 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0 ; Give a friendly creature +1/+4.\n" +
                "126 ; Heavy Shield ; itemGreen ; 3 ; 2 ; 3 ; ------ ; 0 ; 0 ; 0 ; 0.1 ; 0.3 ; 0.2 ; Give a friendly creature +2/+3.\n" +
                "127 ; Imperial Helm ; itemGreen ; 3 ; 0 ; 6 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ; Give a friendly creature +0/+6.\n" +
                "128 ; Enchanted Cloth ; itemGreen ; 4 ; 4 ; 3 ; ------ ; 0 ; 0 ; 0 ; 0.3 ; 0.6 ; 0 ; Give a friendly creature +4/+3.\n" +
                "129 ; Enchanted Leather ; itemGreen ; 4 ; 2 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0 ; Give a friendly creature +2/+5.\n" +
                "130 ; Helm of Remedy ; itemGreen ; 4 ; 0 ; 6 ; ------ ; 4 ; 0 ; 0 ;  0 ; 0 ; 0 ; Give a friendly creature +0/+6.\\nYou gain 4 health.\n" +
                "131 ; Heavy Gauntlet ; itemGreen ; 4 ; 4 ; 1 ; ------ ; 0 ; 0 ; 0 ; 0.5 ; 0.1 ; 0 ; Give a friendly creature +4/+1.\n" +
                "132 ; High Protein ; itemGreen ; 5 ; 3 ; 3 ; B----- ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0 ; Give a friendly creature +3/+3 and Breakthrough.\n" +
                "133 ; Pie of Power ; itemGreen ; 5 ; 4 ; 0 ; -----W ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0 ;  Give a friendly creature +4/+0 and Ward.\n" +
                "134 ; Light The Way ; itemGreen ; 4 ; 2 ; 2 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.4 ; 0 ; Give a friendly creature +2/+2.\\nDraw a card.\n" +
                "135 ; Imperial Armour ; itemGreen ; 6 ; 5 ; 5 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.1 ; Give a friendly creature +5/+5.\n" +
                "136 ; Buckler ; itemGreen ; 0 ; 1 ; 1 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ; Give a friendly creature +1/+1.\n" +
                "137 ; Ward ; itemGreen ; 2 ; 0 ; 0 ; -----W ; 0 ; 0 ; 0 ; 0 ; 0 ; 0 ; Give a friendly creature Ward.\n" +
                "138 ; Grow Horns ; itemGreen ; 2 ; 0 ; 0 ; ---G-- ; 0 ; 0 ; 1 ; 0 ; 0.2 ; 0.4 ; Give a friendly creature Guard.\\nDraw a card.\n" +
                "139 ; Grow Stingers ; itemGreen ; 4 ; 0 ; 0 ; ----LW ; 0 ; 0 ; 0 ; 0.4 ; 0.5 ; 0.1 ;  Give a friendly creature Lethal and Ward.\n" +
                "140 ; Grow Wings ; itemGreen ; 2 ; 0 ; 0 ; -C---- ; 0 ; 0 ; 0 ; 0.7 ; 0.2 ; 0.3 ; Give a friendly creature Charge.\n" +
                "141 ; Throwing Knife ; itemRed ; 0 ; -1 ; -1 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.2 ; 0.5 ; Give an enemy creature -1/-1.\n" +
                "142 ; Staff of Suppression ; itemRed ; 0 ; 0 ; 0 ; BCDGLW ; 0 ; 0 ; 0 ; 1 ; 0.5 ; 0.7 ; Remove all abilities from an enemy creature.\n" +
                "143 ; Pierce Armour ; itemRed ; 0 ; 0 ; 0 ; ---G-- ; 0 ; 0 ; 0 ; 1 ; 0.3 ; 0 ; Remove Guard from an enemy creature.\n" +
                "144 ; Rune Axe ; itemRed ; 1 ; 0 ; -2 ; ------ ; 0 ; 0 ; 0 ; 0.4 ; 0.5 ; 1 ; Deal 2 damage to an enemy creature.\n" +
                "145 ; Cursed Sword ; itemRed ; 3 ; -2 ; -2 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.3 ; 0.6 ; Give an enemy creature -2/-2.\n" +
                "146 ; Cursed Scimitar ; itemRed ; 4 ; -2 ; -2 ; ------ ; 0 ; -2 ; 0 ; 0 ; 0 ; 0; Give an enemy creature -2/-2.\\nDeal 2 damage to your opponent.\n" +
                "147 ; Quick Shot ; itemRed ; 2 ; 0 ; -1 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.1 ; 0.4 ; Deal 1 damage to an enemy creature.\\nDraw a card.\n" +
                "148 ; Helm Crusher ; itemRed ; 2 ; 0 ; -2 ; BCDGLW ; 0 ; 0 ; 0 ; 0.4 ; 0.5 ; 0.7 ; Remove all abilities from an enemy creature,\\nthen deal 2 damage to it.\n" +
                "149 ; Rootkin Ritual ; itemRed ; 3 ; 0 ; 0 ; BCDGLW ; 0 ; 0 ; 1 ; 0.1 ; 0.1 ; 0.2 ; Remove all abilities from an enemy creature.\\nDraw a card.\n" +
                "150 ; Throwing Axe ; itemRed ; 2 ; 0 ; -3 ; ------ ; 0 ; 0 ; 0 ; 0 ; 0.5 ; 1 ; Deal 3 damage to an enemy creature.\n" +
                "151 ; Decimate ; itemRed ; 5 ; 0 ; -99 ; BCDGLW ; 0 ; 0 ; 0 ; 0 ; 0.4 ; 1 ; Remove all abilities from an enemy creature,\\nthen deal 99 damage to it.\n" +
                "152 ; Mighty Throwing Axe ; itemRed ; 7 ; 0 ; -7 ; ------ ; 0 ; 0 ; 1 ; 0 ; 0.2 ; 0.6 ; Deal 7 damage to an enemy creature.\\nDraw a card.\n" +
                "153 ; Healing Potion ; itemBlue ; 2 ; 0 ; 0 ; ------ ; 5 ; 0 ; 0 ; 0 ; 0 ; 0.4 ; Gain 5 health.\n" +
                "154 ; Poison ; itemBlue ; 2 ; 0 ; 0 ; ------ ; 0 ; -2 ; 1 ; 1 ; 0.1 ; 0.2 ;  Deal 2 damage to your opponent.\\nDraw a card.\n" +
                "155 ; Scroll of Firebolt ; itemBlue ; 3 ; 0 ; -3 ; ------ ; 0 ; -1 ; 0 ; 0.4 ; 0.1 ; 0.5 ; Deal 3 damage.\\nDeal 1 damage to your opponent\n" +
                "156 ; Major Life Steal Potion ; itemBlue ; 3 ; 0 ; 0 ; ------ ; 3 ; -3 ; 0 ; 0.2 ; 0.1 ; 0.1 ; Deal 3 damage to your opponent and gain 3 health.\n" +
                "157 ; Life Sap Drop ; itemBlue ; 3 ; 0 ; -1 ; ------ ; 1 ; 0 ; 1 ; 0.1 ; 0.1 ; 0.1 ; Deal 1 damage, gain 1 health, and draw a card.\n" +
                "158 ; Tome of Thunder ; itemBlue ; 3 ; 0 ; -4 ; ------ ; 0 ; 0 ; 0 ; 1 ; 1 ; 1 ; Deal 4 damage.\n" +
                "159 ; Vial of Soul Drain ; itemBlue ; 4 ; 0 ; -3 ; ------ ; 3 ; 0 ; 0 ; 0.1 ; 0.1 ; 0.3 ; Deal 3 damage and gain 3 health.\n" +
                "160 ; Minor Life Steal Potion ; itemBlue ; 2 ; 0 ; 0 ; ------ ; 2 ; -2 ; 0 ; 0.2 ; 0.1 ; 0.1 ; Deal 2 damage to your opponent and gain 2 health.\n";
        InputStream is = new ByteArrayInputStream( myString.getBytes() );
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("//.*", "").trim();
                if (line.length() > 0) {
                    Card c = new Card(line.split("\\s*;\\s*"));
                    CARDSET.put(c.baseId, c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}