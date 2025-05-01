package hr.java.project.exceptions;

public class InvalidNumberException extends RuntimeException{
    public InvalidNumberException() {
    }

    public InvalidNumberException(String message) {
        super(message);
    }

    public InvalidNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
