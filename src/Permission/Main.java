package Permission;

public class Main {

    public static void main(String[] args) {

        // Create the messenger helper class and the shop
        Messenger messenger = new Messenger();
        Shop shop = new Shop();

        // Create 25 households
        for (int i = 1; i <= 25; i++) {
            Household household = new Household(i, messenger, shop);
            messenger.addHousehold(household);
        }

        // Start each household as its own thread (afterwards, otherwise they start before all the rest are generated)
        for (Household household : messenger.getHouseholds()) {
            household.start();
        }

    }

}
