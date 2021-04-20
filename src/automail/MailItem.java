package automail;


import java.util.Map;
import java.util.TreeMap;


/**
 * Represents a mail item
 */
public class MailItem {
	
    /** Represents the destination floor to which the mail is intended to go */
    protected final int DESTINATION_FLOOR;
    /** The mail IDentifier */
    protected final String ID;
    /** The time the mail item arrived */
    protected final int ARRIVAL_TIME;
    /** The WEIGHT in grams of the mail item */
    protected final int WEIGHT;
    private final boolean COMMERCIAL_DISPLAY;

    protected String deliveringRobotId;

    private double finalCharge;
    private double expectedCharge;
    private double activityUnits;
    private double serviceFee;
    private double activityUnitPrice;




    /**
     * Constructor for a MailItem
     * @param dest_floor the destination floor intended for this mail item
     * @param ARRIVAL_TIME the time that the mail arrived
     * @param WEIGHT the WEIGHT of this mail item
     * @param commercial_display switch for turning on/off commercial stats display
     *
     */
    public MailItem(int dest_floor, int ARRIVAL_TIME, int WEIGHT, boolean commercial_display) {
        this.DESTINATION_FLOOR = dest_floor;
        this.ID = String.valueOf(hashCode());
        this.ARRIVAL_TIME = ARRIVAL_TIME;
        this.WEIGHT = WEIGHT;
        this.finalCharge = 0;
        this.expectedCharge = 0;
        this.activityUnits = 0;
        this.serviceFee = 0;
        this.activityUnitPrice =0;
        this.COMMERCIAL_DISPLAY = commercial_display;
    }

    public String toString(boolean deliveredStat){
        /** check if called by delivery call and if cost stats needs to be displayed **/
        if(deliveredStat && COMMERCIAL_DISPLAY) {
            return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d | Charge: %.2f" +
                            " | Cost: %.2f | Fee: %.2f | Activity: %.2f"
                    , ID, ARRIVAL_TIME, DESTINATION_FLOOR, WEIGHT, finalCharge,
                    activityUnits*activityUnitPrice,serviceFee,activityUnits);
        }
        else{
            return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d"
                    , ID, ARRIVAL_TIME, DESTINATION_FLOOR, WEIGHT);
        }
    }

    /**
     *
     * @return the destination floor of the mail item
     */
    public int getDestFloor() {
        return DESTINATION_FLOOR;
    }

    /**
     *
     * @return the ID of the mail item
     */
    public String getId() {
        return ID;
    }

    /**
     *
     * @return the arrival time of the mail item
     */
    public int getArrivalTime(){
        return ARRIVAL_TIME;
    }

    /**
    *
    * @return the WEIGHT of the mail item
    */
   public int getWeight(){
       return WEIGHT;
   }

	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

    public double getFinalCharge() {
        return finalCharge;
    }

    public void setFinalCharge(double finalCharge) {
        this.finalCharge = finalCharge;
    }

    public double getExpectedCharge() {
        return expectedCharge;
    }

    public void setExpectedCharge(double expectedCharge) {
        this.expectedCharge = expectedCharge;
    }

    public void setDeliveringRobotId(String deliveringRobotId) {
        this.deliveringRobotId = deliveringRobotId;
    }

    public double getActivityUnits() {
        return activityUnits;
    }

    public void setActivityUnits(double activityUnits) {
        this.activityUnits = activityUnits;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public void setActivityUnitPrice(double activityUnitPrice) {
        this.activityUnitPrice = activityUnitPrice;
    }
}
