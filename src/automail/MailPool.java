package automail;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;
import exceptions.ItemTooHeavyException;
import simulation.Building;


/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */

public class MailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}

	//use charge threshold here
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			double i1_charge = i1.mailItem.getExpectedCharge();
			double i2_charge = i2.mailItem.getExpectedCharge();
			if (chargeThreshold == 0) {
				if (i1.destination < i2.destination) {
					order = 1;
				} else if (i1.destination > i2.destination) {
					order = -1;
				}
				return order;
			}
			else{
				if (i1_charge >= chargeThreshold && i2_charge< chargeThreshold) {
					order = -1;
				} else if (i1_charge < chargeThreshold && i2_charge>=chargeThreshold) {
					order = 1;
				}
				else if(i1_charge >= chargeThreshold && i2_charge >= chargeThreshold) {
					if(i1_charge > i2_charge){
						order = -1;
					}
					else if(i1_charge < i2_charge){
						order = -1;
					}
				}
				else{
					if (i1.destination < i2.destination) {
						order = 1;
					} else if (i1.destination > i2.destination) {
						order = -1;
					}
				}

			}
			return order;
		}
	}

	
	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;
	static public final int MOVEMENT = 5;

	private Calculator calculator = new Calculator();
	private double serviceFee;
	private double markupPercentage;
	private double activityUnitPrice;
	private double chargeThreshold;

	public MailPool(int nrobots, double markupPercentage, double activityUnitPrice, double chargeThreshold) throws Exception {
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
		this.serviceFee = 0;
		this.markupPercentage = markupPercentage;
		this.activityUnitPrice = activityUnitPrice;
		this.chargeThreshold = chargeThreshold;
	}

	/**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		int activityUnits = (item.destination - Building.MAILROOM_LOCATION)*MOVEMENT;
		int destFloor = item.destination;
		pool.add(item);
		calculator.calculateCharge(item.mailItem,destFloor,markupPercentage,activityUnitPrice,
				activityUnits,false);
		pool.sort(new ItemComparator());
	}
	
	
	
	/**
     * load up any waiting robots with mailItems, if any.
     */
	public void loadItemsToRobot() throws ItemTooHeavyException {
		//List available robots
		ListIterator<Robot> i = robots.listIterator();
		while (i.hasNext()) loadItem(i);
	}
	
	//load items to the robot . associate item with bot ID
	private void loadItem(ListIterator<Robot> i) throws ItemTooHeavyException {
		Robot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();
		if (pool.size() > 0) {
			try {
				Item item = j.next();
				robot.addToHand(item.mailItem);// hand first as we want higher priority delivered first
				item.mailItem.setDeliveringRobotId(robot.id);
				j.remove();
			if (pool.size() > 0) {
				robot.addToTube(j.next().mailItem);
				item.mailItem.setDeliveringRobotId(robot.id);
				j.remove();
			}

			robot.dispatch(); // send the robot off if it has any items to deliver
			i.remove();       // remove from mailPool queue
			} catch (Exception e) { 
	            throw e; 
	        } 
		}
	}

	/**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

	public double getMarkupPercentage() {
		return markupPercentage;
	}

	public double getActivityUnitPrice() {
		return activityUnitPrice;
	}

	/**public double calculateExpectedCharge(MailItem mailitem){
		this.serviceFee = -1;
		while(serviceFee==-1) {
			//System.out.println(mailitem.getDestFloor());
			serviceFee = wifiModem.forwardCallToAPI_LookupPrice(mailitem.getDestFloor());
		}

		//System.out.println("fee is "+ serviceFee);
		double expected_charge = ((mailitem.destination_floor - Building.MAILROOM_LOCATION)
				*activityUnitPrice + serviceFee)*(1+markupPercentage);
		return expected_charge;

	}**/

}
