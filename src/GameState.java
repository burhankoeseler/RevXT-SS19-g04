import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class GameState implements Comparable{


    public int playerID;
    public char playerIDChar;



    public Player ownPlayer;
    public ArrayList<Character> playerListComplete = new ArrayList<Character>() {
        {
            add('1');
            add('2');
            add('3');
            add('4');
            add('5');
            add('6');
            add('7');
            add('8');
        }
    };//includes all possible Players
    public ArrayList<Player> playerList=new ArrayList<Player>();
    public char hostileSpecialField; //TODO Sollte, falls Zeit, noch anders gelöst werden. Wenn ein Gegner auf ein Choice Feld kommt setzt die Client Klasse hier den Spieler mit dem er tauschen will.
    public byte specialField = 0;
    public int move=-1;
    public int depth;
    public int dir = 0;
    public int xDir = 0;
    public int yDir = 0;
    public int possibleMoves = 0;
    public char steine;
    public int ueberschreibsteine;
    public int bomben;
    public int bombenStaerke;
    public Spielfeld sf;
    public ArrayList<Integer[]> reversed = new ArrayList<>();
    public ArrayList<Integer[]> mightReverse = new ArrayList<>();
    public Integer[] firstField = new Integer[2];
    public Integer[] lastMove={0,0};
    public SimpleRating sr=new SimpleRating();

    /*Algorithmen Attribute*/
    /*verknuepft Spielfelder mit Werten zwischen 0 und 100, Wert eines Feldes in int[][] fieldValues an
     * derselben Koordinate wie in char[][] spielfeld.
     */
    protected int[][]fieldValues;
    protected int playerValues[];		// speichert Werte fuer Spieler

    //Konstruktor
    public GameState(Spielfeld sf){
        this.sf = sf;
        this.bomben = sf.bomben;
        this.ueberschreibsteine = sf.ueberschreibsteine;
        this.bombenStaerke = sf.bombenStaerke;
        this.fieldValues = new int[sf.spielfeldbreite][sf.spielfeldhoehe];
        this.playerValues = new int[8];
        fillFieldValues();
        fillPlayerList();
    }

    public GameState(GameState other) {

        //this.playerList = other.playerList;
        this.playerID = other.playerID;
        this.playerIDChar = other.playerIDChar;
        this.hostileSpecialField = other.hostileSpecialField;
        this.specialField = other.specialField;
        this.move = other.move;
        this.depth = other.depth;
        this.dir = other.dir;
        this.xDir = other.xDir;
        this.yDir = other.yDir;
        this.possibleMoves = other.possibleMoves;
        this.steine = other.steine;
        this.ueberschreibsteine = other.ueberschreibsteine;
        this.bomben = other.bomben;
        this.bombenStaerke = other.bombenStaerke;
        this.fieldValues = other.fieldValues;
        this.playerValues = other.playerValues;
        this.lastMove=other.lastMove.clone();

        this.ownPlayer = new Player(other.ownPlayer);
        this.sf = new Spielfeld(other.sf);
        this.firstField[0]=other.firstField[0];
        this.firstField[1]=other.firstField[1];
        for(Player player:other.playerList){
            this.playerList.add(new Player(player));
        }
        this.playerValues = other.playerValues.clone();
        this.fieldValues = new int[other.fieldValues.length][];
        for (int i=0; i<other.fieldValues.length; i++){
            this.fieldValues[i] = other.fieldValues[i].clone();
        }
    }

    //Funktion das Spielfeld nach Bombenwurf zu verändern, wenn es unser Zug ist, verringert sich unsere Bombenzahl um 1
    public void throwBomb(int x, int y, int bombenStaerke){
        ArrayList<Integer[]> destroyed = new ArrayList<>();
        getDestroyedFields(x, y, bombenStaerke, destroyed);
        destroyFields(destroyed);
    }

    public void destroyFields(ArrayList<Integer[]> destroyed){
        for (Integer[] feld: destroyed) {
            sf.spielfeld[feld[0]][feld[1]] = '-';
        }
    }

    public void getDestroyedFields(int x, int y, int bombenStaerke, ArrayList<Integer[]> destroyed){
        Integer[] field = {x, y};
        Integer[] transition = new Integer[6];
        destroyed.add(field);
        if(bombenStaerke == 0){
            return;
        }else {
            for (int i = 0; i < 8; i++) {
                setDirection(i);
                if ((x + this.xDir >= sf.spielfeldbreite || x + this.xDir < 0 || y + this.yDir >= sf.spielfeldhoehe || y + this.yDir < 0) || sf.spielfeld[x + this.xDir][y + this.yDir] == '-') {
                    transition = getTransition(x, y, i);
                    if (transition != null) {
                        getDestroyedFields(transition[3], transition[4], bombenStaerke-1, destroyed);
                    }
                }else{
                    getDestroyedFields(x+this.xDir, y+this.yDir, bombenStaerke-1, destroyed);
                }
            }
        }
    }

    //Funktion, die einen Zug animmt und das Spielfeld nach Regeln färbt TODO für gegner steine overrides um 1 reduzieren falls genutzt
    public void makeTurn(char player,int x, int y){
        this.move++;
        this.lastMove[0]=x;//Wichtig für KI Algorithmus
        this.lastMove[1]=y;
        char feld;
        //if(canPlay(player,x, y)){//TODO Prüfen ob immer richtig -> Züge werden entweder vom Server validiert oder von GameState.showMoves
        //                                                                  TODO Also gibt es hier eigentlich keine ungeprüften Züge mehr
        if(true){
            feld = sf.spielfeld[x][y];
            //if(GameInfo.notTestMode)System.out.println("Gueltiger Zug");
            sf.spielfeld[x][y] = player;
            flip(player,x, y);
            switch (feld){
                case 'c': choiceField(player); break;
                case 'i': inversionField(); break;
                case 'b': bonusField(player,1); break;
                case 'x':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                default: break;
            }
        }else{
            if(GameInfo.notTestMode)System.out.println("Ungueltiger Zug.");
            //exit(-1);
        }

    }

    //Funktion die auswählt mit welchem Spieler man wechselt auf Choice Feldern
    private char choiceFieldDeciderProvisionally(char player){ //TODO keine Logik!! es wird immer der Spieler mit den meisten Steinen gewählt
        ArrayList<Player> rankedList = getCurrentRanking();
        return rankedList.get(0).playerIdChar;

    }

    //Funktion die mit ausgesuchtem Spieler alle Steine umfärbt
    public void choiceField(char player){
        char otherPlayer;
        if (player==this.ownPlayer.playerIdChar) {//Falls der eigene Spieler übergeben wird
            otherPlayer=choiceFieldDeciderProvisionally(player);
            this.specialField= (byte)Character.getNumericValue(otherPlayer);//Setzt den spieler mit dem getauscht werden soll in die specialField Variable damit der Client den Wert an den Server leiten kann.
        }
        else{

            otherPlayer=this.hostileSpecialField; //Falls ein Gegner eine "Choice" trifft wird diese hier in den otherPlayer (Spieler mit dem getauscht wird) geladen

        }

        for (int y = 0; y < sf.spielfeldhoehe; y++) {
            for (int x = 0; x < sf.spielfeldbreite; x++) {
                if (sf.spielfeld[x][y] == otherPlayer) {
                    sf.spielfeld[x][y] = player;
                } else if (sf.spielfeld[x][y] == player) {
                    sf.spielfeld[x][y] = otherPlayer;
                }
            }
        }

    }

    //Funtion die entweder Bombenanzahl oder Ueberschreibsteine um 1 erhöht//TODO für die gegner Spieler bomben oder ueberschreibsteine addieren
    public void bonusField(char player, int choice){//Bombe(1) oder Ueberschreibstein(2)
        if (player==this.ownPlayer.playerIdChar) {
            if (choice == 0){
                this.specialField = 20;//setzt den Wert 20(Code für Bombe) in die specialField Variable damit der Client den Wert an den Server leiten kann.
            }

            else if (choice == 1) {
                this.specialField = 21;//Setzt den Wert 21(Code für Überschreibstein) in die specialField Variable damit der Client den Wert an den Server leiten kann.
            }
        }
    }

    //Funktion die Spielfeld nach regeln des Inversions Feldes umfärbt
    public void inversionField(){
        for(int y = 0; y < sf.spielfeldhoehe; y++){
            for(int x = 0; x < sf.spielfeldbreite; x++){
                if(playerListComplete.contains(sf.spielfeld[x][y])){
                    int index = playerListComplete.indexOf(sf.spielfeld[x][y]);
                    if(index==sf.spieler-1) {
                        sf.spielfeld[x][y] = playerListComplete.get(0);
                    }
                    else{
                        sf.spielfeld[x][y] = playerListComplete.get(index+1);
                    }
                }
            }
        }
    }

    //Rekursive Funktion, welche Steine die beim Zug umgefärbt werden in Liste speichert, Rückgabe wert ist ob am Ende ein eigener Stein gefunden wird oder nicht
    public boolean flipFurther(char player,int x, int y, int i){
        Integer[] reverse = new Integer[2];
        Integer[] transition = new Integer[6];
        if((x+this.xDir >= sf.spielfeldbreite || x+this.xDir < 0 || y+this.yDir >= sf.spielfeldhoehe || y+this.yDir < 0) || sf.spielfeld[x + this.xDir][y + this.yDir] == '-') {
            transition = getTransition(x, y, i);
            if (transition != null) {
                setNewDirection(transition[5]);
                //if (sf.spielfeld[transition[3]][transition[4]] >= '2' && sf.spielfeld[transition[3]][transition[4]] <= '8') {
                if((this.playerListComplete.contains(sf.spielfeld[transition[3]][transition[4]])) || sf.spielfeld[transition[3]][transition[4]] == 'x') {
                    if(this.firstField[0] == transition[3] && this.firstField[1] == transition[4]){
                        return false;
                    }else if(sf.spielfeld[transition[3]][transition[4]] == player){
                        return true;
                    }else {
                        reverse[0] = transition[3];
                        reverse[1] = transition[4];
                        this.mightReverse.add(reverse);
                        if (flipFurther(player, transition[3], transition[4], this.dir)) {
                            return true;
                        }else{
                            return false;
                        }
                    }
                }else{
                    return false;
                }
            }else return false;
        }//else if(sf.spielfeld[x + xDir][y + yDir] >= '2' && sf.spielfeld[x + xDir][y + yDir] <= '8'){
        else if((this.playerListComplete.contains(sf.spielfeld[x + this.xDir][y + this.yDir])&&sf.spielfeld[x + this.xDir][y + this.yDir] != player) || sf.spielfeld[x + this.xDir][y + this.yDir] == 'x'){  //True wenn in der Richtung noch ein gegnerischer Stein liegt
            reverse[0] = x + this.xDir;
            reverse[1] = y + this.yDir;
            this.mightReverse.add(reverse);
            if(flipFurther(player,x+this.xDir, y+this.yDir, this.dir))
                return true;
            else return false;
        }else if(this.firstField[0] == x+xDir && this.firstField[1] == y+yDir) {    //Überprüft ob man aufgrund von Transitionen das eigene Feld wieder erreicht hat
            return false;
        }else if(sf.spielfeld[x + this.xDir][y + this.yDir] == player){
            return true;
        }//TODO abfrage ob '0'
        return false;
    }

    //Funktion die schaut ob benachbarter Stein bei Zugauswahl gültig ist für das Backtracken, wenn ja ruft rekursive Funktion flipFurther auf
    public void flip(char player,int x, int y){
        this.firstField[0] = x;
        this.firstField[1] = y;
        this.reversed.clear();                          //löscht Werte vom vorherigen zug
        //this.mightReverse.clear();
        for(int i = 0; i < 8; i++) {                    //geht jede mögliche Richtung durch
            Integer[] transition = new Integer[6];
            this.mightReverse.clear();
            Integer[] reverse = new Integer[2];
            setDirection(i);
            if((x+this.xDir >= sf.spielfeldbreite || x+this.xDir < 0 || y+this.yDir >= sf.spielfeldhoehe || y+this.yDir < 0) || sf.spielfeld[x + this.xDir][y + this.yDir] == '-'){
                transition = getTransition(x, y, this.dir);
                if (transition != null) {
                    setNewDirection(transition[5]);
                    //if (sf.spielfeld[transition[3]][transition[4]] >= '2' && sf.spielfeld[transition[3]][transition[4]] <= '8') {
                    if ((this.playerListComplete.contains(sf.spielfeld[transition[3]][transition[4]])&& sf.spielfeld[transition[3]][transition[4]] != player) || sf.spielfeld[transition[3]][transition[4]] == 'x'){
                        reverse[0] = transition[3];
                        reverse[1] = transition[4];
                        this.mightReverse.add(reverse);
                        if(!(flipFurther(player,transition[3], transition[4], this.dir))){
                            this.mightReverse.clear();
                        }else if(this.mightReverse.size() != 0) {
                            this.reversed.addAll(this.mightReverse);
                        }
                    } else {
                        continue;
                    }
                }
            }//else if(sf.spielfeld[x + xDir][y + yDir] >= '2' && sf.spielfeld[x + xDir][y + yDir] <= '8'){
            else if((this.playerListComplete.contains(sf.spielfeld[x + this.xDir][y + this.yDir])&& sf.spielfeld[x + this.xDir][y + this.yDir] != player) || sf.spielfeld[x + this.xDir][y + this.yDir] == 'x'){  //true wenn ein Umliegendes Feld ein gegnerischer Stein ist
                reverse[0] = x+this.xDir;
                reverse[1] = y+this.yDir;
                this.mightReverse.add(reverse);//fügt das Feld mightReverse hinzu
                if(!(flipFurther(player,x+this.xDir, y+this.yDir, i))){  //true wenn die folgenden Steine auch noch gegnersteine sind und irgendwann ein leeres Feld folgt
                    this.mightReverse.clear();
                }else if(this.mightReverse.size() != 0){
                    this.reversed.addAll(this.mightReverse);
                }
            }else if(sf.spielfeld[x + this.xDir][y + this.yDir] == '0'){//TODO =='0' statt !=player
                this.mightReverse.clear();
                continue;
            }
        }
        for (Integer[] reversePLS : reversed){
            //if(GameInfo.notTestMode)System.out.println("Steine die umgedreht werden: x="+reversePLS[0]+"y=" + reversePLS[1]);
            sf.spielfeld[reversePLS[0]][reversePLS[1]] = player;
        }
    }

    //Gibt alle möglichen Funktionen in einer Liste zurück
    public ArrayList<Integer[]> showMoves(char player, boolean firstDepth) {
        ArrayList<Integer[]> moves = new ArrayList<>();
        for(int y = 0; y < sf.spielfeldhoehe; y++){
            for(int x = 0; x < sf.spielfeldbreite; x++){
                if(canPlay(player,x, y)){
                    Integer[] move = {x,y};
                    moves.add(move);
                }
            }
        }
        possibleMoves = moves.size();

        if (moves.size() == 0 && !GameInfo.overrideMode && firstDepth) {
            GameInfo.overrideMode=true;
            moves = showMoves(player, true);
            GameInfo.overrideMode=false;
            if(moves.size()>0&&player==this.ownPlayer.playerIdChar){//Heuristik für Zug mit override soll nur angewendet werden wenn eigener SPieler ein Override benutzt
                GameInfo.usingOverrideForThisMove=true;
            }
            if(GameInfo.notTestMode&&false){
                System.out.println("found following moves with overrides");
                for (Integer[] move:moves){
                    System.out.println(move[0]+","+move[1]);
                }
            }

        }
        return moves;
    }

    //Gibt zurück ob Feld als Zugmöglichkeit gültig wäre, vom Inhalt des Feldes
    public boolean checkField(char player, int x, int y){
        //Für JSON formatter
        Player currentPlayer = null;
        for (Player playerObj : this.playerList) {
            if(playerObj.playerIdChar == player){
                currentPlayer = playerObj;
                break;
            }
        }
        return 0 <= x && x < sf.spielfeldbreite
                && 0 <= y && y < sf.spielfeldhoehe
                && (sf.spielfeld[x][y] == '0' || (((sf.spielfeld[x][y] >= '1' && sf.spielfeld[x][y] <= '8') || sf.spielfeld[x][y] == 'x') && (currentPlayer.overrides > 0)&&(GameInfo.overrideMode))
                || sf.spielfeld[x][y] == 'c' || sf.spielfeld[x][y] == 'i' || sf.spielfeld[x][y] == 'b');
        /**return 0 <= x && x < sf.spielfeldbreite
                && 0 <= y && y < sf.spielfeldhoehe
                && (sf.spielfeld[x][y] == '0' || (((sf.spielfeld[x][y] >= '1' && sf.spielfeld[x][y] <= '8') || sf.spielfeld[x][y] == 'x') && (this.ueberschreibsteine > 0 || player!=this.ownPlayer.playerIdChar))
                || sf.spielfeld[x][y] == 'c' || sf.spielfeld[x][y] == 'i' || sf.spielfeld[x][y] == 'b');**/
    }

    //Settet die Variabeln xDir, yDir und dir mit jeweiligen Werten in jeweiligen Richtungen
    public void setDirection(int iDir){
        switch(iDir){
            case 0: this.xDir = 0; this.yDir = -1; this.dir = 0; break;
            case 1: this.xDir = 1; this.yDir = -1; this.dir = 1; break;
            case 2: this.xDir = 1; this.yDir = 0; this.dir = 2; break;
            case 3: this.xDir = 1; this.yDir = 1; this.dir = 3; break;
            case 4: this.xDir = 0; this.yDir = 1; this.dir = 4; break;
            case 5: this.xDir = -1; this.yDir = 1; this.dir = 5; break;
            case 6: this.xDir = -1; this.yDir = 0; this.dir = 6; break;
            case 7: this.xDir = -1; this.yDir = -1; this.dir = 7; break;
        }
    }

    //Setter die Variabeln xDir, yDir, und dir mit jewiligen Werten wenn man aus einer Transition raus kommt
    public void setNewDirection(int iDir){
        switch(iDir){
            case 4: this.xDir = 0; this.yDir = -1; this.dir = 0; break;
            case 5: this.xDir = 1; this.yDir = -1; this.dir = 1; break;
            case 6: this.xDir = 1; this.yDir = 0; this.dir = 2; break;
            case 7: this.xDir = 1; this.yDir = 1; this.dir = 3; break;
            case 0: this.xDir = 0; this.yDir = 1; this.dir = 4; break;
            case 1: this.xDir = -1; this.yDir = 1; this.dir = 5; break;
            case 2: this.xDir = -1; this.yDir = 0; this.dir = 6; break;
            case 3: this.xDir = -1; this.yDir = -1; this.dir = 7; break;
        }
    }

    //Gibt zurück ob Feld x,y als gültiger Zug zählt und geht in rekursive Funktion checkFurther, außer wenn direkt neben dem feld ein eigener Stein oder eine wand ist
    public boolean canPlay(char player, int x, int y){
        if(!(checkField(player,x, y))) return false;
        this.firstField[0] = x;
        this.firstField[1] = y;
        if(sf.spielfeld[x][y] == 'x'){
            for(Player playerObj: this.playerList){
                if(playerObj.playerIdChar == player && playerObj.overrides > 0) return true;
            }
        }
        for(int i = 0; i < 8; i++){
            Integer[] transition = new Integer[6];
            setDirection(i);
            if((x+this.xDir >= sf.spielfeldbreite || x+this.xDir < 0 || y+this.yDir >= sf.spielfeldhoehe || y+this.yDir < 0) || sf.spielfeld[x + this.xDir][y + this.yDir] == '-'){
                transition = getTransition(x, y, this.dir);
                if (transition != null){
                    setNewDirection(transition[5]);
                    //if(sf.spielfeld[transition[3]][transition[4]] >= '2' && sf.spielfeld[transition[3]][transition[4]] <= '8'){
                    if((this.playerListComplete.contains(sf.spielfeld[transition[3]][transition[4]])&&sf.spielfeld[transition[3]][transition[4]] != player) || sf.spielfeld[transition[3]][transition[4]] == 'x'){
                        if(checkFurther(player,transition[3], transition[4])) return true;
                        //if(checkFurther(gameState,transition[3] + xDir, transition[4] + yDir, i)) return true;
                    }
                }
            }//else if (sf.spielfeld[x + xDir][y + yDir] >= '2' && sf.spielfeld[x + xDir][y + yDir] <= '8') {
            else if ((this.playerListComplete.contains(sf.spielfeld[x + this.xDir][y + this.yDir])&&sf.spielfeld[x + this.xDir][y + this.yDir] != player) || sf.spielfeld[x + this.xDir][y + this.yDir] == 'x'){
                if(checkFurther(player,x + this.xDir, y + this.yDir)) return true;
            }
        }
        return false;
    }

    //Rekursive Funktion die in eine Richtung backtrackt und schaut ob am ende der richtung ein eigener Stein steht
    public boolean checkFurther(char player, int x,int y){
        Integer[] transition;
        if((x+this.xDir >= sf.spielfeldbreite || x+this.xDir < 0 || y+this.yDir >= sf.spielfeldhoehe || y+this.yDir < 0) || sf.spielfeld[x + this.xDir][y + this.yDir] == '-') {
            if ((transition = getTransition(x, y, this.dir)) != null) {
                setNewDirection(transition[5]);
                //if(sf.spielfeld[transition[3]][transition[4]] >= '2' && sf.spielfeld[transition[3]][transition[4]] <= '8')
                if(this.playerListComplete.contains(sf.spielfeld[transition[3]][transition[4]]) || sf.spielfeld[transition[3]][transition[4]] == 'x') {
                    if(this.firstField[0] == transition[3] && this.firstField[1] == transition[4]) {
                        return false;
                    }else if (sf.spielfeld[transition[3]][transition[4]] == player) {
                        return true;
                    } else {
                        return checkFurther(player, transition[3], transition[4]);
                        //return checkFurther(gameState,transition[3] + xDir, transition[4] + yDir, i);
                    }
                }
            }
        }//else if (sf.spielfeld[x + xDir][y + yDir] >= '2' && sf.spielfeld[x + xDir][y + yDir] <= '8') {
        else if(this.firstField[0] == x && this.firstField[1] == y) {
            return false;
        }else if(this.firstField[0] == x+this.xDir && this.firstField[1] == y+this.yDir){
            return false;
        }else if(sf.spielfeld[x + this.xDir][y + this.yDir] == player){
            return true;
        }else if((this.playerListComplete.contains(sf.spielfeld[x + this.xDir][y + this.yDir])&&sf.spielfeld[x + this.xDir][y + this.yDir] != player) || sf.spielfeld[x + this.xDir][y + this.yDir] == 'x'){
            return checkFurther(player,x + this.xDir, y + this.yDir);
        }
        return false;
    }

    //Gibt transition an feld x, y in richtung dir zurück als Integer[] mit 6 werten x1,y1,dir1 <-> x2,y2,dir2. Wenn ziel feld am anfang des arrays steht, wird umgedreht
    public Integer[] getTransition(int x, int y, int dir){
        Integer[] newTransition = new Integer[6];
        //int dirInversed=(dir+4)%8;
        for(Integer[] transition : sf.transitionen){
            if(transition[0] == x && transition[1] == y && transition[2] == dir){
                return transition;
            }else if(transition[3] == x && transition[4] == y && transition[5] == dir){
                newTransition[0] = transition[3];
                newTransition[1] = transition[4];
                newTransition[2] = transition[5];
                newTransition[3] = transition[0];
                newTransition[4] = transition[1];
                newTransition[5] = transition[2];
                return newTransition;
            }
        }
        return null;
    }

    private void fillPlayerList(){
        for (int i=0;i< this.sf.spieler; i++){
            this.playerList.add(new Player(i+1, this.bomben, this.ueberschreibsteine));

        }
    }

    /*Algorithmen Methoden*/
    public void fillFieldValues(){
        for(int y = 0; y < sf.spielfeldhoehe; y++){
            for(int x = 0; x < sf.spielfeldbreite; x++){
                if(sf.spielfeld[x][y] != '-'){
                    if(checkForCorner(x,y) == true){
                        this.fieldValues[x][y] += 100;		// Ecke ist das stabilste und wertvollste Feld -> value =100
                    }
                    /*
                    if(checkForWedge(x,y)== true){
                        this.fieldValues[x][y] += 25;		//Keile kï¿½nnen sehr nï¿½tzlich sein -> value = 75
                    }
                    */
                    /*
                    if(checkForXOrC(x,y) == true){			// das Besetzen von X- oder C- Feldern ist oft fatal -> value = 0 oder 15(s.u.)
                        this.fieldValues[x][y] -= 100;
                    }
                    */
                    if(checkForBorder(x,y)== true){			// das besetzen von Kanten ist oft nï¿½tzlich -> value = 65
                        this.fieldValues[x][y] += 25;
                    }

                }
            }
        }
    }

    /*Ecken*/
    public boolean checkForCorner(int xPos, int yPos){
        // Fall Ecke oben links
        if(xPos == 0 && yPos == 0){	// 3 Fallunterscheidungen um array out of bound zu vermeiden
            return true;			// sollte immer eine Ecke sein
        } else if(xPos == 0 && yPos > 0){
            if(sf.spielfeld[xPos][yPos-1] == '-') {	//li kann nichts sein, oben muss Wall sein
                return true;
            }
        } else if(xPos > 0 && yPos == 0){
            if(sf.spielfeld[xPos-1][yPos] == '-') {	//oben kann nichts sein, li muss Wall sein
                return true;
            }
        } else{
            if(sf.spielfeld[xPos-1][yPos] == '-' && sf.spielfeld[xPos-1][yPos-1] == '-'
                    && sf.spielfeld[xPos][yPos-1] == '-') {
                return true;
            }
        }

        // Fall Ecke oben rechts
        if(xPos == sf.spielfeldbreite-1 && yPos == 0){
            return true;											//sollte immer eine Ecke sein
        } else if(xPos == sf.spielfeldbreite-1 && yPos > 0) {
            if(sf.spielfeld[sf.spielfeldbreite-1][yPos-1] == '-') {	// re kann nichts sein, oben muss Wall sein
                return true;
            }
        } else if(xPos < sf.spielfeldbreite-1 && yPos == 0) {
            if(sf.spielfeld[xPos+1][yPos] == '-') {					//oben kann nichts sein, re muss Wall sein
                return true;
            }
        } else {
            if(sf.spielfeld[xPos+1][yPos] == '-' && sf.spielfeld[xPos+1][yPos-1] == '-'
                    && sf.spielfeld[xPos][yPos-1] == '-') {
                return true;
            }
        }

        // Fall Ecke unten links
        if(xPos == 0 && yPos == sf.spielfeldhoehe-1){
            return true;
        } else if(xPos > 0 && yPos == sf.spielfeldhoehe-1) {
            if(sf.spielfeld[xPos-1][yPos] == '-') {
                return true;
            }
        } else if(xPos == 0 && yPos < sf.spielfeldhoehe-1) {
            if(sf.spielfeld[xPos][yPos+1] == '-') {
                return true;
            }
        } else {
            if(sf.spielfeld[xPos-1][yPos] == '-' && sf.spielfeld[xPos-1][yPos+1] == '-'
                    && sf.spielfeld[xPos][yPos+1] == '-') {
                return true;
            }
        }

        // Fall Ecke unten rechts
        if(xPos == sf.spielfeldbreite-1 && yPos == sf.spielfeldhoehe-1) {
            return true;
        } else if(xPos < sf.spielfeldbreite-1 && yPos == sf.spielfeldhoehe-1){
            if(sf.spielfeld[xPos+1][yPos] == '-') {
                return true;
            }
        } else if(xPos == sf.spielfeldbreite-1 && yPos < sf.spielfeldhoehe-1) {
            if(sf.spielfeld[xPos][yPos+1] == '-') {
                return true;
            }
        } else {
            if(sf.spielfeld[xPos+1][yPos] == '-' && sf.spielfeld[xPos+1][yPos+1] == '-'
                    && sf.spielfeld[xPos][yPos+1] == '-') {
                return true;
            }
        }
        return false;
    }

    /*Kanten*/
    public boolean checkForBorder(int xPos, int yPos){
        if(checkForCorner(xPos, yPos) == false) {
            if((xPos == 0 || xPos == sf.spielfeldbreite-1) || (yPos == 0 || yPos == sf.spielfeldhoehe-1)){ 		// array out of range
                return true;
            } else if((sf.spielfeld[xPos-1][yPos] == '-') || (sf.spielfeld[xPos][yPos-1] == '-') || (sf.spielfeld[xPos+1][yPos] == '-')
                    || (sf.spielfeld[xPos][yPos+1] == '-')) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


    /*Keil moeglich?*/
    /*
    public boolean checkForWedge(int xPos, int yPos){
    	if(checkForBorder(xPos, yPos) == true) {
    		if(xPos == 0 || xPos == sf.spielfeldbreite-1) {
    			if((sf.spielfeld[xPos][yPos-1] != '1' && sf.spielfeld[xPos][yPos+1] != '1') 				// 2 Sonderfaelle am Rand des Spielfelds
    					&& (sf.spielfeld[xPos][yPos-1] != '0' && sf.spielfeld[xPos][yPos+1] != '0')) {
    						return true;
    			}
    		} else if(yPos == 0 || yPos == sf.spielfeldhoehe-1){
    			if((sf.spielfeld[xPos-1][yPos] != '1' && sf.spielfeld[xPos+1][yPos] != '1')
    					&& (sf.spielfeld[xPos-1][yPos] != '0' && sf.spielfeld[xPos+1][yPos] != '0' )){
    						return true;
    			}
    		} else{																							// nicht am Rand des Spielfelds aber Kante (4 Faelle)
    			if(((sf.spielfeld[xPos][yPos-1] != '1' && sf.spielfeld[xPos][yPos+1] != '1') && (sf.spielfeld[xPos][yPos-1] != '0' && sf.spielfeld[xPos][yPos+1] != '0'))
    					|| ((sf.spielfeld[xPos+1][yPos-1] != '1' && sf.spielfeld[xPos-1][yPos+1] != '1') && (sf.spielfeld[xPos+1][yPos-1] != '0' && sf.spielfeld[xPos-1][yPos+1] != '0'))
    						|| ((sf.spielfeld[xPos+1][yPos] != '1' && sf.spielfeld[xPos-1][yPos] != '1') && (sf.spielfeld[xPos+1][yPos] != '0' && sf.spielfeld[xPos-1][yPos] != '0'))
    							|| ((sf.spielfeld[xPos-1][yPos-1] != '1' && sf.spielfeld[xPos+1][yPos+1] != '1') && (sf.spielfeld[xPos-1][yPos-1] != '0' && sf.spielfeld[xPos+1][yPos+1] != '0'))) {
    								return true;
    			}
    		}
    	}
    	return false;
    }
    */


    /*handelt es sich bei dem eingegebenen Feld um ein X- oder C-Feld (benachbart zu einer Ecke liegend)?*/
    /*
    public boolean checkForXOrC(int xPos, int yPos){
    	if(checkForCorner(xPos, yPos) == false) {
    		if(xPos == 0 || xPos == sf.spielfeldbreite-1) {													// Sonderfaelle
    			if(checkForCorner(xPos, yPos-1) == true || checkForCorner(xPos, yPos+1) == true) {
    				return true;
    			}
    		} else if(yPos == 0 || yPos == sf.spielfeldhoehe-1) {
    			if(checkForCorner(xPos-1, yPos) == true || checkForCorner(xPos+1, yPos) == true) {
    				return true;
    			}
    		} else {
    			if(checkForCorner(xPos, yPos-1) == true || checkForCorner(xPos+1, yPos-1) == true || checkForCorner(xPos+1, yPos) == true 			// alle umliegenden 8 Felder abfragen
    					|| checkForCorner(xPos+1, yPos+1) == true || checkForCorner(xPos, yPos+1) == true || checkForCorner(xPos-1, yPos+1) == true
    						|| checkForCorner(xPos-1, yPos) == true || checkForCorner(xPos-1, yPos-1) == true){
    							return true;
    			}
    			return false;
    		}
    	}
    	return false;
    }
    */
/*<<<<<<< HEAD
    errechnet den aktuellen Gesamtwert der Felder des uebergebenen Spielers aus
=======
    errechnet den aktuellen Gesamtwert der Felder des ubergebenen Spielers aus
>>>>>>> a1e08ec9458ef1c5a6de4078c8f8459d43510b7b*/
    public void checkValue(char player, int playerNum){
        for(int y = 0; y < sf.spielfeldhoehe; y++) {
            for( int x = 0; x < sf.spielfeldbreite; x++) {
                if(sf.spielfeld[x][y] == player) {
                    this.playerValues[playerNum] += fieldValues[x][y];

                }
            }
        }
    }

    //Getter und Setter


    public void setPlayerID(int playerID) {
        this.playerID = playerID;
        this.playerIDChar = Integer.toString(this.playerID).charAt(0);
        //if(GameInfo.notTestMode)System.out.println("DAS IST DIE SPIELERNUMMER: "+this.playerIDChar);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
    public char getPlayerIDChar(){
        return this.playerIDChar;
    }
    public int getPlayerID() {
        return playerID;
    }
    public void setOwnPlayer(Player ownPlayer) {
        this.ownPlayer = ownPlayer;
    }
    public byte getSpecialField(){//Gibt den aktuellen wert des Spezialfelds(0=normal,20=Bombe,21=Überschreibstein,Spielernummer für choice) aus und setzt ihn zurück auf 0(für normal)
        byte value = this.specialField;
        this.specialField=0;
        return  value;
    }
    public void setHostileSpecialField(char hostileSpecialField){
        this.hostileSpecialField = hostileSpecialField;
    }

    public ArrayList<Integer[]> showPossibleBombMoves(){
        ArrayList<Integer[]> possibleMoves = new ArrayList<>();
        Integer[] target=new Integer[2];
        for(int y = 0; y < sf.spielfeldhoehe; y++) {
            for( int x = 0; x < sf.spielfeldbreite; x++) {
                if(sf.spielfeld[x][y] != '-' && sf.spielfeld[x][y] != this.getPlayerIDChar()){
                    target[0] = x;
                    target[1] = y;
                    possibleMoves.add(target);
                }
            }
        }
        return possibleMoves;
    }

    public byte[] getBombTarget(){
        ArrayList<byte[]> bombMoves = getPossibleBombMoves();
        ArrayList<Player> rankedPlayerList = getCurrentRanking();
        char ownChar = ownPlayer.playerIdChar;
        char enemyChar;
        int ownRanking = ownPlayer.ranking;
        if(ownRanking != 0){
            enemyChar = rankedPlayerList.get(ownRanking-1).playerIdChar;
        }else{
            enemyChar = rankedPlayerList.get(ownRanking+1).playerIdChar;
        }
        byte[] target = new byte[2];
        target[0] = bombMoves.get(0)[0];
        target[1] = bombMoves.get(0)[1];
        int collateralDamage = Integer.MAX_VALUE;
        int highestDamage = 0;
        for(byte[] move : bombMoves){
            ArrayList<Integer[]> destroyedFields = new ArrayList<>();
            getDestroyedFields((int) move[0], (int) move[1], this.bombenStaerke, destroyedFields);
            int damageEnemy = 0;
            int damageSelf = 0;
            for(Integer[] field: destroyedFields){
                char fieldChar = sf.spielfeld[field[0]][field[1]];
                if(fieldChar == enemyChar){
                    damageEnemy++;
                }else if (fieldChar == ownChar){
                    damageSelf++;
                }
            }
            if(highestDamage < damageEnemy && collateralDamage >= damageSelf) {
                highestDamage = damageEnemy;
                collateralDamage = damageSelf;
                target[0] = move[0];
                target[1] = move[1];
            }
        }
        return target;
    }

    public ArrayList<byte[]> getPossibleBombMoves() {
        ArrayList<byte[]> possibleBombMoves = new ArrayList<>();
        for(int y = 0; y < sf.spielfeldhoehe; y++) {
            for( int x = 0; x < sf.spielfeldbreite; x++) {
                if(sf.spielfeld[x][y] != '-'){
                    byte[] target =new byte[2];
                    target[0] = (byte) x;
                    target[1] = (byte) y;
                    possibleBombMoves.add(target);
                }
            }
        }
        return possibleBombMoves;
    }

    public ArrayList<Player> getCurrentRanking(){
        ArrayList<Player> playerList=new ArrayList<>(this.playerList);
        for (Player player: playerList){
            player.ranking = 0;
            player.points = 0;
        }
        ArrayList<Player> playerListRanked = playerList;
        for(int y = 0; y < this.sf.spielfeldhoehe; y++){
            for(int x = 0; x < this.sf.spielfeldbreite; x++){
                char field = this.sf.spielfeld[x][y];
                if(field == '1'){
                    playerList.get(0).points++;
                }else if(field == '2'){
                    playerList.get(1).points++;
                }else if(field == '3'){
                    playerList.get(2).points++;
                }else if(field == '4'){
                    playerList.get(3).points++;
                }else if(field == '5'){
                    playerList.get(4).points++;
                }else if(field == '6'){
                    playerList.get(5).points++;
                }else if(field == '7'){
                    playerList.get(6).points++;
                }else if(field == '8'){
                    playerList.get(7).points++;
                }
            }
        }
        Collections.sort(playerListRanked);
        int i = 0;
        for(Player player : playerListRanked){
            player.ranking = i;
            i++;
        }
        return playerListRanked;
    }

    public ArrayList<Player> getPlayerList() {
        return playerList;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof GameState){
            GameState other =(GameState) o;

            if (this.sr.getSimpleRatingMoveSort(this, this.ownPlayer.playerIdChar) < this.sr.getSimpleRatingMoveSort(other, this.ownPlayer.playerIdChar)) {
                return 1;
            }
            if (this.sr.getSimpleRatingMoveSort(this, this.ownPlayer.playerIdChar) > this.sr.getSimpleRatingMoveSort(other, this.ownPlayer.playerIdChar)) {
                return -1;
            }
            return 0;
        }
        return 0;
    }

    public Integer[] checkForPossibleBonusFields(List<Integer[]> possibleMoves){
        Integer[] miss= new Integer[]{-1, -1};
        for (Integer[] move: possibleMoves){
            if(this.sf.spielfeld[move[0]][move[1]]=='b'){
                return move;
            }
        }
        return miss;
    }

    public ArrayList<Integer[]> checkForPossibleCorners(List<Integer[]> possibleMoves, List<int[]> corners) {
        ArrayList<Integer[]> possibleMovesWithCorners = new ArrayList<>();
        Integer[] miss= new Integer[]{-1, -1};
        for (Integer[] move: possibleMoves){
            for (int[]corner: corners){
                if(move[0]==corner[0]&&move[1]==corner[1]){
                    possibleMovesWithCorners.add(move);
                }
            }

        }
        return possibleMovesWithCorners;
    }

    public Integer[] checkForFieldsNearBonus(List<Integer[]> possibleMoves, List<int[]> fieldsNearBonus){
        Integer[] miss= new Integer[]{-1, -1};
        for (Integer[] move: possibleMoves){
            for (int[]fieldNearBonus: fieldsNearBonus){
                if(move[0]==fieldNearBonus[0]&&move[1]==fieldNearBonus[1]){
                    //System.out.println("Field Near Bonus!");
                    return move;
                }
            }

        }
        return miss;
    }

    public int calculateDistancetoNearestBonus(List<int[]> bonusFields){
        int min=Integer.MAX_VALUE;

        if(bonusFields.size()>0) {

            for (int y = 0; y < sf.spielfeldhoehe; y++) {
                for (int x = 0; x < sf.spielfeldbreite; x++) {
                    if (sf.spielfeld[x][y] == ownPlayer.playerIdChar) {
                        for (int[] bonusField : bonusFields) {
                            int cacheDistance = Math.abs((x - bonusField[0])) + Math.abs((y - bonusField[1]));
                            if (cacheDistance < min && cacheDistance > 2) {
                                System.out.println("x: " + x);
                                System.out.println("bonusField[0]: "+bonusField[0]);
                                System.out.println("y: " + y);
                                System.out.println("bonusField[1]: "+bonusField[1]);
                                min = cacheDistance;
                            }
                        }

                    }
                }
            }
        }

    return min;
    }

    public int possibleMovesWithoutOverrides(char player) {
        ArrayList<Integer[]> moves = new ArrayList<>();
        for (int y = 0; y < sf.spielfeldhoehe; y++) {
            for (int x = 0; x < sf.spielfeldbreite; x++) {
                if (canPlay(player, x, y)) {
                    Integer[] move = {x, y};
                    moves.add(move);
                }
            }
        }
        int possibleMovesWithoutOverridesNumber = moves.size();
        return possibleMovesWithoutOverridesNumber;
    }

    public int checkHowManyOfThisFieldsAreInPossession(List<int[]> fields) {
        int counter = 0;
        for (int[] field : fields) {
            if (sf.spielfeld[field[0]][field[1]] == ownPlayer.playerIdChar) {
                counter++;
            }
        }
        return counter;
    }

    public List<int[]> reachableBonusFields(List<int[]> bonusFields) {

        List<int[]> reachableBonusFields = new ArrayList<>();

        for (int[] bonusField : bonusFields) {
            int x = bonusField[0];
            int y = bonusField[1];
            for (int i = 0; i < 8; i++) {
                setDirection(i);
                int distance = 1;
                boolean found = false;
                while (!(x + distance * this.xDir >= sf.spielfeldbreite || x + distance * this.xDir < 0 || y + distance * this.yDir >= sf.spielfeldhoehe || y + distance * this.yDir < 0)) {


                    if (sf.spielfeld[x + distance * this.xDir][y + distance * this.yDir] == '-') {
                        distance++;
                        continue;
                    }
                    if (sf.spielfeld[x + distance * this.xDir][y + distance * this.yDir] == this.ownPlayer.playerIdChar) {
                        int[] reachableField = {x, y};
                        reachableBonusFields.add(reachableField);
                        distance++;
                        found = true;
                        break;
                    }
                    distance++;

                }
                if (found) {
                    found = false;
                    break;
                }

            }
        }


        return reachableBonusFields;
    }
}
