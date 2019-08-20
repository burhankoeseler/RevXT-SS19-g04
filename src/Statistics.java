import java.util.ArrayList;

public class Statistics {

    RevXTLogger logger ;


    long startOfGame=0;
    long endOfGame=0;
    long currentGameTime=0;
    long allCountedNodesInGame=0;
    long totalTimeForMove;
    long totalTimeForLastDepth;
    long timePerState=0;
    long averageTimerPerMove=0;
    long guessedTimeForNextDepth=0;
    int lastDepth=0;
    int highestReachedDepth=0;
    int StateCounter=0;
    private ArrayList <Long> allTimes=new ArrayList<>();
    public int move=0;
    public long timeToUpdateStaticValues;

    public int nodeCounter=0;
    public double averageTimePerNode=0;
    public int bottomRowCounter=0;
    public long timeNeededForBottomRow=0;
    int allPossibleMovesInBottomRow=0;
    public int aspirationWindowFails =0;
    public int aspirationWindowSucesses=0;
    public long totalTime=0;
    public int possibleMovesNumber=0;
    public long ExceptionThrown=0;
    public long guessedTimeoutAt=0;
    public int playerId;

    public long leftFunctionWithException=0;
    public long timeWhenleavingIterativeDeppening=0;
    public long timeForFinishingMoveAfterException=0;

    Statistics(){
        if(GameInfo.loggMode&&false){
            logger=new RevXTLogger("statistics"+System.currentTimeMillis());
            logger.log.info("Move;"+"ExceptionThrownat(ms);"+"LeftFunctionWithException;"+"TimeforFinishingAfterException;"+"guessedTimoutAt(ms);"+"Timelimit(ms)"+"highestDepth"+"Total Time for Move"+System.lineSeparator());
            //logger.log.info("Move;"+"LastDepth;"+"NodeCounter;"+"BottomRowNodeCounter;"+"TimeNeededforBottomRow;"+"Total Time for Move;"+"PossibleMoves;"+"TotalTimeForLastDepth;"+"guessedTime;"+System.lineSeparator());
        }
    }

   public void output(){
       if(GameInfo.notTestMode&&false) {
           System.out.println("------------------------------------Statistics Zug "+move+"--------------------------------------------");

           //System.out.println("currentGameTime: "+currentGameTime);
           //System.out.println("totalTimerFor Move: " + totalTimeForMove);
           //System.out.println("allCountedNodesInGame: "+allCountedNodesInGame);
           //System.out.println("averageTimerFor Move: " + averageTimerPerMove);
           //System.out.println("timeToUpdateStaticValues: " + timeToUpdateStaticValues);
           System.out.println("totalsNodes: " + nodeCounter);
           System.out.println("averageTimePerNode:"+ averageTimePerNode);
           //System.out.println("BottomRowNodes: "+ bottomRowCounter);
           //System.out.println("TimeNeeded for Bottom Row: "+timeNeededForBottomRow);
           //System.out.println("Time for last depth: "+ totalTimeForLastDepth);
           System.out.println("ExceptionThrown: "+ ExceptionThrown);
           //System.out.println("leftFunctionWithException: " + leftFunctionWithException);
           //System.out.println("timeForFinishingMoveAfterException: " + timeForFinishingMoveAfterException);
           System.out.println("guessedTimeoutAt: " + guessedTimeoutAt);
           //System.out.println("aspirationWindowFails: "+aspirationWindowFails);
           //System.out.println("aspirationWindowSucesses: "+aspirationWindowSucesses);
           //System.out.println("lastDepth: "+lastDepth);
           System.out.println("highestReachedDepth: "+highestReachedDepth);
           //System.out.println("possibleMovesNumber: "+possibleMovesNumber);
           //System.out.println("PlayerID: "+playerId);
           //System.out.println("------------------------------------Statistics End----------------------------------------");
       }
   }

   public void addToAllTimes(long timeForMove){
       allTimes.add(timeForMove);
       long sum=0;

       for (int i=0;i<allTimes.size();i++){
           sum+=allTimes.get(i);
       }
       this.averageTimerPerMove=sum/allTimes.size();
       this.averageTimePerNode=(double)totalTimeForMove/(double)nodeCounter;
   }

   public void updateTotalTimeForLastDepth(long time){
       this.totalTimeForLastDepth =time;
   }

   public void increaseNodeCounter(){
       this.nodeCounter++;
       this.allCountedNodesInGame++;
   }
   public void resetNodeCounter(){
       this.nodeCounter=0;
   }
   public void incrementBottomRowCounter(){
       this.bottomRowCounter++;
   }
   public void resetBottomRowCounter(){
        this.bottomRowCounter=0;
   }
   public void addToBottomRowTime(long time){
       timeNeededForBottomRow+=time;
   }
   public void resetBottomRowTime(){
       this.timeNeededForBottomRow=0;
   }
   public void updateCurrentGameTime(){
       this.currentGameTime=System.currentTimeMillis()-this.startOfGame;
   }
   public void setLastDepth(int depth){
       this.lastDepth= depth;
       if(depth>highestReachedDepth){
           highestReachedDepth=lastDepth;
       }
   }

   public void reset(){
       resetNodeCounter();
       resetBottomRowCounter();
       resetBottomRowTime();
       this.averageTimePerNode=0;
       this.aspirationWindowFails =0;
       this.timePerState=0;
       this.aspirationWindowSucesses=0;

   }

   public void timeLog(){
       if(GameInfo.loggMode&&false){
           logger.log.info(move+";");
           logger.log.info(ExceptionThrown+";");
           logger.log.info(leftFunctionWithException+";");
           logger.log.info(timeForFinishingMoveAfterException+";");
           logger.log.info(guessedTimeoutAt+";");
           logger.log.info(GameInfo.timeLimit+";");
           logger.log.info(highestReachedDepth+";");
           logger.log.info(totalTimeForMove+";"+System.lineSeparator());
       }
   }

   public void depthLog(){
       if(GameInfo.loggMode&&false){
           logger.log.info(move+";");
           logger.log.info(lastDepth+";");
           logger.log.info(nodeCounter+";");
           logger.log.info(bottomRowCounter+";");
           logger.log.info(timeNeededForBottomRow+";");
           logger.log.info(totalTimeForMove+";");
           logger.log.info(possibleMovesNumber+";");
           logger.log.info(totalTimeForLastDepth+";");
           logger.log.info(guessedTimeForNextDepth+System.lineSeparator());
       }
   }
}
