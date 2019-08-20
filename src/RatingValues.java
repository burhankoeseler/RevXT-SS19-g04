public class RatingValues {
    public static int valueCorner = 5;
    public static int valuexorC = -4;
    public static int valueBoarder = 5;
    public static int valueNearToBonus=4;
    public static int valueBonus = 5;
    public static  int valueTooNearToBonus=-3;
    //For ComplexRating
    public static int gewichtungStoneCount =1;//wird seperat festgelegt in COmplex Ratin set RatingValues
    public static int gewichtungMobility=1;//wird seperat festgelegt in COmplex Ratin set RatingValues
    public static int gewichtungStaticThings = 1;
    public static int gewichtungBonusFields=6;
    public static int gewichtungHierarchy=5;
    public static double ausgleichsfaktorForDynamicValues=1;//wird seperat festgelegt in COmplex Ratin set RatingValues
    //For ComplexRating Anpassung
    public static int staticThingsAnpassung = 75;
    public static int fieldsWithGoodDIstanceAnpassung = 400;
    public static int fieldswithBadDistanceAnpassung = -500;

    //For SimpleRating
    public static int gewichtungCorner = 3; //um ein wievielfaches wird eine Ecke im Vergleich zu einer Kante bewertet (Simplerating getStaticFieldValues)
    /*Diese Werte werden noch + 1 gerechnet und dann als multiplikator des gesamtergebnisses verwendet*/
    public static int bonusfieldsTakenValue = 3;
    public static int fieldsWithGoodDistanceTakenValue=1;
    public static int fieldswithBadDistanceTakenValue=-1;
    /*Diese Werte werden noch + 1 gerechnet und dann als Multiplikator des gesamtergebnisses verwendet*/
}
