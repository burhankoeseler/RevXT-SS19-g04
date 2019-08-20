import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class Client {

    private GameState gameState;
    private Spielfeld sf;
    private KIAlgorithmus ki;
    private Statistics statistics;
    private DataOutputStream output;
    private DataInputStream input;
    private RevXTLogger logger;
    private String ip;
    private int port;
    private boolean gameIsOn=false;
    private boolean phase1=false;
    private boolean phase2=false;

    public Client(String ip, int port){
        this.ip=ip;
        this.port=port;
        if(GameInfo.loggMode)this.logger=new RevXTLogger(Long.toString(new Date().getTime()));
        if(GameInfo.loggMode)logger.setHeadlines();
    }

    public void initConnection(){
        try {
            Socket client = new Socket(this.ip, this.port);
            this.output = new DataOutputStream(client.getOutputStream());
            this.input = new DataInputStream(client.getInputStream());
            byte[] login={1,0,0,0,1,4};
            this.output.write(login);

            int type;
            byte[] message;
            this.gameIsOn=true;
            this.phase1=true;

            while (this.gameIsOn){
                type=this.input.read();
                //if(GameInfo.notTestMode)System.out.println("Type:"+type);
                message=getMessage(getMessagelength());
                handleMessages(type,message);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        if(GameInfo.notTestMode)System.out.println("I have Successfully finished the Game");

    }

    private byte[] getMessage(int length){
        //if(GameInfo.notTestMode)System.out.println("length:"+ length);
        byte[] message=new byte[length];
        try {
            this.input.read(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }
    private int getMessagelength(){
        byte[] buffer = new byte[4];
        try {
            this.input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //if(GameInfo.notTestMode)System.out.println(buffer[0]+" "+buffer[1]+" "+buffer[2]+" "+buffer[3]+" ");
        return convertToInt(buffer);
    }


    private void handleMessages(int type,byte[] message){
        switch (type) {
            case 1:
                if (GameInfo.notTestMode)
                    System.out.println("Typ1: Sollte eigentlich nicht passieren. Eigentlich nur für login in den Server gedacht");
                break;
            case 2://Enthält Spielfeld gemäß Spezifikation als String
                String map = new String(message);
                //if(GameInfo.notTestMode)System.out.println(map);
                sf = new Spielfeld(map);
                initiateGame();
                sf.outputReversedSfArray(sf.reversedSfArray());
                break;
            case 3://Weist einem Spieler ein vorzeichenloses 8-Bit-Integer als Spielernummer zu
                if (GameInfo.notTestMode||true) System.out.println("Du bist Spieler: " + message[0]);
                int playerId = (int) message[0];
                gameState.setPlayerID(playerId);
                for (Player player : gameState.getPlayerList()) {
                    if (playerId == player.playerId) {
                        gameState.setOwnPlayer(player);
                        break;
                    }
                }
                statistics.playerId = playerId;
                break;
            case 4: //Die Zugaufforderung gibt einen vorzeichenlosen 32-Bit-Integer als Zeitlimit(in 10^(-3) Sek
                // und einen vorzeichenlosen 8-Bit-Integer als maximale Suchtiefe vor.
                Date startTimeMove = new Date();//Zeit zu der der Zug begonnen wurde
                GameInfo.startOfCurrentMove = startTimeMove;
                statistics.totalTimeForMove = 0;
                int timelimit = convertToInt(splitMessage(message, 0, 3));
                GameInfo.setTimeLimit(timelimit);
                int depth = message[4];
                ki.setMaxDepth(depth);

            case 5: //Die Zugantwort enth¨alt die x- und y-Koordinate als vorzeichenlosen 16-Bit-Integer,
                // sowie einen vorzeichenlosen 8-Bit-Integer f¨ur eventuelle Sonderfelder
                // (0 bei normalem Feld; beim Choice-Feld die Spielernummer mit der getauscht wird;
                // beim Bonus-Felder eine 20 f¨ur extra Bombe oder 21 f¨ur extra ¨Uberschreibstein).
                if (this.phase1) {
                    Integer[] selectedMove = ki.getBestMoveIterativeDeepening();

                    boolean overrideUsed = false;
                    boolean bonusField = false;
                    //Funktion für JSON abfrage
                    if (!GameInfo.silentMode&&GameInfo.jsonOn) {
                        String possibleMovesString[] = new String[gameState.showMoves(gameState.getPlayerIDChar(), false).size()];
                        int iString = 0;
                        for (Integer[] move : gameState.showMoves(gameState.ownPlayer.playerIdChar, false)) {
                            String moveString = move[0] + " " + move[1];
                            possibleMovesString[iString] = moveString;
                            iString++;
                        }
                        JSONFormatter.addMoveRequest(gameState.sf.reversedSfArray(), gameState.ownPlayer.overrides, gameState.ownPlayer.playerIdChar, possibleMovesString);
                    }
                    //Funktion für JSON abfrage ende
                    if (gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] == 'b') {
                        bonusField = true;

                    }
                    if (gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != '0'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'c'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'i'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'b') {
                        overrideUsed = true;
                    }

                    gameState.makeTurn(gameState.getPlayerIDChar(), selectedMove[0], selectedMove[1]);//Gibt den Zug an die GameState(GameState) Klasse weiter

                    if (bonusField) {
                        if (gameState.specialField == 20) {
                            for (Player player : gameState.playerList) {
                                if (player.playerIdChar == gameState.ownPlayer.playerIdChar) player.bombs++;
                            }
                        } else if (gameState.specialField == 21) {
                                for (Player player : gameState.playerList) {
                                    if (player.playerIdChar == gameState.ownPlayer.playerIdChar) player.overrides++;
                                }
                        }
                    }
                    if (overrideUsed) {
                        for (Player player : gameState.playerList) {
                            if (player.playerIdChar == gameState.ownPlayer.playerIdChar) player.overrides--;
                        }
                    }
                    //if(GameInfo.notTestMode)System.out.println("ueberschreibsteine:" + gameState.ueberschreibsteine);
                    byte[] x = intToByteArray(selectedMove[0]);
                    byte[] y = intToByteArray(selectedMove[1]);

                    byte[] move = {5, 0, 0, 0, 5, x[2], x[3], y[2], y[3], gameState.getSpecialField()};
                    try {

                        output.write(move);
                        //if(GameInfo.notTestMode)System.out.println("Feld nach unserem Zug:");
                        //sf.output();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(this.phase2){
                    byte[] target= gameState.getBombTarget();
                    byte[] move = {5, 0, 0, 0, 5, 0, target[0], 0, target[1], 0};//TODO nur zu vorführungszwecken nochmal komplett
                    //Funktion für JSON abfrage
                    if(!GameInfo.silentMode&&GameInfo.jsonOn) {
                        ArrayList<Integer[]> bombMovesList = gameState.showPossibleBombMoves();
                        String possibleMovesString[] = new String[bombMovesList.size()];
                        int iString = 0;
                        for (Integer[] bombMove : bombMovesList) {
                            String moveString = bombMove[0] + " " + bombMove[1];
                            possibleMovesString[iString] = moveString;
                            iString++;
                        }
                        JSONFormatter.addBombMoveRequest(gameState.sf.reversedSfArray(), gameState.ownPlayer.playerIdChar, possibleMovesString);
                    }
                    //Funktion für JSON abfrage ende
                    gameState.throwBomb(target[0],target[1], gameState.bombenStaerke);
                    for (Player player : gameState.playerList) {
                        if (player.playerIdChar == gameState.ownPlayer.playerIdChar) player.bombs--;
                    }
                    try {
                        //statistics.timeForFinishingMoveAfterException=System.currentTimeMillis()-statistics.timeWhenleavingIterativeDeppening;
                        output.write(move);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //if(GameInfo.notTestMode)System.out.println("Feld nach unserem Zug:");
                    //sf.output();
                }

                //Zeit zu der ein kompletter Zug abgeschlossen ist;
                statistics.timeForFinishingMoveAfterException = System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime();
                long timerForMove = System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime();
                statistics.totalTimeForMove = System.currentTimeMillis() - GameInfo.startOfCurrentMove.getTime();
                statistics.addToAllTimes(timerForMove);
                statistics.updateCurrentGameTime();
                statistics.output();
                sf.outputReversedSfArray(sf.reversedSfArray());
                GameInfo.usingOverrideForThisMove=false;
                if (GameInfo.loggMode) {
                    logger.writeLine(ki.maxDepth, statistics.nodeCounter, statistics.averageTimePerNode, statistics.totalTimeForMove, GameInfo.aplphaBetaPruning, GameInfo.sortMoves, GameInfo.aspirationWindow);
                }
                break;
            case 6://Ein Spielzug enth¨alt die gleichen Angaben, wie eine Zugantwort,
                // und zus¨atzlich noch die Nummer des ziehenden Spielers als
                // vorzeichenlosen 8-Bit-Integer.
                statistics.move++;//Zähler für den Zug in dem wir uns gerade befinden
                if (this.phase1) {
                    if ((int) message[5] != gameState.getPlayerID()) {
                        Integer[] selectedMove = {convertToInt(splitMessage(message, 0, 1)), convertToInt(splitMessage(message, 2, 3))};
                        char playerChar = Byte.toString(message[5]).charAt(0);
                        boolean overrideUsed = false;
                        boolean bonusField = false;
                        if (gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] == 'b') {
                            bonusField = true;
                        }
                        if (gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != '0'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'c'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'i'
                            && gameState.sf.spielfeld[selectedMove[0]][selectedMove[1]] != 'b') {
                            overrideUsed = true;
                        }
                        gameState.setHostileSpecialField(Byte.toString(message[4]).charAt(0)); //liefert den Wert 0 für normales feld oder die entsprechenden Codes für Bonusfelder
                        if (bonusField) {
                            for (Player player : gameState.playerList) {
                                if(player.playerIdChar == playerChar){
                                    if ((int) message[4] == 20) {
                                        player.bombs++;
                                    } else if ((int) message[4] == 21) {
                                        player.overrides++;
                                    }
                                }
                            }
                        }
                        if (overrideUsed) {
                            for (Player player : gameState.playerList) {
                                if (player.playerIdChar == playerChar) player.overrides--;
                            }
                        }
                        gameState.makeTurn(Byte.toString(message[5]).charAt(0), convertToInt(splitMessage(message, 0, 1)), convertToInt(splitMessage(message, 2, 3)));//TODO prüfen ob die meke Turn Logik wirklich notwendig ist(Zug wurd vorher schon validiert)


                        //if(GameInfo.notTestMode)System.out.println("Hostile Move: GameState=" + message[5] + " x=" + convertToInt(splitMessage(message, 0, 1)) + "  y=" + convertToInt(splitMessage(message, 2, 3)) + "  Bonus: " + message[4]);
                        //if(GameInfo.notTestMode)System.out.println("Feld nach Gegner Zug:");
                        //sf.output();
                    }
                    //JSONFORMATTER CODE
                    if (!GameInfo.silentMode&&GameInfo.jsonOn) {
                        String receivedMove = convertToInt(splitMessage(message, 0, 1)) + " " + convertToInt(splitMessage(message, 2, 3)) + " " + message[4];
                        char hostilePlayer = Byte.toString(message[5]).charAt(0);
                        int[] overrides = new int[sf.spieler];
                        int iPlayer = 0;
                        for (Player player : gameState.getPlayerList()) {
                            overrides[iPlayer] = player.overrides;
                            iPlayer++;
                            if (iPlayer == sf.spieler) break;
                        }
                        int[] bombs = new int[sf.spieler];
                        iPlayer = 0;
                        for (Player player : gameState.getPlayerList()) {
                            bombs[iPlayer] = player.bombs;
                            iPlayer++;
                            if (iPlayer == sf.spieler) break;
                        }
                        JSONFormatter.addMove(receivedMove, hostilePlayer, gameState.sf.reversedSfArray(), overrides, bombs);
                    }
                    //JSONFORMATTER CODE ENDE
                }
                if (this.phase2) {
                    Integer[] target = {convertToInt(splitMessage(message, 0, 1)), convertToInt(splitMessage(message, 2, 3))};
                    char playerChar = Byte.toString(message[5]).charAt(0);

                    if((int) message[5] != gameState.getPlayerID()) {
                        gameState.throwBomb(convertToInt(splitMessage(message, 0, 1)), convertToInt(splitMessage(message, 2, 3)), gameState.bombenStaerke);
                        for (Player player : gameState.playerList) {
                            if (playerChar == player.playerIdChar) player.bombs--;
                        }
                    }
                    //JSONFORMATTER CODE
                    if (!GameInfo.silentMode&&GameInfo.jsonOn) {
                        String receivedMove = convertToInt(splitMessage(message, 0, 1)) + " " + convertToInt(splitMessage(message, 2, 3)) + " " + message[4];
                        char hostilePlayer = Byte.toString(message[5]).charAt(0);
                        int iPlayer = 0;
                        int[] bombs = new int[sf.spieler];
                        for (Player player : gameState.getPlayerList()) {
                            bombs[iPlayer] = player.bombs;
                            iPlayer++;
                            if (iPlayer == sf.spieler) break;
                        }
                        JSONFormatter.addBombMove(receivedMove, hostilePlayer, gameState.sf.reversedSfArray(), bombs);
                    }
                    //JSONFORMATTER CODE ENDE
                    // if(GameInfo.notTestMode)System.out.println("Feld nach Gegner Zug:");
                    //sf.output();
                }

                break;
            case 7://Disqualifikation des im Nachrichtenfeld als vorzeichenlosen 8-Bit-Integers angegebenen Spielers.
                // Das Spiel ist fuer den betreffenden Spieler beendet.
                int disqualifiedPlayer = (int) message[0];
                ArrayList<Player> PlayerList = gameState.getPlayerList();
                for (Player player : PlayerList) {
                    if (player.playerId == disqualifiedPlayer) {
                        player.diqualified = true;
                    }
                }
                for (Player player :
                        PlayerList) {
                    if (GameInfo.notTestMode)
                        System.out.println("Spieler " + player.playerId + " disqualifiziert: " + player.diqualified);
                }
                break;
            case 8://Es wurde ein Endzustand in der ersten Phase erreicht (kein Inhalt).
                this.phase1 = false;
                this.phase2 = true;
                break;
            case 9://Signalisiert Endzustand der zweiten Phase und somit das Ende des Spiels (kein Inhalt).
                //JSONFORMATTER CODE
                if (!GameInfo.silentMode&&GameInfo.jsonOn){
                    if (!GameInfo.notTestMode) System.out.println(JSONFormatter.buildFinalString());
                    statistics.endOfGame = System.currentTimeMillis();
                }
                //JSONFORMATTER CODE ENDE
                this.phase2=false;
                this.gameIsOn=false;
                if(GameInfo.notTestMode)this.gameState.sf.output();

                break;
            default:
                if(GameInfo.notTestMode)System.out.println("Something went wrong this is not a valid Type");
                exit(-1);
                break;

        }
    }

    private byte[] splitMessage(byte[] message, int start, int end) {
        int size=end-start+1;
        byte[] splitted=new byte[size];
        int counter=0;
        for(int i=start;i<=end;i++){
            splitted [counter] = message[i];
            counter++;

        }
        return splitted;
    }
    private byte[] intToByteArray(int value){
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(value);
        buf.flip();
        byte[] arr = buf.array();
        return arr;
    }

    private void initiateGame(){
        this.gameState =new GameState(this.sf);
        this.statistics=new Statistics();
        this.ki=new KIAlgorithmus(this.gameState, this.statistics);
        statistics.startOfGame=currentTimeMillis();
    }

    private int convertToInt(byte[] buffer){
        int result=0;
        if(buffer.length==4) {
            result = buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF);
        }
        else if (buffer.length==2){
            result=(buffer[0] & 0xFF) << 8 | (buffer[1] & 0xFF);
        }
        else{
            if(GameInfo.notTestMode)System.out.println("Can't convert this length of Array. Exit Game...");
            exit(-1);
        }
        //if(GameInfo.notTestMode)System.out.println("Inhalt Byte[]:" + buffer[0]+" "+buffer[1]+" "+buffer[2]+" "+buffer[3]+" ");
        //if(GameInfo.notTestMode)System.out.println("Result: "+result);
        return result;
    }

    public Spielfeld getSf() {
        return sf;
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
