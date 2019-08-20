import java.util.ArrayList;
import java.util.List;

public class SimpleRating {

    Statistics statistics;
    SimpleRating(Statistics st){
        this.statistics=st;
    }
    SimpleRating(){};

    public int getSimpleRatingCountStones(GameState gameState, char player) {
        //int rating= gameState.showMoves(gameState.getPlayerIDChar()).size();
        int rating = countStones(gameState, player);
        return rating;
    }
    public int getSimpleRatingMobility(GameState gameState){
        int rating = gameState.showMoves(gameState.ownPlayer.playerIdChar, false).size();
        //statistics.allPossibleMovesInBottomRow+=rating;
        return rating;
    }

    public int getSimpleRatingMoveSort(GameState gameState, char player) {
        int rating = countStones(gameState, player);
        return rating;
    }

    public int getSimpleRatingStaticThingsOld(char player, GameState gameState, int[][] fieldValues) {
        int rating=0;
        for(int y=0;y<gameState.sf.spielfeldhoehe;y++){
            for(int x=0;x<gameState.sf.spielfeldbreite;x++){
                if(gameState.sf.spielfeld[x][y]==player){
                    rating+=fieldValues[x][y];
                }
            }
        }
        return rating;
    }

    public int getSimpleRatingStaticThings(char player, GameState gameState, List<int[]> corners, List<int[]> boarders) {
        int rating = 0;
        int cornersInPossession = 0;
        int boarderFieldsInPossession = 0;

        for (int y = 0; y < gameState.sf.spielfeldhoehe; y++) {
            for (int x = 0; x < gameState.sf.spielfeldbreite; x++) {
                if (gameState.sf.spielfeld[x][y] == player) {
                    for (int[] corner : corners) {
                        if (x == corner[0] && y == corner[1]) {
                            cornersInPossession++;
                            break;
                        }
                    }
                    for (int[] boarderField : boarders) {
                        if (x == boarderField[0] && y == boarderField[1]) {
                            boarderFieldsInPossession++;
                            break;
                        }
                    }
                }
            }
            //System.out.println("Corners: "+cornersInPossession+"  Boarders: "+boarderFieldsInPossession);
        }

        rating = RatingValues.gewichtungCorner * cornersInPossession + boarderFieldsInPossession;
        //System.out.println("Ergebnis Static things:"+rating);
        return rating;
    }

    public int getSimpleRatingBonusFields(List<int[]> bonusFields, List<int[]> goodDistanceForBonusFields, List<int[]> badDistanceForBonusFields, GameState gameState){
        int bonusfieldsTaken =0;
        int fieldsWithGoodDistanceTaken=0;
        int fieldswithBadDistanceTaken=0;

        for (int[] bonusField: bonusFields){
            if(gameState.sf.spielfeld[bonusField[0]][bonusField[1]] == gameState.ownPlayer.playerIdChar){
                bonusfieldsTaken++;
            }
        }

        for (int[] field: goodDistanceForBonusFields){
            if(gameState.sf.spielfeld[field[0]][field[1]] == gameState.ownPlayer.playerIdChar){
                fieldsWithGoodDistanceTaken++;
            }
        }

        if(bonusfieldsTaken==0){
            for (int[] field: badDistanceForBonusFields){
                if(gameState.sf.spielfeld[field[0]][field[1]] == gameState.ownPlayer.playerIdChar){
                    fieldswithBadDistanceTaken++;
                }
            }
        }

        int rating=(bonusfieldsTaken*RatingValues.bonusfieldsTakenValue)  + (fieldsWithGoodDistanceTaken*RatingValues.fieldsWithGoodDistanceTakenValue) + (fieldswithBadDistanceTaken*RatingValues.fieldswithBadDistanceTakenValue);

        /*System.out.println("bonusfieldsTaken*RatingValues.bonusfieldsTakenValue: "+bonusfieldsTaken*RatingValues.bonusfieldsTakenValue);
        System.out.println("fieldsWithGoodDistanceTaken*RatingValues.fieldsWithGoodDistanceTakenValue: "+fieldsWithGoodDistanceTaken*RatingValues.fieldsWithGoodDistanceTakenValue);
        System.out.println("fieldswithBadDistanceTaken*RatingValues.fieldswithBadDistanceTakenValue: "+fieldswithBadDistanceTaken*RatingValues.fieldswithBadDistanceTakenValue);
        */
        return rating;

    }

    public double getSimpleRatingHierarchy(GameState gameState,ArrayList<Player> playerHierarchyAtBeginOfCurrentMove){
        ArrayList<Player> currentPlayerHierarchy=gameState.getCurrentRanking();

        int newIndex=0;
        int oldIndex=0;
        for (int i=0;i<currentPlayerHierarchy.size();i++){
            if(currentPlayerHierarchy.get(i).playerIdChar==gameState.ownPlayer.playerIdChar){
                newIndex=i;
                break;
            }
        }
        for (int i=0;i<playerHierarchyAtBeginOfCurrentMove.size();i++){
            if(playerHierarchyAtBeginOfCurrentMove.get(i).playerIdChar==gameState.ownPlayer.playerIdChar){
                oldIndex=i;
                break;
            }
        }


        if(newIndex==(oldIndex-1)){
            return 1.5;

        }
        if(newIndex<oldIndex){
            return 1.9;
        }
        if(newIndex>oldIndex){
            return 0.5;
        }


        return 1;
    }

    private int countStones(GameState gs, char player) {
        int counter=0;
        for(char[] element : gs.sf.spielfeld){
            for (char value:element){
                if (value == player) {
                    counter++;
                }
            }
        }
        return counter;
    }
}
