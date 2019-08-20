import java.util.ArrayList;
import java.util.List;

public class Evaluator {
	
    /*Algorithmen Attribute*/
    /*verknuepft Spielfelder mit Werten zwischen 0 und 100, Wert eines Feldes in int[][] fieldValues an
     * derselben Koordinate wie in char[][] spielfeld.
     */
    protected int[][]fieldValues;
    protected double playerValues[];		// speichert Werte fuer Spieler
    private Spielfeld sf;
    private GameState gameState;
    private double singleMoveValue=5;			//Multiplikator fuer den Wert jeder Zugmoeglichkeit
    private int xDir;
    private int yDir;
    private int dir;
    public int fieldsWichAreUsable =0;
    public int fieldsOnedByAnyPlayer =0;
    public int bonusFieldsLeft =0;
    public int choiceFieldsLeft =0;
    public int inversionFieldsLeft=0;

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

    private List<int[]> bonusFields=new ArrayList<>();
    private List<int[]> goodDistanceForBonusFields=new ArrayList<>();
    private List<int[]> badDistanceForBonusFields=new ArrayList<>();
    private List<int[]> corners=new ArrayList<>();
    private List<int[]> boarders = new ArrayList<>();
    private List<int[]> boardersAndCorners = new ArrayList<>();



    /*public Evaluator(Spielfeld sf) {
    	this.sf = sf;
    	this.gameState = new GameState(sf);
    	this.fieldValues = new int[sf.spielfeldbreite][sf.spielfeldhoehe];
    	this.playerValues = new double[8];
    	fillFieldValues();
    	checkPlayerValues();
    }*/
    public Evaluator(GameState gs) {
        this.gameState=gs;
        this.sf = gs.sf;
        this.fieldValues = new int[sf.spielfeldbreite][sf.spielfeldhoehe];
    }


    public Evaluator(){//Konstruktor für KI Algorithmus
        this.playerValues = new double[8];
    }

    public int evaluate(GameState gameState,char currentPlayer){
        this.gameState=gameState;
        this.sf=gameState.sf;
        this.fieldValues = new int[sf.spielfeldbreite][sf.spielfeldhoehe];
        fillFieldValues();
        return checkValue(currentPlayer,Character.getNumericValue(currentPlayer));
    }

    //Static Rating: Die Statische Bewertung muss nur einmal gemacht werden und speichert alle Bewertungsanteile die sich nie verändern z.B Eckfelder, Felder an Kanten o.Ä
    //in einem Array mit den selben ausmaßen wie der Spielfeldarray. Im Vergleich zu diesem kann dann eine Bewertung der Spielsituation geschehen;
    public int[][] getStaticFieldValues(){
        fillFieldValues();
        return this.fieldValues;
    }
    
	/*Algorithmen Methoden*/
    public void fillFieldValues(){
        this.fieldValues=new int[sf.spielfeldbreite][sf.spielfeldhoehe];
        for(int y = 0; y < sf.spielfeldhoehe; y++){
            for(int x = 0; x < sf.spielfeldbreite; x++){
                if(sf.spielfeld[x][y] != '-'){
                    if (sf.spielfeld[x][y]=='b'){
                        this.fieldValues[x][y]+=RatingValues.valueBonus;
                        int[] bonusField={x,y};
                        this.bonusFields.add(bonusField);
                    }
                    if (perfectDistanceToBonus(x,y)){
                        this.fieldValues[x][y]+=RatingValues.valueNearToBonus;
                    }
                    if (badDistanceToBonus(x,y)){
                        this.fieldValues[x][y]+=RatingValues.valueTooNearToBonus;
                    }
                    if(checkForCorner(x,y) == true){
                        this.fieldValues[x][y] += RatingValues.valueCorner;// Ecke ist das stabilste und wertvollste Feld -> value =100
                        int[] corner={x,y};
                        this.corners.add(corner);
                        this.boardersAndCorners.add(corner);
                    }
                    
                    if(checkForXOrC(x,y) == true){			// das Besetzen von X- oder C- Feldern ist oft fatal -> value = 0 oder 15(s.u.)
                        this.fieldValues[x][y] += RatingValues.valuexorC;
                    }
                    
                    if(checkForBorder(x,y)== true){			// das besetzen von Kanten ist oft nï¿½tzlich -> value = 65
                        this.fieldValues[x][y] += RatingValues.valueBoarder;
                        int[] boarderField = {x, y};
                        this.boarders.add(boarderField);
                        this.boardersAndCorners.add(boarderField);
                    }

                }
                else{
                    this.fieldValues[x][y] = Integer.MIN_VALUE;
                }
            }
        }
    }

    /*Ecken*/
    public boolean checkForCorner(int xPos, int yPos){
    	
    	for(int i = 0; i < 8; i++) {						//bei Transition kann es keine Ecke sein
    		if(gameState.getTransition(xPos, yPos, i) != null) {
    			return false;
    		}
    	}
    	
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
            		&& sf.spielfeld[xPos][yPos-1] == '-'){
            			/*Diagonale*/
            			if(xPos-1 >= 0 && xPos+1 <= sf.spielfeldbreite-1 && yPos-1 >= 0 && yPos+1 <= sf.spielfeldhoehe-1) {
            				if((sf.spielfeld[xPos-1][yPos+1] == '-' || sf.spielfeld[xPos+1][yPos-1] == '-') 
                                	&& (sf.spielfeld[xPos-1][yPos-1] == '-' || sf.spielfeld[xPos+1][yPos+1] == '-')) {
                						return true;
                			}
            			} else {
            				return true;
            			}		
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
            			if(xPos-1 >= 0 && xPos+1 <= sf.spielfeldbreite-1 && yPos-1 >= 0 && yPos+1 <= sf.spielfeldhoehe-1) {
            					if((sf.spielfeld[xPos-1][yPos+1] == '-' || sf.spielfeld[xPos+1][yPos-1] == '-') 
            							&& (sf.spielfeld[xPos-1][yPos-1] == '-' || sf.spielfeld[xPos+1][yPos+1] == '-')) {
            								return true;
            					}
            			} else {
            				return true;
            			}
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
            			if(xPos-1 >= 0 && xPos+1 <= sf.spielfeldbreite-1 && yPos-1 >= 0 && yPos+1 <= sf.spielfeldhoehe-1) {
            				if((sf.spielfeld[xPos-1][yPos+1] == '-' || sf.spielfeld[xPos+1][yPos-1] == '-') 
            						&& (sf.spielfeld[xPos-1][yPos-1] == '-' || sf.spielfeld[xPos+1][yPos+1] == '-')) {
        								return true;
            				}
            			} else {
            				return true;
            			}
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
            			if(xPos-1 >= 0 && xPos+1 <= sf.spielfeldbreite-1 && yPos-1 >= 0 && yPos+1 <= sf.spielfeldhoehe-1) {
            				if((sf.spielfeld[xPos-1][yPos+1] == '-' || sf.spielfeld[xPos+1][yPos-1] == '-') 
            						&& (sf.spielfeld[xPos-1][yPos-1] == '-' || sf.spielfeld[xPos+1][yPos+1] == '-')) {
        								return true;
            				}
            			} else {
            				return true;
            			}
            }
        }
        return false;
    }

    /*Kanten*/
    public boolean checkForBorder(int xPos, int yPos){
    	
    	for(int i = 0; i < 8; i++) {
    		if(gameState.getTransition(xPos, yPos, i) != null) {
    			return false;
    		}
    	}
        
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

    /*handelt es sich bei dem eingegebenen Feld um ein X- oder C-Feld (benachbart zu einer Ecke liegend)?*/
    public boolean checkForXOrC(int xPos, int yPos){
    	/*da checkForCorner() Transitionen beruecksichtigt, muessen Spielfeldecken nochmal abgefragt werden*/
		if((xPos == 0 && yPos == 0) || (xPos == sf.spielfeldbreite-1 && yPos == 0) || (xPos == 0 && yPos == sf.spielfeldhoehe-1) 
				|| (xPos == sf.spielfeldbreite-1 && yPos == sf.spielfeldhoehe-1)){
					return false;
		}
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
    			if((checkForCorner(xPos, yPos-1) == true && sf.spielfeld[xPos][yPos-1] != '-') 
    					|| (checkForCorner(xPos+1, yPos-1) == true && sf.spielfeld[xPos+1][yPos-1] != '-')
    						|| (checkForCorner(xPos+1, yPos) == true && sf.spielfeld[xPos+1][yPos] != '-')			// alle umliegenden 8 Felder abfragen
    							|| (checkForCorner(xPos+1, yPos+1) == true && sf.spielfeld[xPos+1][yPos+1] != '-') 
    								|| (checkForCorner(xPos, yPos+1) == true && sf.spielfeld[xPos][yPos+1] != '-') 
    									|| (checkForCorner(xPos-1, yPos+1) == true && sf.spielfeld[xPos-1][yPos+1] != '-')
    										|| (checkForCorner(xPos-1, yPos) == true && sf.spielfeld[xPos-1][yPos] != '-') 
    											|| (checkForCorner(xPos-1, yPos-1) == true && sf.spielfeld[xPos-1][yPos-1] != '-')){
    												return true;
    			} else {
    				return false;
    			}
    		}
    	}
    	return false;
    }
    
    /*ruft für jeden Spieler die Funktion checkValue auf (s.u.)*/
    public void checkPlayerValues() {
    	/**checkValue('1', 1);
    	checkValue('2', 2);
    	checkValue('3', 3);
    	checkValue('4', 4);
    	checkValue('5', 5);
    	checkValue('6', 6);
    	checkValue('7', 7);
    	checkValue('8', 8); **/
    	char player = '1';
    	int playerNum = 1;
    	for(int i = 0; i < sf.spieler; i++){
    	    player += i;
    	    playerNum += i;
            checkValue(player, playerNum);
        }
    }
    
    /*errechnet den aktuellen Gesamtwert der Felder des ubergebenen Spielers aus*/
    public int checkValue(char player, int playerNum){
        this.playerValues[playerNum-1]=0;
        for(int y = 0; y < sf.spielfeldhoehe; y++) {
            for( int x = 0; x < sf.spielfeldbreite; x++) {
                if(sf.spielfeld[x][y] == player) {
                    this.playerValues[playerNum-1] += fieldValues[x][y];

                }
            }
        }
        /*Addiert den Wert fuer die Spielerzuege auf den Gesamtwert der Spieler*/
        gameState.showMoves(player, false);
    	this.playerValues[playerNum-1] += gameState.possibleMoves * singleMoveValue;	// *5 als Multiplikator f�r den Wert jeder Zugm�glichkeit

        return (int)this.playerValues[playerNum-1];
    }

    
    /*Zaehlt wie viele Spielsteine ein bestimmter Spieler besitzt*/
    public int checkPlayerTokens(char player) {
    	int playerTokens = 0;
    	for(int y = 0; y < sf.spielfeldhoehe; y++) {
    		for(int x = 0; x < sf.spielfeldbreite; x++) {
    			if(sf.spielfeld[x][y] == player) {
    				playerTokens++;
    			}
    		}
    	}
    	return playerTokens;
    }

    public boolean perfectDistanceToBonus(int x, int y){

        for (int i=0;i<8;i++){
            setDirection(i);
            if((x+xDir*2)>0&&(x+xDir*2)<sf.spielfeldbreite&&(y+yDir*2)>0&&(y+yDir*2)<sf.spielfeldhoehe) {
                if (sf.spielfeld[x + xDir][y + yDir] == '-') {
                    continue;
                }
                if (sf.spielfeld[x + xDir * 2][y + yDir * 2] == 'b' && sf.spielfeld[x + xDir][y + yDir] != 'b') {
                    int[] fieldWithGoodDistance = {x, y};
                    goodDistanceForBonusFields.add(fieldWithGoodDistance);
                    return true;
                }
            }
        }
        return false;
    }
    public boolean badDistanceToBonus(int x, int y){
        if(sf.spielfeld[x][y]=='b'){
            return false;
        }
        for (int i=0;i<8;i++){
            setDirection(i);

            if ((x + xDir) >= 0 && (x + xDir) < (sf.spielfeldbreite) && (y + yDir) >= 0 && (y + yDir) < (sf.spielfeldhoehe)) {
                if (sf.spielfeld[x+xDir][y+yDir] == 'b') {
                    int[] fieldWithBadDistance={x,y};
                    badDistanceForBonusFields.add(fieldWithBadDistance);
                    return true;
                }
            }
        }
        return false;
    }

    public List<int[]>getBonusFields(){
        List<int[]>copy=new ArrayList<>(this.bonusFields);
        return copy;
    }
    public List<int[]>getGoodDistanceForBonusFields(){
        List<int[]>copy=new ArrayList<>(this.goodDistanceForBonusFields);
        return copy;
    }
    public List<int[]>getBadDistanceForBonusFields(){
        List<int[]>copy=new ArrayList<>(this.badDistanceForBonusFields);
        return copy;
    }
    public List<int[]>getCorners(){
        List<int[]>copy=new ArrayList<>(this.corners);
        return copy;
    }

    public List<int[]> getBoarders() {
        List<int[]> copy = new ArrayList<>(this.boarders);
        return copy;
    }

    public List<int[]> getBoardersAndCorners() {
        List<int[]> copy = new ArrayList<>(this.boardersAndCorners);
        return copy;
    }

    public void clearLists(){
        this.bonusFields.clear();
        this.badDistanceForBonusFields.clear();
        this.goodDistanceForBonusFields.clear();
        this.corners.clear();
        this.boarders.clear();
    }
    public double getCurrentGameProgressAndUpdateDynamicFieldValues(){
        int counterGeneralUseableFields=0;
        int counterFieldsOnedByAnyPlayer=0;
        int counterBonusFieldsLeft=0;
        int counterChoiceFields=0;
        int counterInversionFields=0;

        double result=0.0;
        for(int y = 0; y < sf.spielfeldhoehe; y++){
            for(int x = 0; x < sf.spielfeldbreite; x++){
                if(sf.spielfeld[x][y]!='-'){
                    counterGeneralUseableFields++;
                }
                if(playerListComplete.contains(sf.spielfeld[x][y])){
                    counterFieldsOnedByAnyPlayer++;
                }
                if(sf.spielfeld[x][y]=='b'){
                    counterBonusFieldsLeft++;
                }
                if(sf.spielfeld[x][y]=='c'){
                    counterChoiceFields++;
                }
                if(sf.spielfeld[x][y]=='i'){
                    counterInversionFields++;
                }
            }
        }
        this.fieldsWichAreUsable =counterGeneralUseableFields;
        this.fieldsOnedByAnyPlayer =counterFieldsOnedByAnyPlayer;
        this.bonusFieldsLeft =counterBonusFieldsLeft;
        this.choiceFieldsLeft =counterChoiceFields;
        this.inversionFieldsLeft=counterInversionFields;
        if(false) {
            System.out.println("counterGeneralUseableFields: " + counterGeneralUseableFields);
            System.out.println("fieldsOnedByAnyPlayer: " + counterFieldsOnedByAnyPlayer);
            System.out.println("BonusFIeldsLeft: " + counterBonusFieldsLeft);
            System.out.println("InversionFields left: " + counterInversionFields);
            System.out.println("ChoiceFields left: " + counterChoiceFields);
        }

        if(counterGeneralUseableFields!=0){
            result=(double)counterFieldsOnedByAnyPlayer/(double)counterGeneralUseableFields;
        }
        return result;
    }



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

}
