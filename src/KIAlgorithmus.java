import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.lang.System.exit;


public class KIAlgorithmus {

    //Evaluation
    ComplexRating complexRating;

    private GameState gameState;
    private int depth=0;
    private int currentMaxDepth=1;
    public int maxDepth;
    private Integer[] bestMove=new Integer[2];
    private int highestRatingOfLastIteration=0;
    private int aspirationWindowSize=2;
    private Statistics statistics;
    private boolean alphaBeta;
    private boolean moveSort;
    public  boolean moveSortComplete;
    private boolean iterativeDeepening;
    private boolean aspirationWindow;
    private boolean repeatedBecauseOfAspirationWindowFail =false;
    private ArrayList<Integer> mobilities=new ArrayList<>();
    ArrayList<Player> playerHierarchyAtBeginOfMove;
    private boolean shortCut=false;


    KIAlgorithmus(GameState gameState, Statistics statistics){
        this.gameState = gameState;
        this.maxDepth = 1;
        this.statistics=statistics;
        this.complexRating=new ComplexRating(gameState,statistics);
        this.moveSort=GameInfo.sortMoves;
        this.moveSortComplete=GameInfo.sortMoves;
        this.alphaBeta=GameInfo.aplphaBetaPruning;
        this.aspirationWindow=GameInfo.aspirationWindow;
        bestMove[0]=-1;//Um festzustellen ob Algorithmus immer gültige Werte liefert
        bestMove[1]=-1;
        //this.moveSort=GameInfo.sortMoves;
    }



    public Integer[] getBestMoveIterativeDeepening(){
        complexRating.updateFieldValues();//muss an erster Stelle stehen
        complexRating.updateStartValues(new GameState(gameState));
        //complexRating.outputReachableBonusFields(new GameState(gameState));
        this.playerHierarchyAtBeginOfMove=gameState.getCurrentRanking();
        statistics.aspirationWindowFails=0;
        statistics.aspirationWindowSucesses=0;
        this.currentMaxDepth=1;
        statistics.lastDepth=0;
        statistics.guessedTimeoutAt=0;
        statistics.ExceptionThrown=0;
        statistics.highestReachedDepth=0;
        long timeBeforeStaticUpdate=System.currentTimeMillis();

        statistics.timeToUpdateStaticValues=(System.currentTimeMillis()-timeBeforeStaticUpdate);//muss hier passieren


        Integer[] bestMove;
        if(GameInfo.iterativeDeepening&&GameInfo.timeLimitNeeded){
            int alpha=Integer.MIN_VALUE;
            int beta=Integer.MAX_VALUE;
            for(int i=1;i<100;i++){
                if (i == 99) {
                    System.out.println("TIEFE 99");
                }
                this.currentMaxDepth=i;
                try {
                    //if(Math.pow(System.currentTimeMillis()-GameInfo.startOfCurrentMove.getTime(),2)<(GameInfo.customTimeLimit-(System.currentTimeMillis()-GameInfo.startOfCurrentMove.getTime()))) {

                    if(enoughTimeForNextDepth(i)){//TODO Zurückändern //Wenn iterative deepening aus, dann soll immer die nächste Tiefe abgesucht werden
                          //System.out.println("depth: "+i);

                        if(aspirationWindow) {
                            if (i > 1 && !this.repeatedBecauseOfAspirationWindowFail) {//Wird ab der zweiten Iterationsebene ausgeführt und übersprungen falls wegen eines Aspiration Windows nochmal gesucht wird
                                alpha = this.highestRatingOfLastIteration - this.aspirationWindowSize;
                                beta = this.highestRatingOfLastIteration + this.aspirationWindowSize;
                            }
                        }

                        this.bestMove = getBestMove(alpha,beta);

                        statistics.setLastDepth(i);
                        if(shortCut){
                            shortCut=false;
                            return this.bestMove;
                        }
                        if (GameInfo.usingOverrideForThisMove) {
                            break;
                        }
                        if(aspirationWindow) {
                            if(i>1) {//Muss bei der erstenIteration mit tiefe 1 noch nicht gemacht werden
                                if (this.highestRatingOfLastIteration < alpha || this.highestRatingOfLastIteration > beta) {
                                    alpha = Integer.MIN_VALUE;
                                    beta = Integer.MAX_VALUE;
                                    statistics.aspirationWindowFails += 1;
                                    this.repeatedBecauseOfAspirationWindowFail = true;
                                    continue;
                                } else {
                                    if (!repeatedBecauseOfAspirationWindowFail) {//Wenn es eine wiederholung ist weil die aspiration Windows nicht gestimmt haben liegt der Wert auf jeden Fall im aspiration Window weil der gesamte Baum durchsucht wurde
                                        statistics.aspirationWindowSucesses += 1;
                                    }
                                }
                                if (repeatedBecauseOfAspirationWindowFail) {
                                    repeatedBecauseOfAspirationWindowFail = false;
                                }
                            }
                        }

                    }
                    else{
                        //if(GameInfo.notTestMode)System.out.println("Break at Time: "+(GameInfo.customTimeLimit-(System.currentTimeMillis()-GameInfo.startOfCurrentMove.getTime()))+"With "+statistics.nodeCounter+" Nodes");

                       return this.bestMove;
                    }
                }
                catch(TimeoutException e){
                    //if(GameInfo.notTestMode)System.out.println("threw Exception");
                    statistics.leftFunctionWithException=System.currentTimeMillis()-GameInfo.startOfCurrentMove.getTime();
                    statistics.timeWhenleavingIterativeDeppening=System.currentTimeMillis();
                    return this.bestMove;
                }


                //if(GameInfo.notTestMode)System.out.println("maxDepth:"+this.currentMaxDepth+"   "+this.bestMove[0]+"  "+this.bestMove[1]);

            }

        }
        else if(GameInfo.iterativeDeepening&&!GameInfo.timeLimitNeeded){//iterativedeepening with just depthlimit
            int alpha=Integer.MIN_VALUE;
            int beta=Integer.MAX_VALUE;
            for(int i=1;i<=this.maxDepth;i++){
                this.currentMaxDepth=i;
                try {

                        if(aspirationWindow) {
                            if (i > 1 && !this.repeatedBecauseOfAspirationWindowFail) {//Wird ab der zweiten Iterationsebene ausgeführt und übersprungen falls wegen eines Aspiration Windows nochmal gesucht wird
                                alpha = this.highestRatingOfLastIteration - this.aspirationWindowSize;
                                beta = this.highestRatingOfLastIteration + this.aspirationWindowSize;
                            }
                        }
                        enoughTimeForNextDepth(i);
                        this.bestMove = getBestMove(alpha,beta);
                        if(shortCut){
                            shortCut=false;
                            return this.bestMove;
                        }

                        statistics.setLastDepth(i);
                        statistics.depthLog();

                        if(aspirationWindow) {
                            if(i>1) {//Muss bei der erstenIteration mit tiefe 1 noch nicht gemacht werden
                                if (this.highestRatingOfLastIteration < alpha || this.highestRatingOfLastIteration > beta) {
                                    alpha = Integer.MIN_VALUE;
                                    beta = Integer.MAX_VALUE;
                                    statistics.aspirationWindowFails += 1;
                                    this.repeatedBecauseOfAspirationWindowFail = true;
                                    continue;
                                } else {
                                    if (!repeatedBecauseOfAspirationWindowFail) {//Wenn es eine wiederholung ist weil die aspiration Windows nicht gestimmt haben liegt der Wert auf jeden Fall im aspiration Window weil der gesamte Baum durchsucht wurde
                                        statistics.aspirationWindowSucesses += 1;
                                    }
                                }
                                if (repeatedBecauseOfAspirationWindowFail) {
                                    repeatedBecauseOfAspirationWindowFail = false;
                                }
                            }
                        }
                }
                catch(TimeoutException e){
                    //if(GameInfo.notTestMode)System.out.println("threw Exception");
                    exit(-2);//Darf keine Exception schmeißen weil er nur iterative Tiefensuche macht
                    return this.bestMove;
                }


                //if(GameInfo.notTestMode)System.out.println("maxDepth:"+this.currentMaxDepth+"   "+this.bestMove[0]+"  "+this.bestMove[1]);

            }
        }
        else {
            this.currentMaxDepth = this.maxDepth;
            try {
                this.bestMove = getBestMove(Integer.MIN_VALUE, Integer.MAX_VALUE);
            } catch (TimeoutException e) {
                System.out.println("Should not happen Ki 1");
                exit(-1);
                return this.bestMove;
            }
        }
        return  this.bestMove;
    }


    private boolean enoughTimeForNextDepth(int nextDepth){
        boolean prediction=false;
        long timeForNextDepth=0;
        if (nextDepth>1) {
            if(statistics.nodeCounter!=0&&statistics.bottomRowCounter!=0) {
                long timeLeft = GameInfo.customTimeLimit - (System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime());
                long timeForBuildingTheTree=0;
                if (nextDepth>2&&(statistics.nodeCounter - statistics.bottomRowCounter!=0)) {
                    timeForBuildingTheTree = (statistics.totalTimeForLastDepth - statistics.timeNeededForBottomRow) / (statistics.nodeCounter - statistics.bottomRowCounter) * statistics.nodeCounter;
                }
                double averageTimeForBottomNodes = (double) statistics.timeNeededForBottomRow / (double) statistics.bottomRowCounter;
                timeForNextDepth = (long) (statistics.allPossibleMovesInBottomRow * averageTimeForBottomNodes) + timeForBuildingTheTree + statistics.timeToUpdateStaticValues;
                prediction=timeLeft>timeForNextDepth;
            }
            else{
                //System.out.println("statistics.nodeCounter==0||statistics.bottomRowCounter==0 in move "+statistics.move);
            }
            //System.out.println("Time For next Depth: "+timeForNextDepth);


            /*if(GameInfo.notTestMode){
                System.out.println("---------------------------------enoughTimeForNextDepth-----------------------------------");
                System.out.println("Tiefe: "+nextDepth);
                System.out.println("timeForBuildingTheTree: "+timeForBuildingTheTree);
                System.out.println("statistics.totalTimeForLastDepth: "+statistics.totalTimeForLastDepth);
                System.out.println("statistics.timeNeededForBottomRow:"+statistics.timeNeededForBottomRow);
                System.out.println("statistics.nodeCounter:"+statistics.nodeCounter);
                System.out.println("statistics.bottomRowCounter: "+statistics.bottomRowCounter);
                System.out.println("averageTimeForBottomNodes: "+ averageTimeForBottomNodes);
                System.out.println("timeForNextDepth: "+ timeForNextDepth);
                System.out.println("statistics.bottomRowCounter:"+statistics.bottomRowCounter);
                System.out.println("statistics.allPossibleMovesInBottomRow: "+statistics.allPossibleMovesInBottomRow);
                System.out.println("averageTimeForBottomNodes: "+averageTimeForBottomNodes);
                System.out.println("---------------------------------EnoughTimeForNextDepthENDE-----------------------------------");
                System.out.println();
            }*/
            if (!prediction){
                statistics.guessedTimeoutAt=System.currentTimeMillis()-GameInfo.startOfCurrentMove.getTime();
                statistics.guessedTimeForNextDepth=timeForNextDepth;
            }
            //System.out.println("Prediction: The next Depth will run through: "+prediction+"  Estimated Time: "+(currentDuration+timeForNextDepth));
            return prediction;

        }


        return true;
    }

    public Integer[] getBestMove(int alpha,int beta) throws TimeoutException{
        if(GameInfo.timeLimitNeeded)checkTimelimit(1);
        /*Sonderabfragen die nichts mit dem ALgorithmus zu tun haben*/

        statistics.resetNodeCounter();
        statistics.resetBottomRowCounter();
        statistics.resetBottomRowCounter();
        statistics.resetBottomRowTime();
        statistics.allPossibleMovesInBottomRow=0;
        long timeForThisDepthStart=System.currentTimeMillis();


        /*Algorithmus*/

        int v = Integer.MIN_VALUE;
        this.depth = 1;
        char currentPlayer = gameState.ownPlayer.playerIdChar;

        Integer[] bestMove={-2,-2};
        GameState originalGameState=new GameState(gameState);//
        //System.out.println("Distance: "+ complexRating.calculateDistancetoNearestBonus(originalGameState));
        GameState bestChoice=originalGameState;
        ArrayList<Integer[]> possibleMoves = originalGameState.showMoves(currentPlayer, true);
        possibleMoves = sortOutBadMoves(possibleMoves, complexRating.getFieldswithBadDistanceTooBonus());
        statistics.possibleMovesNumber=possibleMoves.size();

        Integer[] moveIfBonusFieldIsReachable=originalGameState.checkForPossibleBonusFields(possibleMoves);
        if(moveIfBonusFieldIsReachable[0]!=-1&&!GameInfo.usingOverrideForThisMove){
            //System.out.println("bonus!");
            this.shortCut=true;
            return moveIfBonusFieldIsReachable;
        }

        ArrayList<Integer[]> possibleMovesWithCorners = complexRating.checkForPossibleCorner(possibleMoves, gameState);
        if (possibleMovesWithCorners.size() != 0 && !GameInfo.usingOverrideForThisMove) {
            //System.out.println("corner!");
            possibleMoves = possibleMovesWithCorners;
            if (possibleMoves.size() == 1) {
                shortCut = true;
                return possibleMoves.get(0);
            }

        }

        Integer[] moveIfFieldNearBonusIsReachable=complexRating.checkForPossibleFieldNearBonus(possibleMoves,gameState);
        if(moveIfFieldNearBonusIsReachable[0]!=-1&&!GameInfo.usingOverrideForThisMove){
            //System.out.println("NearBonus!");
            this.shortCut=true;
            return moveIfFieldNearBonusIsReachable;
        }

        if(this.currentMaxDepth==1&&possibleMoves.size()!=0)this.bestMove=possibleMoves.get(0);//Speichert ersten gefundenen Zug ab (Notfall)
        this.mobilities.add(possibleMoves.size());
        ArrayList<GameState> sortedGameStates = new ArrayList<GameState>();

        int ratingOfKIAlgorithm;


        if(GameInfo.timeLimitNeeded)checkTimelimit(2);
        if (possibleMoves.size() != 0) {
            GameState newGameState;

            /*Zugsortierung*/
            {
                for (Integer[] cmove : possibleMoves) {

                    newGameState = new GameState(originalGameState);
                    newGameState.makeTurn(currentPlayer, cmove[0], cmove[1]);
                    sortedGameStates.add(newGameState);
                }
                if (this.moveSort) {
                    if(GameInfo.timeLimitNeeded)checkTimelimit(3);
                    Date sortTime=new Date();
                    Collections.sort(sortedGameStates);//TODO frisst extrem viel Zeit -->wenn nur niedrige Zeitschranken angegeben sind Movesort ausschalten!
                    if(GameInfo.notTestMode)System.out.println("Sortierzeit: "+(new Date().getTime()-sortTime.getTime()));
                    if(GameInfo.timeLimitNeeded)checkTimelimit(4);
                }
                bestMove = sortedGameStates.get(0).lastMove; //BestMove wird mit dem erstmöglichen Zug initialisiert

            }
            /*Zugsortierung Ende*/

            /*Rekursions Algorithmus*/
            if(GameInfo.timeLimitNeeded)checkTimelimit(5);
            for(GameState gs: sortedGameStates){
                /*if(complexRating.PositiveChangeInHierarchy(gs,playerHierarchyAtBeginOfMove)&&GameInfo.usingOverrideForThisMove){
                    this.shortCut=true;
                    return gs.lastMove;
                }*/
                if(GameInfo.timeLimitNeeded)checkTimelimit(6);
                if (this.currentMaxDepth > 1) {//Wenn es Tiefer gehen soll als in erster Ebenen
                    this.depth++;
                    ratingOfKIAlgorithm = maxMin(alpha,beta,getNextPlayer(currentPlayer), gs);
                }
                else {
                    statistics.increaseNodeCounter();
                    statistics.incrementBottomRowCounter();
                    ratingOfKIAlgorithm=complexRating.ratingForBottomNodes(gs, playerHierarchyAtBeginOfMove);//Wenn nur eine Ebene darunter gesucht werden soll//TODO Rating ändern
                }
                if (ratingOfKIAlgorithm > v) {//Ist immer Maximier
                    v = ratingOfKIAlgorithm;
                    bestMove=gs.lastMove;

                    /*AlphaBetaPruning*/
                    if(alphaBeta&&ratingOfKIAlgorithm>alpha) {
                        alpha = ratingOfKIAlgorithm;
                    }
                    /*AlphaBetaPruning Ende*/
                }
            }
            this.highestRatingOfLastIteration=v;//Höchste Bewertung der aktuellen Iterationsstufe wird gespeichert-->Wichtig für aspiration Window;
            /*Rekursions Algorithmus Ende*/
        }
        else{//possible moves =0
            if(GameInfo.notTestMode) System.out.println("Darf nicht passieren (Eigener Spieler hätte keinen Zug mehr und dürfte gar nicht drankommen)");
        }

        statistics.updateTotalTimeForLastDepth(System.currentTimeMillis()-timeForThisDepthStart);

        return bestMove;
    }

    public int maxMin(int alpha, int beta,char currentPlayer, GameState previousGameState) throws TimeoutException{
        /*Sonderabfragen die nichts mit dem ALgorithmus zu tun haben*/
        statistics.increaseNodeCounter();

        /*Algorithmus*/
        boolean maximzer;
        int v;

        if (currentPlayer == gameState.ownPlayer.playerIdChar) {
            maximzer = true;
            v =Integer.MIN_VALUE;
        } else {
            maximzer = false;
            v =Integer.MAX_VALUE;
        }
        int ratingOfKIAlgorithm;
        ArrayList<Integer[]> possibleMoves = previousGameState.showMoves(currentPlayer, false);
        this.mobilities.add(possibleMoves.size());
        ArrayList<GameState> sortedGameStates = new ArrayList<GameState>();




        if (this.depth < this.currentMaxDepth) {

            if(GameInfo.timeLimitNeeded)checkTimelimit(7);

            if (possibleMoves.size() != 0) {
                GameState newGameState;
                /*Zugsortierung*/
                {
                    for (Integer[] cmove : possibleMoves) {


                        newGameState = new GameState(previousGameState);
                        newGameState.makeTurn(currentPlayer, cmove[0], cmove[1]);
                        sortedGameStates.add(newGameState);
                    }


                    if (this.moveSortComplete) {
                        if (maximzer) {
                            Collections.sort(sortedGameStates);
                        } else {
                            Collections.sort(sortedGameStates, Collections.reverseOrder());
                        }
                    }
                }
                /*Zugsortierung Ende*/

                for (GameState gs:sortedGameStates) {
                    if(GameInfo.timeLimitNeeded)checkTimelimit(8);
                    this.depth++;
                    ratingOfKIAlgorithm = maxMin(alpha,beta,getNextPlayer(currentPlayer), gs);
                    if (maximzer) {
                        if (ratingOfKIAlgorithm > v) {
                            v = ratingOfKIAlgorithm;

                            /*AlphaBetaPruning*/
                            if(alphaBeta) {
                                alpha = ratingOfKIAlgorithm;
                                if (v >= beta) {
                                    break;
                                }
                            }
                            /*AlphaBetaPruning Ende*/

                        }



                    } else {
                        if (ratingOfKIAlgorithm < v) {
                            v = ratingOfKIAlgorithm;

                            /*AlphaBetaPruning*/
                            if(alphaBeta) {
                                beta = ratingOfKIAlgorithm;
                                if (v <= alpha) {
                                    break;
                                }
                            }
                            /*AlphaBetaPruningEnde*/

                        }
                    }


                }

            }
            else {//possible Moves=0
                this.depth++;
                ratingOfKIAlgorithm = maxMin(alpha, beta, getNextPlayer(currentPlayer), new GameState(previousGameState));
                if (maximzer) {
                    if (ratingOfKIAlgorithm > v) {
                        v = ratingOfKIAlgorithm;//kein Alpha Beta notwendig weil Possible Moves sowieso =0 und damit kann nichts gepruned werden
                    }
                }
                else{
                    if (ratingOfKIAlgorithm < v) {
                        v = ratingOfKIAlgorithm;//kein Alpha Beta notwendig weil Possible Moves sowieso =0 und damit kann nichts gepruned werden
                    }
                }
            }
        }
        else {


            if(GameInfo.timeLimitNeeded)checkTimelimit(9);

            if (possibleMoves.size() != 0) {
                GameState newGameState;
                long startTimeForBottomRow = System.currentTimeMillis();
                for (Integer[] move : possibleMoves) {
                    if(GameInfo.timeLimitNeeded)checkTimelimit(10);
                    statistics.increaseNodeCounter();
                    statistics.incrementBottomRowCounter();


                    newGameState = new GameState(previousGameState);
                    //if(GameInfo.notTestMode)System.out.println("Spieler "+currentPlayer+" hat "+possibleMoves.size()+" mögliche Züge in der Tiefe "+ this.depth);//TODO entfernen
                    newGameState.makeTurn(currentPlayer, move[0], move[1]);
                    statistics.allPossibleMovesInBottomRow += newGameState.showMoves(getNextPlayer(currentPlayer), false).size();
                    //newGameState.sf.output();//TODO entfernen
                    //result = ev.evaluate(newGameState, newGameState.ownPlayer.playerIdChar);
                    //newGameState.sf.output(ev.fieldValues);
                    ratingOfKIAlgorithm = complexRating.ratingForBottomNodes(newGameState,playerHierarchyAtBeginOfMove);//TODO entfernen
                    if (maximzer) {
                        if (ratingOfKIAlgorithm > v) {
                            v = ratingOfKIAlgorithm;
                            if(alphaBeta) {
                                if (v >= beta) {
                                    break;
                                }
                            }
                        }
                    } else {
                        if (ratingOfKIAlgorithm < v) {
                            v = ratingOfKIAlgorithm;
                            /*AlphaBetaPruning*/
                            if(alphaBeta) {
                                if (v <= alpha) {
                                    break;
                                }
                            }
                            /*AlphaBetaPruningEnde*/
                        }
                    }
                }
                statistics.addToBottomRowTime(System.currentTimeMillis()-startTimeForBottomRow);

            }
            else{//possible moves = 0
                ratingOfKIAlgorithm=complexRating.ratingForBottomNodes(previousGameState,playerHierarchyAtBeginOfMove);
                if (maximzer) {
                    if (ratingOfKIAlgorithm > v) {
                        v = ratingOfKIAlgorithm;
                    }
                }
                else {
                    if (ratingOfKIAlgorithm < v) {
                        v = ratingOfKIAlgorithm;
                    }
                }
            }

        }
        depth--;
        return v;
    }

    private void checkTimelimit(int checkPoint) throws TimeoutException{
        if(GameInfo.timeLimitNeeded) {
            //if(GameInfo.notTestMode)System.out.println("CheckTime");
            if (System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime() > GameInfo.customTimeLimit) {
                if (GameInfo.notTestMode)
                    System.out.println("TimeoutException in Point: " + checkPoint + "    Time over: " + (System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime()) + "  customTimeLimit:" + GameInfo.customTimeLimit);
                statistics.ExceptionThrown = System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime();
                throw new TimeoutException();
            }
        }
    }


    public void testNextPlayer(){
        char currentPlayer='1';
        for (int i =0;i<100;i++){
            currentPlayer=getNextPlayer(currentPlayer);
            if(GameInfo.notTestMode)System.out.println(currentPlayer);
        }
    }
    private char getNextPlayer(char currentPlayerChar){
        Player currentPlayer = null;
        int currentIndex;
        ArrayList<Player>playerList= gameState.getPlayerList();
        for (Player player:playerList){
            if(player.playerIdChar==currentPlayerChar){
                currentPlayer=player;
            }
        }
        if(currentPlayer==null){
            if(GameInfo.notTestMode)System.out.println("Should not happen getNextPlayer");
            exit(-1);
        }
        currentIndex = playerList.indexOf(currentPlayer);//holt den Index des Aktuellen Spielers
        if (currentIndex+1==playerList.size()){
            currentIndex=0;
        }
        else{
            currentIndex++;
        }
        while (playerList.get(currentIndex).diqualified){
            if (currentIndex+1==playerList.size()){
                currentIndex=0;
            }
            else{
                currentIndex++;
            }
        }
        return playerList.get(currentIndex).playerIdChar;
    }



    /*Getter und Setter*/
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public ArrayList<Integer[]> sortOutBadMoves(ArrayList<Integer[]> possibleMoves, List<int[]> badMoves) {
        ArrayList<Integer[]> goodMoves = new ArrayList<>();
        boolean DoNotUseThisMove = false;

        for (Integer[] move : possibleMoves) {
            for (int[] badMove : badMoves) {
                if (move[0] == badMove[0] && move[1] == badMove[1]) {
                    DoNotUseThisMove = true;
                    break;
                }
            }
            if (DoNotUseThisMove) {
                DoNotUseThisMove = false;
            } else {
                goodMoves.add(move);
            }

        }

        if (goodMoves.size() > 0) {
            return goodMoves;
        }

        return possibleMoves;
    }



}


