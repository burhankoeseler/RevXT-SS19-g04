import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Spielfeld {

    public int spieler;
    public int ueberschreibsteine;
    public int bomben;
    public int bombenStaerke;
    public int spielfeldhoehe;
    public int spielfeldbreite;
    public char[][] spielfeld;
    public List<Integer[]> transitionen = new ArrayList<>();
    public int anzahlTransitionen;

    public Spielfeld(String map){ //Hauptkonstruktor (Für die Karte die man vom Server erhält)
        scanMap(map);
    }
    public  Spielfeld(){ //Testkonstruktor (Falls man eine karte direkt, ohne Serververbindung, laden möchte

    }

    public Spielfeld(Spielfeld other) {
        this.spieler = other.spieler;
        this.ueberschreibsteine = other.ueberschreibsteine;
        this.bomben = other.bomben;
        this.bombenStaerke = other.bombenStaerke;
        this.spielfeldhoehe = other.spielfeldhoehe;
        this.spielfeldbreite = other.spielfeldbreite;
        this.transitionen = other.transitionen;
        this.anzahlTransitionen = other.anzahlTransitionen;

        this.spielfeld = new char[other.spielfeld.length][];
        for (int i=0; i<other.spielfeld.length; i++){
            this.spielfeld[i] = other.spielfeld[i].clone();
        }
    }

    public int getSpieler() {
        return spieler;
    }
    public void setSpieler(int spieler) {
        this.spieler = spieler;
    }
    public int getUeberschreibsteine() {
        return ueberschreibsteine;
    }
    public void setUeberschreibsteine(int ueberschreibsteine) {
        this.ueberschreibsteine = ueberschreibsteine;
    }
    public int getBomben() {
        return bomben;
    }
    public void setBomben(int bomben) {
        this.bomben = bomben;
    }
    public int getBombenStaerke() {
        return bombenStaerke;
    }
    public void setBombenStaerke(int bomben_staerke) {
        this.bombenStaerke = bomben_staerke;
    }
    public int getSpielfeldhoehe() {
        return spielfeldhoehe;
    }
    public void setSpielfeldhoehe(int spielfeldhoehe) {
        this.spielfeldhoehe = spielfeldhoehe;
    }
    public int getSpielfeldbreite() {
        return spielfeldbreite;
    }
    public void setSpielfeldbreite(int spielfeldbreite) {
        this.spielfeldbreite = spielfeldbreite;
    }
    public char[][] getSpielfeld() {
        return spielfeld;
    }
    public void setSpielfeld(char[][] spielfeld) {
        this.spielfeld = spielfeld;
    }
    public List<Integer[]> getTransitionen() {
        return transitionen;
    }
    public void setTransitionen(List<Integer[]> transitionen) {
        this.transitionen = transitionen;
    }
    public int getAnzahlTransitionen() {
        return anzahlTransitionen;
    }
    public void setAnzahlTransitionen(int anzahl_transitionen) {
        this.anzahlTransitionen = anzahl_transitionen;
    }

    public void scanMap(){

        File map = new File("maps/test05_2p_BombenTest.map");

        try {
            Scanner sc = new Scanner(map);

            for (int k = 0; k < 6; k++){
                int int_value = sc.nextInt();

                switch (k){
                    case 0: this.setSpieler(int_value); break;
                    case 1: this.setUeberschreibsteine(int_value); break;
                    case 2: this.setBomben(int_value); break;
                    case 3: this.setBombenStaerke(int_value); break;
                    case 4: this.setSpielfeldhoehe(int_value); break;
                    case 5: this.setSpielfeldbreite(int_value); break;
                    default: if(GameInfo.notTestMode)System.out.println ("Error"); break;
                }
            }

            int hoehe = this.getSpielfeldhoehe();
            int breite = this.getSpielfeldbreite();
            char[][] spielfeld = new char[breite][hoehe];

            for(int y = 0; y < hoehe; y++){
                for(int x = 0; x < breite; x++){
                    spielfeld[x][y] = sc.next().charAt(0);
                }
            }
            this.setSpielfeld(spielfeld);

            int anzahlTransitionen = 0;
            List<Integer[]> transitionen = new ArrayList<>();
            while (sc.hasNextLine() && sc.hasNextInt()) {
                Integer[] transition = new Integer[6];
                for(int i = 0; i < 3; i++){
                    transition[i] = sc.nextInt();
                }
                sc.next().charAt(0);
                for(int i = 3; i < 6; i++){
                    transition[i] = sc.nextInt();
                }
                anzahlTransitionen++;
                transitionen.add(transition);
            }
            this.setAnzahlTransitionen(anzahlTransitionen);
            this.setTransitionen(transitionen);

            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void scanMap(String map){

            Scanner sc = new Scanner(map);

            for (int k = 0; k < 6; k++){
                int int_value = sc.nextInt();

                switch (k){
                    case 0: this.setSpieler(int_value); break;
                    case 1: this.setUeberschreibsteine(int_value); break;
                    case 2: this.setBomben(int_value); break;
                    case 3: this.setBombenStaerke(int_value); break;
                    case 4: this.setSpielfeldhoehe(int_value); break;
                    case 5: this.setSpielfeldbreite(int_value); break;
                    default: if(GameInfo.notTestMode)System.out.println ("Error"); break;
                }
            }

            int hoehe = this.getSpielfeldhoehe();
            int breite = this.getSpielfeldbreite();
        if(GameInfo.notTestMode&&false)System.out.println("Höhe: "+hoehe+",  Breite: "+breite);
            char[][] spielfeld = new char[breite][hoehe];

            for(int y = 0; y < hoehe; y++){
                for(int x = 0; x < breite; x++){
                    spielfeld[x][y] = sc.next().charAt(0);
                }
            }
            this.setSpielfeld(spielfeld);

            int anzahlTransitionen = 0;
            List<Integer[]> transitionen = new ArrayList<>();
            while (sc.hasNextLine() && sc.hasNextInt()) {
                Integer[] transition = new Integer[6];
                for (int i = 0; i < 3; i++) {
                    transition[i] = sc.nextInt();
                }
                sc.next().charAt(0);
                for (int i = 3; i < 6; i++) {
                    transition[i] = sc.nextInt();
                }
                anzahlTransitionen++;
                transitionen.add(transition);
                if (GameInfo.notTestMode && false) {
                    System.out.println("Array: " + transitionen.indexOf(transition));

                    for (int element : transition) {
                        if (GameInfo.notTestMode) System.out.print(element);
                    }
                    if (GameInfo.notTestMode) System.out.println();
                    if (transitionen.indexOf(transition) >= 166) {
                        if (GameInfo.notTestMode) System.out.println();
                    }
                }
            }
            this.setAnzahlTransitionen(anzahlTransitionen);
            this.setTransitionen(transitionen);
            //JSONFORMATTER CODE
            String[] transitionenString = new String[anzahlTransitionen];
            int iString = 0;
            for(Integer[] transition : transitionen){
                transitionenString[iString] = transition[0] +" "+ transition[1] +" "+ transition[2] +" <-> "+ transition[3] +" "+ transition[4] +" "+ transition[5];
                iString++;
            }
            JSONFormatter.addInitialMap(this.spieler, this.ueberschreibsteine, this.bomben, this.bombenStaerke, this.spielfeldhoehe, this.spielfeldbreite, this.reversedSfArray(), transitionenString);
            //JSONFORMATTER CODE ENDE
            sc.close();
            }//Überladene scan Map um die Karte vom Server auch einlesen zu können

    public void output(){
        if (spieler >= 2 && spieler <= 8 && spielfeldhoehe > 0 && spielfeldbreite > 0) {

            /*if(GameInfo.notTestMode)System.out.println("Spieler: " + spieler);
            if(GameInfo.notTestMode)System.out.println("Ueberschreibsteine " + ueberschreibsteine);
            if(GameInfo.notTestMode)System.out.println("Bomben:  " + bomben);
            if(GameInfo.notTestMode)System.out.println("Bombenstaerke: " + bombenStaerke);
            if(GameInfo.notTestMode)System.out.println("Spielfeldhoehe: " + spielfeldhoehe);
            if(GameInfo.notTestMode)System.out.println("Spielfeldbreite: " + spielfeldbreite);*/
            if(GameInfo.notTestMode)System.out.println("Spielfeld:");

            for(int i=0;i<spielfeldbreite;i++){
                if(i==0){
                    if(GameInfo.notTestMode)System.out.print("    "+i+"  ");
                }
                else if(i<10){
                    if(GameInfo.notTestMode)System.out.print(i+"  ");
                }
                else{
                    if(GameInfo.notTestMode)System.out.print(i+" ");
                }
            }//Numeriert die Spielfeldspalten (VP)
            if(GameInfo.notTestMode)System.out.println();//Numeriert die Spielfeldspalten (VP)
            if(GameInfo.notTestMode)System.out.println();

            for(int i = 0; i < spielfeldhoehe; i++){

                if(i<10){if(GameInfo.notTestMode)System.out.print(i+"   ");}else{if(GameInfo.notTestMode)System.out.print(i+"  ");}//Numeriert Spielfeldzeilen

                for(int j = 0; j < spielfeldbreite; j++){
                    if(GameInfo.notTestMode) System.out.print(spielfeld[j][i] + "  ");
                }
                if(GameInfo.notTestMode)System.out.println();
            }

            /*if(GameInfo.notTestMode)System.out.println("Transitionen:");
            for(Integer[] transition : transitionen){
                for(int i = 0; i < 3; i++) System.out.print(transition[i] + " ");
                System.out.print("<-> ");
                for(int i = 3; i < 6; i++) System.out.print(transition[i] + " ");
                if(GameInfo.notTestMode)System.out.println();
            }*/
        }
        else {
            if(GameInfo.notTestMode)System.out.println("Error: Kein gueltiges Spielfeld definiert");
        }
    }
    
    /*Output Wertung unformatiert*/
    public void output(int[][] gewerteteFelder) {
    	for(int i = 0; i < spielfeldhoehe; i++){
            for(int j = 0; j < spielfeldbreite; j++){
                System.out.print(gewerteteFelder[j][i] + " ");
            }
            if(GameInfo.notTestMode)System.out.println();
        }
    }

    public char[][] reversedSfArray(){
        char[][] originalSf=this.spielfeld;
        char[][] reversedSf=new char[this.getSpielfeldhoehe()][this.getSpielfeldbreite()];
        if(GameInfo.notTestMode&&false) {
            System.out.println("-----------------------reversedSFArray---------------------");
            System.out.println("Spielfeldhoehe: " + this.getSpielfeldhoehe() + "  Spielfeldbreite: " + this.getSpielfeldbreite());
            System.out.println("original Array length: " + originalSf.length + "   original Array[0] length: " + originalSf[0].length);
            System.out.println("converted Array length: " + reversedSf.length + "   converted Array[0] length: " + reversedSf[0].length);
            System.out.println("-----------------------reversedSFArray---------------------");
        }
        for(int i=(originalSf.length-1);i>=0;i--){
            for(int j=(originalSf[0].length-1);j>=0;j--){
                reversedSf[j][i]=originalSf[i][j];
            }

        }
        return reversedSf;
    }
    public void outputReversedSfArray(char[][] reversedArray) {
        if (GameInfo.notTestMode&&false) {

            System.out.println("---------------------outputReversedSfArray---------------------------");
            System.out.println("Array größe: " + reversedArray.length);
            System.out.println("Array[0] Größe: " + reversedArray[0].length);
            System.out.println("Abgeändert");
            for (char[] Array : reversedArray) {
                for (char value : Array) {
                    System.out.print(value);
                }
                System.out.println();
            }
            System.out.println(build2dArrayString(reversedArray));
            System.out.println();
            System.out.println("Orginal");
            for (char[] Array : this.spielfeld) {
                for (char value : Array) {
                    System.out.print(value);
                }
                System.out.println();
            }
            System.out.println(build2dArrayString(this.spielfeld));
            System.out.println("---------------------outputReversedSfArrayEnde-----------------------");
        }
    }

    private static String build2dArrayString(char[][] array) {
        StringBuilder arrayString = new StringBuilder();
        arrayString.append('[');
        for (int y = 0; y < array.length; y++) {
            arrayString.append('[');
            for (int x = 0; x < array[y].length; x++) {
                arrayString.append(String.format("\"%s\"", array[y][x]));
                if (!(x == array[y].length - 1)) {
                    arrayString.append(',');
                }
            }
            arrayString.append(']');
            if (!(y == array.length - 1)) {
                arrayString.append(',');
            }
        }
        arrayString.append(']');
        return arrayString.toString();
    }
}
