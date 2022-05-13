package Token;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Shop {

    // Used to track which household(s) are in the shop at a given time. Should never be more than 1!
    CopyOnWriteArrayList<Household> occupants;
    Random rng;

    // Initialise the shop
    public Shop() {
        occupants = new CopyOnWriteArrayList<>();
        rng = new Random();
    }

    // Start shopping, alert if there's already a household in the shop, wait a random amount of time and then pay and leave.
    public void doShop(Household household) throws InterruptedException {
        if (!household.hasToken()) {
            System.out.println("Denying household " + household.getID() + " access to the store, they don't have the token!");
        } else {
            // Household enters and is added to the array
            occupants.add(household);
            System.out.println("");
            System.out.println("Household " + household.getID() + " is entering the shop");
            // Alert if there's already someone in the shop
            if (occupants.size() > 1) {
                System.out.println("There are multiple households in the shop at once! People are going to get sick...");
            }
            // Wait a little bit...
            Thread.sleep(rng.nextInt(50, 250));
            // Household leaves, their shop count is iterated and they're removed.
            System.out.println("Household " + household.getID() + " has spent $" + rng.nextInt(50, 250) + "." + rng.nextInt(0, 10) + rng.nextInt(0, 10) + " and is leaving the shop.");
            Thread.sleep(100);
            household.completedShop();
            occupants.remove(household);
        }
    }


}
