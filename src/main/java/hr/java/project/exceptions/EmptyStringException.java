package hr.java.project.exceptions;

public class EmptyStringException extends Exception{
    public EmptyStringException() {}

    public EmptyStringException(String message) {
        super(message);
    }

    public EmptyStringException(String message, Throwable cause) {
        super(message, cause);
    }
}
