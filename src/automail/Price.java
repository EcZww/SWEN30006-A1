package automail;

import simulation.Building;

public class Price {
    protected final double ACTIVITY_UNITPRICE;
    protected final double MARKUP_PERCENTAGE;
    protected final double CHARGE_THRESHOLD;
    protected static Price _instance = null;

    public Price(double ACTIVITY_UNITPRICE, double MARKUP_PERCENTAGE, double CHARGE_THRESHOLD) {
        this.ACTIVITY_UNITPRICE = ACTIVITY_UNITPRICE;
        this.MARKUP_PERCENTAGE = MARKUP_PERCENTAGE;
        this.CHARGE_THRESHOLD = CHARGE_THRESHOLD;
    }

    public static Price getInstance(double ACTIVITY_UNITPRICE, double MARKUP_PERCENTAGE,
                                    double CHARGE_THRESHOLD) throws Exception {
        if (_instance == null) {
            synchronized(Building.class) {
                if (_instance == null) {
                    _instance = new Price(ACTIVITY_UNITPRICE,MARKUP_PERCENTAGE,CHARGE_THRESHOLD);
                }
            }
        }
        return _instance;
    }

    public static Price getInstance(){
        if(_instance!=null){
            return _instance;
        }
        else{
            return null;
        }


    }
}
