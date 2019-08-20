import java.util.ArrayList;
import java.util.List;

public class ComplexRating {

    private SimpleRating sr;
    private Evaluator ev;
    private Spielfeld sf;
    private GameState initialGamestate;
    private Statistics statistics;


    //start Values (Updated at begin of move int getBestMoveIterativeDeepening
    private int mobilityStart;
    private int stonesInPossessionStart;
    private int staticThingsRatingStart;
    private int ratingBonusFieldStart;


    //dynamic fieldValues
    double currentGameProgress=0;
    private int fieldsWichAreUsable =0;
    private int fieldsOnedByAnyPlayer =0;
    private int bonusFieldsLeft =0;
    private int choiceFieldsLeft =0;
    private int inversionFieldsLeft=0;



    //StaticFieldValues
    int[][] staticFieldValues;


    public int ratingForBottomNodes(GameState gameStateToBeEvaluated, ArrayList<Player> playerHierarchyAtBeginOfCurrentMove){
        int rating=0;
        int simpleRatingStoneCount=0;
        int simpleRatingMobility=0;
        int simpleRatingStaticThings=0;
        int simpleRatingBonusFields=0;
        int simpleRatingFieldsWithGoodDistance = 0;
        int simpleRatingFieldsWithBadDistance = 0;
        double simpleRatingHierarchy=0;
        double ratingProportion=0;

        if(!GameInfo.usingOverrideForThisMove) {
            rating = 0;
            simpleRatingStoneCount = sr.getSimpleRatingCountStones(gameStateToBeEvaluated, gameStateToBeEvaluated.ownPlayer.playerIdChar);
            simpleRatingMobility = sr.getSimpleRatingMobility(gameStateToBeEvaluated);
            simpleRatingStaticThings = sr.getSimpleRatingStaticThings(gameStateToBeEvaluated.ownPlayer.playerIdChar, gameStateToBeEvaluated, ev.getCorners(), ev.getBoarders());
            simpleRatingHierarchy=sr.getSimpleRatingHierarchy(gameStateToBeEvaluated,playerHierarchyAtBeginOfCurrentMove);
            simpleRatingBonusFields=sr.getSimpleRatingBonusFields(ev.getBonusFields(),ev.getGoodDistanceForBonusFields(),ev.getBadDistanceForBonusFields(),gameStateToBeEvaluated);
            simpleRatingFieldsWithBadDistance = gameStateToBeEvaluated.checkHowManyOfThisFieldsAreInPossession(ev.getBadDistanceForBonusFields());
            simpleRatingFieldsWithGoodDistance = gameStateToBeEvaluated.checkHowManyOfThisFieldsAreInPossession(ev.getGoodDistanceForBonusFields());
            int counterForAverageCalculation=0;
            double StoneCountProportion=1;
            int StoneCountRating = 0;
            if(stonesInPossessionStart!=0) {
                StoneCountProportion=(double)simpleRatingStoneCount/(double)stonesInPossessionStart;
                if (StoneCountProportion > 1) {
                    StoneCountRating += (StoneCountProportion - 1) * 100;
                } else {
                    StoneCountRating -= (1 - StoneCountProportion) * 100;
                }
            }
            counterForAverageCalculation+= RatingValues.gewichtungStoneCount;

            double mobilityProportion = 1;
            int ratingMobility = 0;
            if (mobilityStart != 0) {
                mobilityProportion = (double) simpleRatingMobility / (double) mobilityStart;
                if (mobilityProportion > 1) {
                    ratingMobility += (mobilityProportion - 1) * 100;
                } else {
                    ratingMobility -= (1 - mobilityProportion) * 100;
                }
            }
            counterForAverageCalculation += RatingValues.gewichtungMobility;


            int ratingStaticThings = 0;
            ratingStaticThings = simpleRatingStaticThings - staticThingsRatingStart;

            double bonusFieldProportion=1;
            if(ratingBonusFieldStart!=0){
                bonusFieldProportion=(double)simpleRatingBonusFields/(double)ratingBonusFieldStart;
            }
            else{
                if(simpleRatingBonusFields!=0){
                    if(simpleRatingBonusFields>6){
                        bonusFieldProportion=1.9;
                    }
                    else if (simpleRatingBonusFields>4){
                        bonusFieldProportion=1.7;
                    }
                    else if (simpleRatingBonusFields>2){
                        bonusFieldProportion=1.5;
                    }
                    else if (simpleRatingBonusFields>0){
                        bonusFieldProportion=1.9;
                    }
                    else if (simpleRatingBonusFields>-2){
                        bonusFieldProportion=0.8;
                    }
                    else if (simpleRatingBonusFields>-4){
                        bonusFieldProportion=0.6;
                    }
                    else {
                        bonusFieldProportion=0.4;
                    }
                }
            }
            counterForAverageCalculation+= RatingValues.gewichtungBonusFields;

            double HierarchyProportion=1;
            if(true){
                HierarchyProportion=simpleRatingHierarchy;
            }
            counterForAverageCalculation+=RatingValues.gewichtungHierarchy;

            if (true) {
                //staticThingsProportion = 0;
                RatingValues.gewichtungHierarchy = 0;
                bonusFieldProportion = 0;

            }
            rating = (int) ((RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungStoneCount * StoneCountRating) + (RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungMobility * ratingMobility) + (simpleRatingFieldsWithBadDistance * RatingValues.fieldswithBadDistanceAnpassung) + (simpleRatingFieldsWithGoodDistance * RatingValues.fieldsWithGoodDIstanceAnpassung));
            //rating = (int) ((RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungStoneCount * StoneCountRating) + (RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungMobility * ratingMobility) + (simpleRatingFieldsWithBadDistance * RatingValues.fieldswithBadDistanceAnpassung) + (simpleRatingFieldsWithGoodDistance * RatingValues.fieldsWithGoodDIstanceAnpassung) + (ratingStaticThings * RatingValues.staticThingsAnpassung * RatingValues.gewichtungStaticThings));
            if(GameInfo.notTestMode&&false){
                System.out.println("------------------------------ratingOFBottomNodes------------------------------------------");
                //System.out.println("StoneCOnuntRating: " + StoneCountRating);
                System.out.println("RatingValues.ausgleichsfaktorForDynamicValues*RatingValues.gewichtungStoneCount*StoneCountRating: " + RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungStoneCount * StoneCountRating);
                //System.out.println("ratingMobility: " + ratingMobility);
                System.out.println("RatingValues.ausgleichsfaktorForDynamicValues*RatingValues.gewichtungMobility*ratingMobility: " + (RatingValues.ausgleichsfaktorForDynamicValues * RatingValues.gewichtungMobility * ratingMobility));
                //System.out.println("ratingStaticTHings: "+ ratingStaticThings);
                //System.out.println("ratingStaticThings*RatingValues.staticThingsAnpassung*RatingValues.gewichtungStaticThings: " + ratingStaticThings * RatingValues.staticThingsAnpassung * RatingValues.gewichtungStaticThings);
                System.out.println("FieldsWithGoodDistance*Anpassung: " + simpleRatingFieldsWithGoodDistance * RatingValues.fieldsWithGoodDIstanceAnpassung);
                System.out.println("FieldsWithBadDistance*Anpassung: " + simpleRatingFieldsWithBadDistance * RatingValues.fieldswithBadDistanceAnpassung);
                System.out.println("rating: " + rating);
                //System.out.println("Rating setzt sich zusammen: SC:"+);
                //System.out.println("StoneCountProportion:  "+StoneCountProportion);
                //System.out.println("StoneCountProportionWithAusgleich:  "+RatingValues.ausgleichsfaktorForDynamicValues*StoneCountProportion);
                // System.out.println("mobilityProportion:  "+mobilityProportion);
                //System.out.println("mobilityProportionWithAusgleich:  "+RatingValues.ausgleichsfaktorForDynamicValues*mobilityProportion);
                //System.out.println("staticThingsProportion:  "+staticThingsProportion);
                //System.out.println("bonusFieldProportion:  "+bonusFieldProportion);
                //System.out.println("simpleRatingHierarchy:  "+simpleRatingHierarchy);
                System.out.println("currentGameProgress: "+currentGameProgress);
                System.out.println("------------------------------ratingOFBottomNodes------------------------------------------");
            }
        }


        else{
            int overrideRating = 0;

            if (currentGameProgress <= 0.95) {

                overrideRating = gameStateToBeEvaluated.possibleMovesWithoutOverrides(gameStateToBeEvaluated.ownPlayer.playerIdChar);
                //System.out.println("Mobility!");

                if (overrideRating == 0) {
                    overrideRating = sr.getSimpleRatingCountStones(gameStateToBeEvaluated, gameStateToBeEvaluated.ownPlayer.playerIdChar);
                    //System.out.println("CountStonesThoughMobility");
                } else {
                    overrideRating += 1000;//Stellt sicher dass Mobilität vor Steinezählen vorrang bekommt
                }
            } else {
                overrideRating = sr.getSimpleRatingCountStones(gameStateToBeEvaluated, gameStateToBeEvaluated.ownPlayer.playerIdChar);
            }
            return overrideRating;
        }

        //rating=(int)(ratingProportion*1000);
        return rating;
    }


    ComplexRating(GameState gs,Statistics statistics) {
        this.initialGamestate=gs;
        this.sf = gs.sf;
        this.ev = new Evaluator(this.initialGamestate);
        this.staticFieldValues=ev.getStaticFieldValues();
        /*output();
        for (int[] boarderField : ev.getBoarders()) {
            System.out.println("x: " + boarderField[0] + "  y:  " + boarderField[1]);
        }*/
        this.statistics=statistics;
        this.sr=new SimpleRating(statistics);


    }

    public void updateFieldValues(){
        ev.clearLists();
        this.staticFieldValues=ev.getStaticFieldValues();
        this.currentGameProgress=ev.getCurrentGameProgressAndUpdateDynamicFieldValues();
        //output();
        //outputList("BonusFields", ev.getBonusFields());
        //outputList("Fields perfect Distance", ev.getGoodDistanceForBonusFields());
        //outputList("Fields with bad Distance", ev.getBadDistanceForBonusFields());
        updateDynamicFieldValues();
        setRatingeValues();
        //System.out.println("currentGameProgress: "+currentGameProgress);
    }
    public void updateDynamicFieldValues(){
        this.fieldsWichAreUsable =ev.fieldsWichAreUsable;
        this.fieldsOnedByAnyPlayer =ev.fieldsOnedByAnyPlayer;
        this.bonusFieldsLeft =ev.bonusFieldsLeft;
        this.choiceFieldsLeft =ev.choiceFieldsLeft;
        this.inversionFieldsLeft=ev.inversionFieldsLeft;

    }

    public void updateStartValues(GameState originalGameState){
        this.mobilityStart = originalGameState.showMoves(originalGameState.ownPlayer.playerIdChar, false).size();
        this.stonesInPossessionStart = sr.getSimpleRatingCountStones(originalGameState, originalGameState.ownPlayer.playerIdChar);
        this.ratingBonusFieldStart=sr.getSimpleRatingBonusFields(ev.getBonusFields(),ev.getGoodDistanceForBonusFields(),ev.getBadDistanceForBonusFields(),originalGameState);
        this.staticThingsRatingStart = sr.getSimpleRatingStaticThings(initialGamestate.ownPlayer.playerIdChar, originalGameState, ev.getCorners(), ev.getBoarders());
    }

    public ArrayList<Integer[]> checkForPossibleCorner(List<Integer[]> possibleMoves, GameState originalGamestate) {

        return originalGamestate.checkForPossibleCorners(possibleMoves, ev.getCorners());
    }

    public Integer[] checkForPossibleFieldNearBonus(List<Integer[]> possibleMoves, GameState originalGamestate){
        return originalGamestate.checkForFieldsNearBonus(possibleMoves, ev.getGoodDistanceForBonusFields());
    }

    public boolean PositiveChangeInHierarchy(GameState gameState, List<Player> playerHierarchyAtBeginOfCurrentMove){
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
        //System.out.println("PlayerList size: "+gameState.playerList.size());
        if(newIndex<=oldIndex-2){
            System.out.println("2 Z: "+statistics.move);
            return true;
        }
        if(newIndex<=oldIndex-1&&gameState.playerList.size()==2||gameState.playerList.size()==3){
            System.out.println("1 Z: "+statistics.move);
            return true;
        }
        if(newIndex<=oldIndex-1&&oldIndex==1){
            System.out.println("1... Z"+statistics.move);
            return true;
        }
        return false;
    }

    public void setRatingeValues(){//sets the Rating Values of the different Factors which are used
        double firstAusgleichsFaktor=1;
        double secondAusgleichsfaktor=1;
        if(this.fieldsWichAreUsable>0&&fieldsWichAreUsable<=100){
            firstAusgleichsFaktor=1;
        }
        if(this.fieldsWichAreUsable>100&&fieldsWichAreUsable<=400){
            firstAusgleichsFaktor=1.3;
        }
        if(this.fieldsWichAreUsable>400&&fieldsWichAreUsable<=900){
            firstAusgleichsFaktor=1.6;
        }
        if(this.fieldsWichAreUsable>900&&fieldsWichAreUsable<=1600){
            firstAusgleichsFaktor=2;
        }
        if(fieldsWichAreUsable>=1600){
            firstAusgleichsFaktor=2.5;
        }

        if(currentGameProgress>0.90){
            secondAusgleichsfaktor=2.5;
            RatingValues.gewichtungMobility=1;
            RatingValues.gewichtungStoneCount=4;
        }
        if(currentGameProgress>0.70&&currentGameProgress<=0.90){
            secondAusgleichsfaktor=2.25;
            RatingValues.gewichtungMobility=1;
            RatingValues.gewichtungStoneCount=3;
        }
        if(currentGameProgress>0.50&&currentGameProgress<=0.70){
            secondAusgleichsfaktor=2;
            RatingValues.gewichtungMobility = 3;
            RatingValues.gewichtungStoneCount = 2;
        }
        if(currentGameProgress>0.30&&currentGameProgress<=0.50){
            secondAusgleichsfaktor=2;
            RatingValues.gewichtungMobility=3;
            RatingValues.gewichtungStoneCount = 1;
        }
        if(currentGameProgress>0.10&&currentGameProgress<=0.30){
            secondAusgleichsfaktor=1.3;
            RatingValues.gewichtungMobility=3;
            RatingValues.gewichtungStoneCount=1;
        }
        if(currentGameProgress<=0.10){
            secondAusgleichsfaktor=1;
            RatingValues.gewichtungMobility=4;
            RatingValues.gewichtungStoneCount=1;
        }

        if(bonusFieldsLeft==0){
            RatingValues.gewichtungBonusFields=0;
        }
        RatingValues.ausgleichsfaktorForDynamicValues=firstAusgleichsFaktor*secondAusgleichsfaktor;
        if(false) {
            System.out.println("gewichtungMobility: " + RatingValues.gewichtungMobility);
            System.out.println("gewichtungStoneCount: " + RatingValues.gewichtungStoneCount);
        }

    }

    public int calculateDistancetoNearestBonus(GameState originalGamestat){
        return originalGamestat.calculateDistancetoNearestBonus(ev.getBonusFields());
    }


    public void output() {
        if (GameInfo.notTestMode||true) {
            System.out.println("Rating:");

            for (int i = 0; i < this.staticFieldValues.length; i++) {
                if (i == 0) {
                    System.out.print("     " + i + "  ");
                } else if (i < 10) {
                    System.out.print(i + "   ");
                } else {
                    System.out.print(i + "  ");
                }
            }//Numeriert die Spielfeldspalten (VP)
            System.out.println();//Numeriert die Spielfeldspalten (VP)
            System.out.println();

            for (int i = 0; i < this.staticFieldValues[0].length; i++) {

                if (i < 10) {
                    System.out.print(i + "   ");
                } else {
                    System.out.print(i + "  ");
                }//Numeriert Spielfeldzeilen

                for (int j = 0; j < this.staticFieldValues.length; j++) {
                    if(this.staticFieldValues[j][i]!=Integer.MIN_VALUE){
                        if(this.staticFieldValues[j][i]>=0){
                            System.out.print(" ");
                        }
                        System.out.print(this.staticFieldValues[j][i]);

                        if(this.staticFieldValues[j][i]>-10&&this.staticFieldValues[j][i]<10){
                            System.out.print(" ");
                        }
                        System.out.print(" ");
                    }
                    else{
                        System.out.print(" -" + "  ");
                    }

                }
                System.out.println();
            }
        }
    }

    public void outputReachableBonusFields(GameState originalGamestate) {

        List<int[]> reachableBonusFields = originalGamestate.reachableBonusFields(ev.getBonusFields());

        System.out.println("Size: " + reachableBonusFields.size());

        for (int[] field : reachableBonusFields) {
            System.out.println("X: " + field[0] + "  Y: " + field[1]);
        }
    }

    public void outputList(String name, List<int[]> list) {
        System.out.println("--------------------------" + name + "--------------------------");
        for (int[] element : list) {
            System.out.println("feld: " + element[0] + ", " + element[1]);
        }
    }

    public List<int[]> getFieldswithBadDistanceTooBonus() {
        return ev.getBadDistanceForBonusFields();
    }
}


