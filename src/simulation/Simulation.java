package simulation;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.MailAlreadyDeliveredException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.unimelb.swen30006.wifimodem.WifiModem;

import automail.Automail;
import automail.MailItem;
import automail.MailPool;
import automail.Price;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {
	private static int NUM_ROBOTS;
	private static double CHARGE_THRESHOLD;
	private static boolean CHARGE_DISPLAY;
	private static double MARKUP_PERCENTAGE;
	private static double ACTIVITY_UNITPRICE;
	private static double BILLABLE_ACTIVITY;

	/**
	 * Constant for the mail generator
	 */
	private static int MAIL_TO_CREATE;
	private static int MAIL_MAX_WEIGHT;

	private static ArrayList<MailItem> MAIL_DELIVERED;
	private static double total_delay = 0;
	private static WifiModem wModem = null;

	public static void main(String[] args) throws Exception {

		/** Load properties for simulation based on either default or a properties file.**/
		Properties automailProperties = setUpProperties();

		//An array list to record mails that have been delivered
		MAIL_DELIVERED = new ArrayList<>();

		/** This code section below is to save a random seed for generating mails.
		 * If a program argument is entered, the first argument will be a random seed.
		 * If not a random seed will be from a properties file.
		 * Otherwise, no a random seed. */

		/** Used to see whether a seed is initialized or not */
		HashMap<Boolean, Integer> seedMap = new HashMap<>();
		if (args.length == 0) { // No arg
			String seedProp = automailProperties.getProperty("Seed");
			if (seedProp == null) { // and no property
				seedMap.put(false, 0); // so randomise
			} else { // Use property seed
				seedMap.put(true, Integer.parseInt(seedProp));
			}
		} else { // Use arg seed - overrides property
			seedMap.put(true, Integer.parseInt(args[0]));
		}
		Integer seed = seedMap.get(true);
		System.out.println("#A Random Seed: " + (seed == null ? "null" : seed.toString()));

		// Install the modem & turn on the modem
		try {
			System.out.println("Setting up Wifi Modem");
			wModem = WifiModem.getInstance(Building.MAILROOM_LOCATION);
			System.out.println(wModem.Turnon());
		} catch (Exception mException) {
			mException.printStackTrace();
		}

		/**
		 * This code section is for running a simulation
		 */
		/* Instantiate MailPool and Automail */
		Price.getInstance(ACTIVITY_UNITPRICE,MARKUP_PERCENTAGE,CHARGE_THRESHOLD);
		MailPool mailPool = new MailPool(NUM_ROBOTS);
		Automail automail = new Automail(mailPool, new ReportDelivery(), NUM_ROBOTS);
		/**new changes**/
		MailGenerator mailGenerator = new MailGenerator(MAIL_TO_CREATE, MAIL_MAX_WEIGHT,CHARGE_DISPLAY, mailPool, seedMap);

		/** Generate all the mails */
		mailGenerator.generateAllMail();
		while (MAIL_DELIVERED.size() != mailGenerator.MAIL_TO_CREATE) {
			// System.out.printf("Delivered: %4d; Created: %4d%n", MAIL_DELIVERED.size(), mailGenerator.MAIL_TO_CREATE);
			mailGenerator.addToMailPool();
			try {
				automail.mailPool.loadItemsToRobot();
				for (int i = 0; i < NUM_ROBOTS; i++) {
					automail.robots[i].operate();
				}
			} catch (ExcessiveDeliveryException | ItemTooHeavyException e) {
				e.printStackTrace();
				System.out.println("Simulation unable to complete.");
				System.exit(0);
			}
			Clock.Tick();
		}

		for (int i = 0; i < NUM_ROBOTS; i++){
			String id = "R"+i;
			BILLABLE_ACTIVITY += automail.getRobot(id).getTotalBillableActivity();
		}
		printResults();
		System.out.println(wModem.Turnoff());
	}


	static private Properties setUpProperties() throws IOException {
		// Default properties
		Properties automailProperties = new Properties();
		automailProperties.setProperty("Robots", "Standard");
		automailProperties.setProperty("Floors", "10");
		automailProperties.setProperty("Mail_to_Create", "80");
		automailProperties.setProperty("ChargeThreshold", "0");
		automailProperties.setProperty("CommercialDisplay", "false");
		/**new changes**/
		automailProperties.setProperty("MarkupPercentage", "0.059");
		automailProperties.setProperty("ActivityUnitPrice", "0.224");

		// Read properties
		FileReader inStream = null;
		try {
			inStream = new FileReader("automail.properties");
			automailProperties.load(inStream);
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}

		// Floors
		Building.FLOORS = Integer.parseInt(automailProperties.getProperty("Floors"));
		System.out.println("#Floors: " + Building.FLOORS);
		// Mail_to_Create
		MAIL_TO_CREATE = Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
		System.out.println("#Created mails: " + MAIL_TO_CREATE);
		// Mail_to_Create
		MAIL_MAX_WEIGHT = Integer.parseInt(automailProperties.getProperty("Mail_Max_Weight"));
		System.out.println("#Maximum weight: " + MAIL_MAX_WEIGHT);
		// Last_Delivery_Time
		Clock.MAIL_RECEVING_LENGTH = Integer.parseInt(automailProperties.getProperty("Mail_Receving_Length"));
		System.out.println("#Mail receiving length: " + Clock.MAIL_RECEVING_LENGTH);
		// Robots
		NUM_ROBOTS = Integer.parseInt(automailProperties.getProperty("Robots"));
		System.out.print("#Robots: ");
		System.out.println(NUM_ROBOTS);
		assert (NUM_ROBOTS > 0);
		// Charge Threshold 
		CHARGE_THRESHOLD = Double.parseDouble(automailProperties.getProperty("ChargeThreshold"));
		System.out.println("#Charge Threshold: " + CHARGE_THRESHOLD);
		// Charge Display
		CHARGE_DISPLAY = Boolean.parseBoolean(automailProperties.getProperty("ChargeDisplay"));
		System.out.println("#Charge Display: " + CHARGE_DISPLAY);
		ACTIVITY_UNITPRICE = Double.parseDouble(automailProperties.getProperty("ActivityUnitPrice"));
		System.out.println("#ActivityUnitPrice: " + ACTIVITY_UNITPRICE);
		MARKUP_PERCENTAGE = Double.parseDouble(automailProperties.getProperty("MarkupPercentage"));
		System.out.println("#MarkupPercentage: "+MARKUP_PERCENTAGE);

		return automailProperties;
	}

	static class ReportDelivery implements IMailDelivery {

		/**
		 * Confirm the delivery and calculate the total score
		 */
		public void deliver(MailItem deliveryItem) {
			if (!MAIL_DELIVERED.contains(deliveryItem)) {
				MAIL_DELIVERED.add(deliveryItem);
				System.out.printf("T: %3d > Delivered(%4d) [%s]%n", Clock.Time(), MAIL_DELIVERED.size(), deliveryItem.toString(true));
				// Calculate delivery score
				total_delay += calculateDeliveryDelay(deliveryItem);
			} else {
				try {
					throw new MailAlreadyDeliveredException();
				} catch (MailAlreadyDeliveredException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static double calculateDeliveryDelay(MailItem deliveryItem) {
		// Penalty for longer delivery times
		final double penalty = 1.2;
		double priority_weight = 0;
		// Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
		return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(), penalty) * (1 + Math.sqrt(priority_weight));
	}

	public static void printResults() {
		System.out.println("T: " + Clock.Time() + " | Simulation complete!");
		System.out.println("Final Delivery time: " + Clock.Time());
		System.out.printf("Delay: %.2f%n", total_delay);
		if(CHARGE_DISPLAY) {
			System.out.printf("Total number of items delivered is %d%n",MAIL_DELIVERED.size());
			System.out.printf("Billable activity is %.2f%n", BILLABLE_ACTIVITY);
			System.out.printf("Total activity cost is %.2f%n", BILLABLE_ACTIVITY * ACTIVITY_UNITPRICE);
		}
	}

}
