import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Spielfeld sf = new Spielfeld();
        sf.scanMap();
        GameState gameState = new GameState(sf);
        gameState.ownPlayer = gameState.playerList.get(0);
        ArrayList<Player> ranked = gameState.getCurrentRanking();
        for (Player player : ranked){
            System.out.println("Before Bomb");
            System.out.println("ID: " + player.playerIdChar);
            System.out.println("Ranking: " + player.ranking);
            System.out.println("Points: " + player.points);
        }
        byte[] target = gameState.getBombTarget();
        System.out.println("Target x: " + (int) target[0] + " y: " + (int) target[1]);
        gameState.throwBomb((int) target[0], (int) target[1], gameState.bombenStaerke);
        ranked = gameState.getCurrentRanking();
        for (Player player : ranked){
            System.out.println();
            System.out.println("After Bomb");
            System.out.println("ID: " + player.playerIdChar);
            System.out.println("Ranking: " + player.ranking);
            System.out.println("Points: " + player.points);
        }
    }
}
