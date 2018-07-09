package info.wheelly.portfolio.exceptions;

public class APIException extends Exception {
    public APIException() {
        super();
    }

    public APIException(String s) {
        super(s);
    }

    public APIException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public APIException(Throwable throwable) {
        super(throwable);
    }

    protected APIException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
