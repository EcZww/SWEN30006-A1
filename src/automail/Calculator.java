package automail;

import com.unimelb.swen30006.wifimodem.WifiModem;
import simulation.Building;

public class Calculator {
    private double charge;
    private double activityCost;
    private double serviceFee;
    private int lookUpCount;
    private final double LOOKUP=0.1;

    private WifiModem wifiModem = WifiModem.getInstance(Building.MAILROOM_LOCATION);
    private Building building = Building.getInstance();
    private Price price = Price.getInstance();


    public Calculator() throws Exception {
        this.charge = 0;
        this.activityCost = 0;
        this.serviceFee = 0;
        this.lookUpCount = 0;
    }

    /** can add more features in future eg. Weight, delay penalty**/
    public void calculateCharge(MailItem deliveryItem, int destination, double activityUnits, boolean finalCharge){
        serviceFee = lookUpServiceFee(destination);
        /** charge tenant only once for lookup hence +LOOKUP**/
        activityCost = calculateActivityCost(activityUnits+LOOKUP, price.ACTIVITY_UNITPRICE);

        charge = (serviceFee + activityCost)*(1+price.MARKUP_PERCENTAGE);
        if(finalCharge) {
            deliveryItem.setFinalCharge(charge);
            deliveryItem.setServiceFee(serviceFee);
            deliveryItem.setActivityUnits(activityUnits+(lookUpCount*LOOKUP));
            deliveryItem.setActivityUnitPrice(price.ACTIVITY_UNITPRICE);
        }
        else{
            deliveryItem.setExpectedCharge(charge);
        }
    }


    public double calculateActivityCost(double activityUnits,double activityUnitPrice) {
        activityCost = activityUnits * activityUnitPrice;
        return activityCost;
    }

    public double lookUpServiceFee(int current_floor){
        lookUpCount=0;
        serviceFee = wifiModem.forwardCallToAPI_LookupPrice(current_floor);
        /** if lookup fails, keep looking up until a price is returned, count number of lookups**/
        if(serviceFee ==-1) {
            while (serviceFee == -1) {
                /** check if current floor has a previously looked up service fee recorded**/
                serviceFee = building.getFloorServiceFee(current_floor);
                /** if not, request lookup from wifimodem**/
                if(serviceFee == -1) {
                    serviceFee = wifiModem.forwardCallToAPI_LookupPrice(current_floor);
                }
                lookUpCount += 1;
            }
        }
        lookUpCount += 1;
        building.insertServiceFee(current_floor,serviceFee);
        return serviceFee;
    }

    public double getLookUpCount() {
        return lookUpCount;
    }
}

