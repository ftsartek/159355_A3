package Token;

public class Token {

    private static Token singleton_token = null;

    private Token() {}

    public static Token getTokenInstance() {
        if (singleton_token == null) {
            singleton_token = new Token();
        }
        return singleton_token;
    }

}
