import java.util.Date;

public class GameInfo {
   static boolean notTestMode =false;
   static boolean sortMoves = true;
   static boolean quietMode = true;
   static boolean timeLimitNeeded = false;
   static boolean iterativeDeepening=false;
   static boolean loggMode =false;
   static boolean aplphaBetaPruning=true;
   static boolean aspirationWindow =false;
   static boolean silentMode=false;
   static boolean jsonOn=false;
   static Date startOfCurrentMove;
   static boolean overrideMode=false;
   static boolean usingOverrideForThisMove=false;

   public static long timeLimit=0; //So viel zeit ist vorgegeben
   public static long customTimeLimit; //So viel Zeit wollen wir brauchen

   /**
    * @param timeLimit
    */
   public static void setTimeLimit(long timeLimit){
      GameInfo.timeLimit=timeLimit;
      GameInfo.customTimeLimit=GameInfo.timeLimit-200;
      if(timeLimit>0){
         timeLimitNeeded=true;
         iterativeDeepening=true;
      }
      if (timeLimit > 1500000) {
         GameInfo.timeLimit = 1500000;
      }

   }

}
