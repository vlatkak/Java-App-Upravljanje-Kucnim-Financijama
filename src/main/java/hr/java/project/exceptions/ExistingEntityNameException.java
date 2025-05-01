package hr.java.project.exceptions;

public class ExistingEntityNameException extends Exception{

    public ExistingEntityNameException() {
    }

    public ExistingEntityNameException(String message) {
        super(message);
    }

    public ExistingEntityNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
