package modules.rates;

public class RatesException extends Exception{

    private String message;

    public RatesException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
