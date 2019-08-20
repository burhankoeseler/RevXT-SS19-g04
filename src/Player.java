public class Player implements Comparable<Player>{
    public char playerIdChar;
    public int playerId;
    public int points = 0;
    public int ranking = 0;
    public int bombs;
    public int overrides;
    public boolean overridesShouldBeUsed=false; //Überschreibsteine sollten nur am ende des Spiels verwendet werden oder wenn es ohne keine possibleMoves gibt
    public boolean diqualified=false;

    public Player(int playerId, int bombs, int overrides){
        this.bombs = bombs;
        this.overrides = overrides;
        this.playerId=playerId;
        this.playerIdChar=Integer.toString(playerId).charAt(0);
    }

    public Player(Player other) {
        this.playerIdChar = other.playerIdChar;
        this.points = other.points;
        this.ranking = other.ranking;
        this.playerId = other.playerId;
        this.diqualified = other.diqualified;
        this.overrides=other.overrides;
    }

    @Override
    public int compareTo(Player player){
        return Integer.compare( player.points, this.points);
    }
}
