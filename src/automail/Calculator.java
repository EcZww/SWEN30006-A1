package automail;

import com.unimelb.swen30006.wifimodem.WifiModem;
import simulation.Building;
public class Calculator {
    private double charge;
    private double activityCost;
    private double serviceFee;
    private int lookUpCount;

    private WifiModem wifiModem = WifiModem.getInstance(Building.MAILROOM_LOCATION);
    private Building building = Building.getInstance();


    public Calculator() throws Exception {
        this.charge = 0;
        this.activityCost = 0;
        this.serviceFee = 0;
        this.lookUpCount = 0;
    }

    public void calculateCharge(MailItem deliveryItem, int current_floor, double markupPercentage, double activityUnitPrice
            , double currentActivityUnits, boolean finalCharge){
        serviceFee = lookUpServiceFee(current_floor);
        activityCost = calculateActivityCost(currentActivityUnits,activityUnitPrice);
        //System.out.println(currentActivityUnits);
        charge = (serviceFee + activityCost)*(1+markupPercentage);
        System.out.printf("charge = (%.3f + %.3f * %.3f)*(1+%.3f) = %.2f%n",
         serviceFee,currentActivityUnits,activityUnitPrice,markupPercentage,charge);
        if(finalCharge) {
            deliveryItem.setFinalCharge(charge);
            deliveryItem.setServiceFee(serviceFee);
            deliveryItem.setActivityUnits(currentActivityUnits);
            deliveryItem.setActivityUnitPrice(activityUnitPrice);
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
        if(serviceFee ==-1) {
            while (serviceFee == -1) {
                System.out.println("failed");
                serviceFee = building.getFloorServiceFee(current_floor);
                if(serviceFee == -1) {
                    serviceFee = wifiModem.forwardCallToAPI_LookupPrice(current_floor);
                }
                lookUpCount += 1;
            }
        }
        lookUpCount += 1;
        System.out.println("Looked up "+lookUpCount+" times,serviceFee is "+serviceFee);
        building.insertServiceFee(current_floor,serviceFee);
        return serviceFee;
    }

    public double getLookUpCount() {
        return lookUpCount;
    }
}

