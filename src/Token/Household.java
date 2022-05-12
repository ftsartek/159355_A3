package Token;

import java.util.ArrayList;

public class Household extends Thread {

    private Token token = null;
    private ArrayList<Integer> requested;
    private ArrayList<Integer> granted;
    private int ticketNumber;
    private boolean inCS;
    private int id;

    public Household(Token token, int id) {
        this.token = token;
        this.id = id;
        requested = new ArrayList<>();
        granted = new ArrayList<>();
        ticketNumber = 0;
        inCS = false;
    }

    @Override
    public void run() {

    }

    public void takeToken(Token token) {
        this.token = token;
    }

    public Token passToken() {
        Token tempToken = token;
        token = null;
        return tempToken;
    }

    public boolean hasToken() {
        if (this.token == null) {
            return false;
        }
        return true;
    }

    public int getID() {
        return this.id;
    }
}


class HouseholdListener extends Thread {

    private int sender;
    private int requestedNumber;

    @Override
    public void run() {

    }
}