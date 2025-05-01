package hr.java.project;

import hr.java.project.exceptions.EmptyStringException;
import hr.java.project.exceptions.ExistingEntityNameException;
import hr.java.project.exceptions.InvalidEmailException;
import hr.java.project.exceptions.InvalidNumberException;
import hr.java.project.utils.FileUtils;
import javafx.scene.control.DatePicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Controller{

    public default void stringEmptyCheck(String string) throws EmptyStringException {
        if(string.isEmpty() || string==null){
            throw new EmptyStringException("Pokušaj rada sa sadržajem praznog TextField objekta.");
        }
    }

    public default void checkAmmountNumberValidity(BigDecimal ammount) throws InvalidNumberException{
        if(ammount.compareTo(BigDecimal.valueOf(0))<0){
            throw new InvalidNumberException("Broj unesen kao novčani iznos je manji od nule.");
        }
    }

    public default void checkIfEntityNameExists(Boolean check) throws ExistingEntityNameException{
        if(check){
            throw new ExistingEntityNameException("Pokušaj kreiranja objekta s već postojećim imenom.");
        }
    }

    public default void checkForEmailFormat(String emailString) throws InvalidEmailException {
        Pattern mailPatter = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.+com");
        Matcher matcher = mailPatter.matcher(emailString);
        if(!matcher.matches()){
            throw new InvalidNumberException("Unos teksta u polju korisničkog emaila ne odgovara formatu emaila.");
        }
    }

}
