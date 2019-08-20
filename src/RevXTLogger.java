import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
/*Loggt die Anzahl der im Spiel expandierten Zustaende und die aktuelle Tiefe in eine csv-Datei.
 * Die Tiefe befindet sich dabei immer in Spalte A und die Zahl der expandierten Zustaende in
 * Spalte B der csv-Datei, sodass aus den Werten der Zustaende leicht das arithm. Mittel gebildet werden
 * kann. Bei mehrmaligem Aufruf der main wird das Log an die bestehende Datei angehaengt.*/
public class RevXTLogger {
	public Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
    public RevXTLogger(String name){
        Logger root = Logger.getLogger("");
        FileHandler fh = null;
        try{
            fh = new FileHandler(name+".csv", true);
        } catch(SecurityException | IOException e){
            e.printStackTrace();
        }
        fh.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String ret="";
                ret+= this.formatMessage(record);
                return ret;
            }
        });
        root.addHandler(fh);
        root.removeHandler(root.getHandlers()[0]);
    }
    
    public void setHeadlines() {
    	//Ueberschriften
        log.info("depth;"); log.info("expandedNodes;");
        log.info("averageTimePerNode;");
        log.info("totalTimeForMove;");log.info("alphaBeta;");
        log.info("moveSort;");log.info("aspirationWindow;\n");
    }
    
    /*
	 private void log(String depth, String nodes){
		 log.info(depth+";");
	     log.info(nodes+";\n");  //Zeilenumbruch wegen leichterer EXCEL-Zuordnung
	}
	*/
	 public void writeLine(int depth, int expandedNodes, double averageTimePerNode, long totalTimeForMove,boolean alphaBeta,boolean moveSort, boolean aspirationWindow) {
		 log.info(depth+";"); log.info(expandedNodes+";");
		 log.info(averageTimePerNode+";"); log.info(totalTimeForMove+";");
		 log.info(alphaBeta+";"); log.info(moveSort+";");
		 log.info(aspirationWindow+";\n");
		 						//Zeilenumbruch
	 }
	
	 /*
	public static void main(String[] args) {
		int nodes = 42;
        int depth = 4;
        String nodeString = ""+nodes;
        String depthString = ""+depth;
        RevXTLogger revLog = new RevXTLogger();
        revLog.log(depthString, nodeString);
	}
	*/
}
