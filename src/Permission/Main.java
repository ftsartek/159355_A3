package Permission;

public class Main {

    public static void main(String[] args) {

        Messenger messenger = new Messenger();

        for (int i = 1; i <= 25; i++) {
            Household household = new Household(i, messenger);
            messenger.addHousehold(household);
        }


        for (Household household : messenger.getHouseholds()) {
            household.start();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }

    }

}
