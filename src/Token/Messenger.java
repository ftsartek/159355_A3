package Token;

import java.util.ArrayList;

public class Messenger {

    // Array used to hold the list of households
    private ArrayList<Household> households;

    private Household tokenHolder;

    // Constructor
    public Messenger() {
        households = new ArrayList<>();
    }

    // Adds a household to the messenger list
    public void addHousehold(Household household) {
        if (household.hasToken()) {
            tokenHolder = household;
        }
        households.add(household);
    }

    // Removes a household from the messenger list
    public void removeHousehold(Household household) {
        households.remove(household);
    }

    // Returns the array of households
    public ArrayList<Household> getHouseholds() {
        return households;
    }

    // Returns an array of household IDs
    public ArrayList<Integer> getHouseholdIDs() {
        ArrayList<Integer> householdIDs = new ArrayList<>();
        for (Household household : households) {
            householdIDs.add(household.getID());
        }
        return householdIDs;
    }

    // Returns a household, given the household's ID.
    public Household getHouseholdByID(int id) {
        for (Household household: households) {
            if (id == household.getID()) {
                return household;
            }
        }
        return null;
    }

    // Forwards a message to the destination contained in the message
    public void forwardMessage(Message message) {
        Household receiver = getHouseholdByID(message.getDestinationID());
        if (receiver != null) {
            receiver.receiveQueue.add(message);
        }
    }

    public void passToken(Token token, int destination) {

    }
}
