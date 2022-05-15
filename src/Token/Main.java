package Token;

import java.util.Random;

public class Main {

    public static void main(String[] args) {

        // Create the messenger helper class and the shop
        Messenger messenger = new Messenger();
        Shop shop = new Shop();
        Random randomizer = new Random();

        // Give the token randomly to one of the starting households
        int getsTokenFirst = randomizer.nextInt(0, 25);
        System.out.println("Household " + getsTokenFirst + " gets the token to start with.");

        // Create 25 households
        for (int i = 0; i < 25; i++) {
            boolean token;
            if (i == getsTokenFirst) {
                token = true;
            } else {
                token = false;
            }
            Household household = new Household(token, i, messenger, shop);
            messenger.addHousehold(household);
        }

        // Start each household as its own thread (afterwards, otherwise they start before all the rest are generated)
        for (Household household : messenger.getHouseholds()) {
            household.start();
        }

    }

}
