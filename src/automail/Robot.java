package automail;


import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Building;
import simulation.Clock;
import simulation.IMailDelivery;



/**
 * The robot delivers mail!
 */
public class Robot {
	
    public static final int INDIVIDUAL_MAX_WEIGHT = 2000;
    /** new changes**/
    public static final int MOVEMENT = 5;
    public static final double LOOKUP = 0.1;

    IMailDelivery delivery;
    protected final String ID;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private boolean receivedDispatch;
    /**new changes**/
    private double currentActivityUnits;
    private double totalBillableActivityUnits;

    private final MailPool MAILPOOL;
    private final Calculator CALCULATOR = new Calculator();

    private MailItem deliveryItem = null;
    private MailItem tube = null;

    private int deliveryCounter;


    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.

     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, MailPool mailPool, int number) throws Exception {
    	this.ID = "R" + number;
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.MAILPOOL = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        this.currentActivityUnits = 0;
        this.totalBillableActivityUnits = 0;
    }

    /**
     * This is called when a robot is assigned the mail items and ready to dispatch for the delivery
     */
    public void dispatch() {
    	receivedDispatch = true;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate() throws ExcessiveDeliveryException {
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                	if (tube != null) {
                        MAILPOOL.addToPool(tube);
                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                	}
        			/** Tell the sorter the robot is ready */
                    MAILPOOL.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);

                    break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
                	setDestination();
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor){ // If already here drop off either way
                    /** Delivery complete, report this to the simulator! **/


                    double mailActivityUnits = (deliveryItem.getDestFloor() - Building.MAILROOM_LOCATION)*2*MOVEMENT;
                    CALCULATOR.calculateCharge(deliveryItem,current_floor, mailActivityUnits, true);
                    currentActivityUnits += CALCULATOR.getLookUpCount()*LOOKUP;

                    delivery.deliver(deliveryItem);
                    deliveryItem = null;
                    if(deliveryCounter > 2){  // Implies a simulation bug
                    	throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if(tube == null){
                    	changeState(RobotState.RETURNING);
                    	currentActivityUnits += (current_floor-Building.MAILROOM_LOCATION)*MOVEMENT;
                        totalBillableActivityUnits += currentActivityUnits;
                        currentActivityUnits = 0;
                    }
                    else{
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube;
                        tube = null;
                        setDestination();
                        changeState(RobotState.DELIVERING);
                    }

                } else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(destination_floor);
	                currentActivityUnits += MOVEMENT;
    			}
                break;
    	}
    }

    public double getTotalBillableActivity(){
        return totalBillableActivityUnits;
    }


    /**
     * Sets the route for the robot
     */
    private void setDestination() {
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    private void moveTowards(int destination) {
        if(current_floor < destination){
            current_floor++;
        } else {
            current_floor--;
        }
    }

    private String getIdTube() {
    	return String.format("%s(%1d)", this.ID, (tube == null ? 0 : 1));
    }

    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString(false));
    	}
    }

	public MailItem getTube() {
		return tube;
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
		assert(deliveryItem == null);
		deliveryItem = mailItem;
		if (deliveryItem.WEIGHT > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube == null);
		tube = mailItem;
		if (tube.WEIGHT > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}




}
