package automail;

import simulation.IMailDelivery;

public class Automail {
	      
    public Robot[] robots;
    public MailPool mailPool;
    
    public Automail(MailPool mailPool, IMailDelivery delivery, int numRobots) throws Exception {
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool, i);
    }

    public Robot getRobot(String robotID){
        Robot robot = null;
        for (Robot value : robots) {
            if (value.ID.equals(robotID)) {
                robot = value;
            }
        }
        return robot;
    }


}
