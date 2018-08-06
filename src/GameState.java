import java.util.ArrayList;

public class GameState {

    public GameState parent;

    public Gamer currentPlayer = new Gamer();
    public Gamer enemyPlayer = new Gamer();

    public boolean isWinning = false;
    public boolean isLoosing = false;

    ArrayList<Action> actions = new ArrayList<>();

    public GameState() {

    }

    public GameState(GameState oldGameState) {
        GameState gameState = new GameState();
        gameState.currentPlayer = oldGameState.currentPlayer;
        gameState.enemyPlayer = oldGameState.enemyPlayer;
        gameState.actions = oldGameState.actions;
    }

    public GameState(Gamer player, Gamer enemyPlayer){
        this.currentPlayer = player;
        this.enemyPlayer = enemyPlayer;
    }

    public double evaluation(){
        double evaluation = 0;
        double myBoardValue = 0;
        double enemyBoardValue = 0;
        /* TO DO !
            La valeur d'une d'une créature vaut :
            -Si c'est mon tour, le maximum entre sa valeur et la valeur de la plus grosse créature adversaire qu'elle peut tuer
            -Si c'est le tour de mon adversaire, la valeur de la plus petite créature qui peut tuer ma créature
            Pour évaluer la valeur d'un board il faut le calculer comme si c'était le tour de l'adversaire.
            Avoir un deuxième type d'évaluation qui prend en compte combien de tour il nous faut pour tuer l'adversaire
            et combien de cartes/value il nous reste dans notre main (all-in si on voit qu'on va être à court de ressources)*/
        for(CreatureOnBoard creature : currentPlayer.board){
            myBoardValue+=CreatureOnBoard.getValue(creature,currentPlayer.health);
        }
        for(CreatureOnBoard creature : currentPlayer.board){
            enemyBoardValue+=CreatureOnBoard.getValue(creature,enemyPlayer.health);
        }
        evaluation = myBoardValue - enemyBoardValue + currentPlayer.hand.size() - enemyPlayer.handSize - currentPlayer.currentMana;

        return evaluation;
    }

}
