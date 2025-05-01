package hr.java.project.utils;

import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.FinanceObjects;
import hr.java.project.entities.ReminderEvent;
import hr.java.project.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /*LISTS TO SERIALIZE --------------------------------------------------------------------------------------------*/
    public static List<ActualCashflowObject> actualCashflowObjects = new ArrayList<>();
    public static List<ExpectedCashflowObject> expectedCashflowObjects = new ArrayList<>();
    public static List<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> remiderEvents = new ArrayList<>();

    /*USER FILE UTILS -----------------------------------------------------------------------------------------------*/

    private static final String USER_FILE = "dataFiles/users.txt";

    public static void saveNewUser(String userName, String password){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))){

            writer.write(userName);

            writer.newLine();
            int hashedPassword = password.hashCode();
            writer.write(String.valueOf(hashedPassword));
            writer.append('\n');

        }catch (IOException e){
            logger.error("Pogreška u spremanju u datoteku");
        }
        logger.info("Spremljeni podaci korisnika u datoteku");
    }

    public static Boolean checkIfUserNameExists(String name){
        Boolean result=false;
        try(BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))){
            Optional<String> line = Optional.empty();
            while((line = Optional.ofNullable(reader.readLine())).isPresent()){
                if((line.get()).compareTo(name)==0){
                    result=true;
                    break;
                }
                reader.readLine();
            }
        }catch (IOException e){
            logger.error("Pogreška u čitanju datoteke");
        }
        return result;
    }

    public static Optional<User> loginInfoSearch(String userName, String password){
        Optional<User> currentUser = Optional.empty();
        String hashedPassword = String.valueOf(password.hashCode());

        try(BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))){
            Optional<String> line = Optional.empty();
            while((line = Optional.ofNullable(reader.readLine())).isPresent()){
                if((line.get()).compareTo(userName)==0){
                    System.out.println(line.get());
                    line = Optional.ofNullable(reader.readLine());
                    if((line.get()).compareTo(hashedPassword)==0){
                        System.out.println(line.get());
                        currentUser = DatabaseUtils.getCurrentUser(userName);
                        break;
                    }
                }
                else{
                    reader.readLine();
                }
            }
        }catch (IOException e){
            logger.error("Pogreška u čitanju datoteke");
        }
        logger.info("Pronađeni podaci korisnika koji se pokušava ulogirat iz datoteke");
        return currentUser;
    }

}
