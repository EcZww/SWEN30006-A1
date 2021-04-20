package simulation;


import java.util.HashMap;

public class Building {

    private HashMap<Integer,Double> ServiceFeeTable = new HashMap<>();

    private static Building _instance = null;
	
    /** The number of floors in the building **/
    public static int FLOORS;
    
    /** Represents the ground floor location */
    public static final int LOWEST_FLOOR = 1;
    
    /** Represents the mailroom location */
    public static final int MAILROOM_LOCATION = 1;

    public void insertServiceFee(int floor,double serviceFee){
        ServiceFeeTable.put(floor,serviceFee);
    }


    public double getFloorServiceFee(int floor){
        if (ServiceFeeTable.containsKey(floor)){
            return ServiceFeeTable.get(floor);
        }else {
            return -1;
        }
    }

    public static Building getInstance() throws Exception {
        if (_instance == null) {
            synchronized(Building.class) {
                if (_instance == null) {
                    _instance = new Building();
                }
            }
        }
        return _instance;
    }

}
