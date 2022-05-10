package Permission;

import java.util.ArrayList;

public class Messenger {
    private ArrayList<Household> households;

    public Messenger() {
        households = new ArrayList<>();
    }

    public void addHousehold(Household household) {
        households.add(household);
    }

    public ArrayList<Household> getHouseholds() {
        return households;
    }

    public ArrayList<Integer> getHouseholdIDs() {
        ArrayList<Integer> householdIDs = new ArrayList<>();
        for (Household household : households) {
            householdIDs.add(household.getID());
        }
        return householdIDs;
    }

    public Household getHouseholdByID(int id) {
        for (Household household: households) {
            if (id == household.getID()) {
                return household;
            }
        }
        return null;
    }

    public void forwardMessage(Message message) {
        Household receiver = getHouseholdByID(message.getDestinationID());
        if (receiver != null) {
            receiver.receiveQueue.add(message);
        }
    }
}
