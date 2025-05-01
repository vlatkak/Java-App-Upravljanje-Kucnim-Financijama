package hr.java.project.utils;

import hr.java.project.BudgetUpdateController;
import hr.java.project.LoginStartApplication;
import hr.java.project.entities.*;
import hr.java.project.enumerations.*;
import hr.java.project.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    private static final String DATABASE_FILE = "conf/database.properties";

    private static synchronized Connection connectToDatabase() throws SQLException, IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(DATABASE_FILE));
        String databaseUrl = properties.getProperty("databaseUrl");
        String userName = properties.getProperty("username");
        String password = properties.getProperty("password");
        Connection connection = DriverManager.getConnection(databaseUrl,
                userName,password);
        logger.info("Povezano s bazom podataka");
        return connection;
    }

    /* USER METODE ------------------------------------------------------------------------------------------------*/

    public static void saveUserToDatabase(User user){
        try(Connection connection = connectToDatabase()){
            String insertQuery = "INSERT INTO USERS(NAME, USERNAME, EMAIL, ROLE) VALUES(?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery);
            pstmt.setString(1, user.name());
            pstmt.setString(2, user.userName());
            pstmt.setString(3, user.email());
            pstmt.setString(4, user.role().toString());
            pstmt.execute();
            logger.info("Pohranjen novi korisnički račun u bazu podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static Optional<User> getCurrentUser(String username){
        Optional<User> currentUser = Optional.empty();
        try(Connection connection = connectToDatabase()){
            String selectQuery = "SELECT * FROM USERS WHERE 1=1 AND USERNAME = ?";
            PreparedStatement pstmt = connection.prepareStatement(selectQuery);
            pstmt.setString(1, username);
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            currentUser = Optional.of(mapUserToResultSet(rs));
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        logger.info("Vraćen trenutno ulogirani korisnik iz baze podataka");
        return currentUser;
    }

    public static Optional<User> getUserById(Integer id){
        Optional<User> user = Optional.empty();
        try(Connection connection = connectToDatabase()){
            String selectQuery = "SELECT * FROM USERS WHERE 1=1 AND ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(selectQuery);
            pstmt.setInt(1, id);
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            user = Optional.of(mapUserToResultSet(rs));
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        logger.info("Vraćen korisnik s traženim ID-em iz baze podataka");
        return user;
    }

    public static User mapUserToResultSet(ResultSet rs) throws SQLException{
        rs.next();
        Integer id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String username = rs.getString("USERNAME");
        String email = rs.getString("EMAIL");
        String role = rs.getString("ROLE");

        User user = new User(id, name, email, username, UserRoles.valueOf(role));
        return user;
    }

    /* ACTUAL CASHFLOW METODE ---------------------------------------------------------------------------------------*/

    public static void saveActualCashflowObjectToDatabase(ActualCashflowObject actualCashflowObject){
        try(Connection connection = connectToDatabase()){
            String insertQuery = "INSERT INTO ACTUAL_CASHFLOW(CATEGORY_NAME, AMMOUNT, DATE_OF, USER_ID) " +
                    "VALUES(?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery);
            pstmt.setString(1, actualCashflowObject.getCategoryName());
            pstmt.setBigDecimal(2, actualCashflowObject.getAmount());
            pstmt.setDate(3, Date.valueOf(actualCashflowObject.getDateOfTransaction()));
            pstmt.setInt(4, LoginStartApplication.currentUser.get().id());
            pstmt.execute();
            logger.info("Pohranjen prihod ili rashod u bazu podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static List<ActualCashflowObject> getActualCashflowObjectList(){
        List<ActualCashflowObject> actualCashflowObjectList = new ArrayList<>();

        try(Connection connection = connectToDatabase()){
            String sqlQuery = "SELECT * FROM ACTUAL_CASHFLOW";
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            actualCashflowObjectList = mapActualCashflowObjectsToList(rs);

        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        logger.info("Vraćen prihod ili rashod iz baze podataka");
        return actualCashflowObjectList;
    }

    public static BigDecimal getProgressToExpectedCashflow(ExpectedCashflowObject expectedCashflow){
        List<ActualCashflowObject> actualCashflowObjectList = getActualCashflowObjectList();
        BigDecimal finalSum = new BigDecimal(0);

        List<ActualCashflowObject> actualCashflowObjectListByCategory = actualCashflowObjectList.stream()
                .filter(o -> o.getCategoryName().compareTo(expectedCashflow.getCategoryName())==0)
                .collect(Collectors.toList());

        if(expectedCashflow.getTimePeriod().equals(TimePeriodOptions.MONTHLY.getString())){
            YearMonth currentYearMonth = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth());
            finalSum = actualCashflowObjectListByCategory.stream()
                    .filter(o -> YearMonth.from(o.getDateOfTransaction()).equals(currentYearMonth))
                    .map(FinanceObjects::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else if (expectedCashflow.getTimePeriod().equals(TimePeriodOptions.ANNUAL.getString())) {
            Year currentYear = Year.now();
            finalSum = actualCashflowObjectListByCategory.stream()
                    .filter(o -> Year.from(o.getDateOfTransaction()).equals(currentYear))
                    .map(FinanceObjects::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else if (expectedCashflow.getTimePeriod().equals(TimePeriodOptions.BIANNUAL.getString())) {
            LocalDate currentDate = LocalDate.now();
            LocalDate lowerLimit;
            LocalDate upperLimit;
            if(currentDate.isAfter(LocalDate.of(currentDate.getYear(), 7, 1))
            && currentDate.isBefore(LocalDate.of(currentDate.getYear(), 12, 31))){
                lowerLimit=LocalDate.of(currentDate.getYear(), 7, 1);
                upperLimit=LocalDate.of(currentDate.getYear(), 12, 31);
            } else {
                lowerLimit = LocalDate.of(currentDate.getYear(), 1, 1);
                upperLimit = LocalDate.of(currentDate.getYear(), 6, 30);
            }

            finalSum = actualCashflowObjectListByCategory.stream()
                    .filter(o -> o.getDateOfTransaction().isBefore(upperLimit) && o.getDateOfTransaction().isAfter(lowerLimit))
                    .map(FinanceObjects::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else if (expectedCashflow.getTimePeriod().equals(TimePeriodOptions.QUARTERLY.getString())){
            LocalDate currentDate = LocalDate.now();
            LocalDate lowerLimit;
            LocalDate upperLimit;
            if(currentDate.isAfter(LocalDate.of(currentDate.getYear(), 1, 1))
                    && currentDate.isBefore(LocalDate.of(currentDate.getYear(), 3, 31))){
                lowerLimit=LocalDate.of(currentDate.getYear(), 1, 1);
                upperLimit=LocalDate.of(currentDate.getYear(), 3, 31);
            } else if (currentDate.isAfter(LocalDate.of(currentDate.getYear(), 4, 1))
                    && currentDate.isBefore(LocalDate.of(currentDate.getYear(), 6, 30))) {
                lowerLimit=LocalDate.of(currentDate.getYear(), 4, 1);
                upperLimit=LocalDate.of(currentDate.getYear(), 6, 30);
            } else if (currentDate.isAfter(LocalDate.of(currentDate.getYear(), 7, 1))
                    && currentDate.isBefore(LocalDate.of(currentDate.getYear(), 9, 30))) {
                lowerLimit=LocalDate.of(currentDate.getYear(), 7, 1);
                upperLimit=LocalDate.of(currentDate.getYear(), 9, 30);
            } else {
                lowerLimit = LocalDate.of(currentDate.getYear(), 10, 1);
                upperLimit = LocalDate.of(currentDate.getYear(), 12, 31);
            }

            finalSum = actualCashflowObjectListByCategory.stream()
                    .filter(o -> o.getDateOfTransaction().isBefore(upperLimit) && o.getDateOfTransaction().isAfter(lowerLimit))
                    .map(FinanceObjects::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        logger.info("Dobivena suma dosad ostvarenih iznosa u zadanoj kategoriji");
        return finalSum;
    }

    public static List<ActualCashflowObject> mapActualCashflowObjectsToList(ResultSet rs) throws SQLException{
        List<ActualCashflowObject> actualCashflowObjectList = new ArrayList<>();
        while (rs.next()){
            String categoryName = rs.getString("CATEGORY_NAME");
            BigDecimal amount = rs.getBigDecimal("AMMOUNT");
            LocalDate date = rs.getDate("DATE_OF").toLocalDate();
            Integer userId = rs.getInt("USER_ID");
            User user = getUserById(userId).get();

            ActualCashflowObject actualCashflowObject = new ActualCashflowObject.ActualCashflowObjectBuilder()
                    .setCategoryName(categoryName)
                    .setAmount(amount)
                    .setDateOfTransaction(date)
                    .build();
            actualCashflowObject.setUser(user);
            actualCashflowObjectList.add(actualCashflowObject);
        }
        return actualCashflowObjectList;
    }

    /* EXPECTED CASHFLOW METODE -------------------------------------------------------------------------------------*/

    public static void saveExpectedCashflowToDatabase(ExpectedCashflowObject expectedCashflowObject,
                                                      ExpectedCashflowTypes type){
        try(Connection connection = connectToDatabase()){
            String insertQuery = "INSERT INTO " +
                    "EXPECTED_CASHFLOW(CATEGORY_NAME, AMOUNT, TIME_PERIOD, TYPE, USER_ID) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery);
            pstmt.setString(1, expectedCashflowObject.getCategoryName());
            pstmt.setBigDecimal(2, expectedCashflowObject.getAmount());
            pstmt.setString(3, expectedCashflowObject.getTimePeriod());
            if (type.equals(ExpectedCashflowTypes.budget)) {
                pstmt.setString(4, ExpectedCashflowTypes.budget.toString());
            }
            else{
                pstmt.setString(4, ExpectedCashflowTypes.expected_profit.toString());
            }
            pstmt.setInt(5, LoginStartApplication.currentUser.get().id());

            pstmt.execute();
            logger.info("Pohranjen budžet ili očekivani prihod u bazu podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static void updateExpectedCashflowDatabase(ExpectedCashflowObject expectedCashflowObject,
                                                      String categoryToUpdate){
        try(Connection connection = connectToDatabase()){
            String updateQuery = "UPDATE EXPECTED_CASHFLOW SET";
            Map<Integer, Object> queryParams = new HashMap<>();
            Integer paramNumber = 0;

            if(Optional.ofNullable(expectedCashflowObject.getCategoryName()).isPresent() &&
                    !expectedCashflowObject.getCategoryName().isEmpty()){
                updateQuery = updateQuery + " CATEGORY_NAME=?";
                paramNumber++;
                queryParams.put(paramNumber, expectedCashflowObject.getCategoryName());
            }

            if(Optional.ofNullable(expectedCashflowObject.getAmount()).isPresent()){
                if(paramNumber>0){
                    updateQuery=updateQuery+",";
                }
                updateQuery = updateQuery + " AMOUNT=?";
                paramNumber++;
                queryParams.put(paramNumber, expectedCashflowObject.getAmount());
            }

            if(Optional.ofNullable(expectedCashflowObject.getTimePeriod()).isPresent()){
                if(paramNumber>0){
                    updateQuery=updateQuery+",";
                }
                updateQuery = updateQuery + " TIME_PERIOD=?";
                paramNumber++;
                queryParams.put(paramNumber, expectedCashflowObject.getTimePeriod());
            }

            if(paramNumber>0){
                updateQuery = updateQuery + " WHERE CATEGORY_NAME=?";
                paramNumber++;
                queryParams.put(paramNumber, categoryToUpdate);

                PreparedStatement pstmt = connection.prepareStatement(updateQuery);

                for(Integer n : queryParams.keySet()){
                    if(queryParams.get(n) instanceof String stringParam){
                        pstmt.setString(n, stringParam);
                    }
                    else if (queryParams.get(n) instanceof BigDecimal bigDecimalParam) {
                        pstmt.setBigDecimal(n, bigDecimalParam);
                    }
                }

                pstmt.execute();
                logger.info("Ažuriran podatak o budžetu ili očekivanom prihodu u bazi podataka");
            }

        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static void deleteExpectedCashflowFromDatabase(String category){
        try(Connection connection = connectToDatabase()){
            String deleteQuery = "DELETE FROM EXPECTED_CASHFLOW WHERE CATEGORY_NAME = ?;";
            PreparedStatement pstmt = connection.prepareStatement(deleteQuery);
            pstmt.setString(1, category);
            pstmt.execute();
            logger.info("Izbrisan budžet ili očekivani prihod iz baze podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static List<ExpectedCashflowObject> getExpectedCashflowFromDatabase(ExpectedCashflowTypes type){
        List<ExpectedCashflowObject> expectedCashflowList = new ArrayList<>();
        try(Connection connection = connectToDatabase()){
            String sqlQuery = "SELECT * FROM EXPECTED_CASHFLOW WHERE TYPE=?";
            PreparedStatement pstmt = connection.prepareStatement(sqlQuery);
            if(type.equals(ExpectedCashflowTypes.budget)){
                pstmt.setString(1, ExpectedCashflowTypes.budget.toString());
            }
            else{
                pstmt.setString(1, ExpectedCashflowTypes.expected_profit.toString());
            }
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();

            expectedCashflowList = mapExpectedCashflowToList(rs);
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        logger.info("Vraćena lista budžeta ili očekivanih prihoda iz baze podataka");
        return expectedCashflowList;
    }

    public static Boolean checkIfExpectedCashflowCategoryNameExists(String categoryName){
        Boolean categoryNameExists = false;
        try(Connection connection = connectToDatabase()){
            String searchQuery = "SELECT * FROM EXPECTED_CASHFLOW WHERE 1=1 AND CATEGORY_NAME=?";
            PreparedStatement pstmt = connection.prepareStatement(searchQuery);
            pstmt.setString(1, categoryName);
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            if (rs.isBeforeFirst()){
                categoryNameExists=true;
            }
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        return categoryNameExists;
    }

    public static List<ExpectedCashflowObject> mapExpectedCashflowToList(ResultSet rs) throws SQLException{
        List<ExpectedCashflowObject> expectedCashflowList = new ArrayList<>();
        while(rs.next()){
            String categoryName = rs.getString("CATEGORY_NAME");
            BigDecimal ammount = rs.getBigDecimal("AMOUNT");
            String timePeriod = rs.getString("TIME_PERIOD");

            ExpectedCashflowObject expectedCashflowObject = new ExpectedCashflowObject.ExpectedCashflowBuilder()
                    .setCategoryName(categoryName)
                    .setAmount(ammount)
                    .setTimePeriod(timePeriod)
                    .build();
            expectedCashflowList.add(expectedCashflowObject);
        }
        return expectedCashflowList;
    }

    /* CASHFLOW OVERVIEW METODE -------------------------------------------------------------------------------------*/

    public static List<ActualCashflowObject>
    actualCashflowFilter(List<ActualCashflowObject> cashflowList, Year year, String monthNumber, String type){
        List<ActualCashflowObject> filteredCashflowList = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth());
        if(monthNumber.equals(NumberOfMonthsToFilterBy.WHOLE_YEAR.getString())){
            if(Optional.ofNullable(year).isPresent()){
                filteredCashflowList = cashflowList.stream()
                        .filter(o -> Year.from(o.getDateOfTransaction()).equals(year))
                        .collect(Collectors.toList());
            }
        }
        else if (monthNumber.equals(NumberOfMonthsToFilterBy.THIS_MONTH.getString())){
            filteredCashflowList = cashflowList.stream()
                    .filter(o -> YearMonth.from(o.getDateOfTransaction()).equals(currentYearMonth))
                    .collect(Collectors.toList());
        }
        else if (monthNumber.equals(NumberOfMonthsToFilterBy.SIX_MONTHS.getString())) {
            LocalDate upperLimit = LocalDate.now();
            YearMonth sixMonthsAgo = currentYearMonth.minusMonths(6);
            LocalDate lowerLimt = sixMonthsAgo.atDay(upperLimit.getDayOfMonth());
            filteredCashflowList = cashflowList.stream()
                    .filter(o -> o.getDateOfTransaction().isAfter(lowerLimt) && o.getDateOfTransaction().isBefore(upperLimit))
                    .collect(Collectors.toList());
        }

        if(type.equals(CashflowTypeFilterOptions.PROFITS.getString())){
            filteredCashflowList = filteredCashflowList.stream()
                    .filter(o -> o.getAmount().compareTo(BigDecimal.ZERO)>0)
                    .collect(Collectors.toList());
        }
        else if(type.equals(CashflowTypeFilterOptions.EXPENSES.getString())){
            filteredCashflowList = filteredCashflowList.stream()
                    .filter(o -> o.getAmount().compareTo(BigDecimal.ZERO)<0)
                    .collect(Collectors.toList());
        }
        logger.info("Dobiven popis dosad ostvarenih prihoda i rashoda");
        return filteredCashflowList;
    }

    /* REMINDER METODE ----------------------------------------------------------------------------------------------*/

    public static void saveReminderToDatabase(ReminderEvent<FinanceObjects<BigDecimal>, Integer> event){
        try(Connection connection = connectToDatabase()){
            String insertQuery = "INSERT INTO REMINDERS(TITLE, AMOUNT, ON_DATES, DURATION, USER_ID) " +
                    "VALUES(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery);
            pstmt.setString(1, event.getEventObject().getCategoryName());
            pstmt.setBigDecimal(2, event.getEventObject().getAmount());
            if(event.getEventObject() instanceof ActualCashflowObject ac){
                pstmt.setString(3, ac.getDateOfTransaction().toString());
            }
            else if (event.getEventObject() instanceof ExpectedCashflowObject ec) {
                pstmt.setString(3, ec.getTimePeriod());
            }
            pstmt.setInt(4, event.getDaysBeforeReminder());
            pstmt.setInt(5, LoginStartApplication.currentUser.get().id());

            pstmt.execute();
            logger.info("Spremljen novi podsjetnik u bazu podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static void updateReminderDatabase(ReminderEvent<FinanceObjects<BigDecimal>, Integer> event,
                                              String reminderTitle){
        try(Connection connection = connectToDatabase()){
            String updateQuery = "UPDATE REMINDERS SET";
            Map<Integer, Object> queryParams = new HashMap<>();
            Integer paramNumber = 0;

            if(Optional.ofNullable(event.getEventObject().getCategoryName()).isPresent() &&
                    !event.getEventObject().getCategoryName().isEmpty()){
                updateQuery = updateQuery + " TITLE=?";
                paramNumber++;
                queryParams.put(paramNumber, event.getEventObject().getCategoryName());
            }

            if(Optional.ofNullable(event.getEventObject().getAmount()).isPresent()){
                if (paramNumber>0){
                    updateQuery = updateQuery + ",";
                }
                updateQuery = updateQuery + " AMOUNT=?";
                paramNumber++;
                queryParams.put(paramNumber, event.getEventObject().getAmount());
            }

            if(event.getEventObject() instanceof ActualCashflowObject ac){
                if(Optional.ofNullable(ac.getDateOfTransaction()).isPresent()){
                    if (paramNumber>0){
                        updateQuery = updateQuery + ",";
                    }
                    updateQuery = updateQuery + " ON_DATES=?";
                    paramNumber++;
                    queryParams.put(paramNumber, ac.getDateOfTransaction().toString());
                }
            }
            else if (event.getEventObject() instanceof ExpectedCashflowObject ec){
                if(Optional.ofNullable(ec.getTimePeriod()).isPresent() &&
                        !ec.getTimePeriod().isEmpty()){
                    if (paramNumber>0){
                        updateQuery = updateQuery + ",";
                    }
                    updateQuery = updateQuery + " ON_DATES=?";
                    paramNumber++;
                    queryParams.put(paramNumber, ec.getTimePeriod());
                }
            }

            if(Optional.ofNullable(event.getDaysBeforeReminder()).isPresent()){
                if (paramNumber>0){
                    updateQuery = updateQuery + ",";
                }
                updateQuery = updateQuery + " DURATION=?";
                paramNumber++;
                queryParams.put(paramNumber, event.getDaysBeforeReminder());
            }

            if(paramNumber>0){

                updateQuery = updateQuery + " WHERE TITLE=?";
                paramNumber++;
                queryParams.put(paramNumber, reminderTitle);

                PreparedStatement pstmt = connection.prepareStatement(updateQuery);

                for(Integer n : queryParams.keySet()){
                    if(queryParams.get(n) instanceof String stringParam){
                        pstmt.setString(n, stringParam);
                    }
                    else if (queryParams.get(n) instanceof BigDecimal bigDecimalParam) {
                        pstmt.setBigDecimal(n, bigDecimalParam);
                    }
                    else if (queryParams.get(n) instanceof Integer intParam) {
                        pstmt.setInt(n, intParam);
                    }
                }

                pstmt.execute();
                logger.info("Ažuriran podsjetnik u bazi podataka");
            }

        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static Set<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> getReminderSetFromDatabase(){
        Set<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> reminderSet = new HashSet<>();
        try(Connection connection = connectToDatabase()){
            String searchQuery = "SELECT * FROM REMINDERS WHERE 1=1 AND USER_ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(searchQuery);
            pstmt.setInt(1, LoginStartApplication.currentUser.get().id());
            pstmt.execute();

            ResultSet rs = pstmt.getResultSet();

            mapReminderResultsetToSet(reminderSet, rs);

        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        logger.info("Vraćen set podsjetnika iz baze podataka");
        return reminderSet;
    }

    public static void deleteReminderFromDatabase(String reminderTitle){
        try(Connection connection = connectToDatabase()){
            String deleteQuery = "DELETE FROM REMINDERS WHERE TITLE = ?;";
            PreparedStatement pstmt = connection.prepareStatement(deleteQuery);
            pstmt.setString(1, reminderTitle);
            pstmt.execute();
            logger.info("Izbrisan podsjetnik iz baze podataka");
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
    }

    public static void
    mapReminderResultsetToSet(Set<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> reminderSet, ResultSet rs)
            throws SQLException {
        while (rs.next()){
            String title = rs.getString("TITLE");
            BigDecimal amount = rs.getBigDecimal("AMOUNT");
            Integer duration = rs.getInt("DURATION");
            String timePeriodString = rs.getString("ON_DATES");

            Pattern datePattern = Pattern.compile("[0-9]{4}+-+[0-9]{2}+-+[0-9]{2}");
            Matcher matcher = datePattern.matcher(timePeriodString);
            if(matcher.matches()){
                LocalDate date = LocalDate.parse(timePeriodString);
                ActualCashflowObject actualCashflowObject = new ActualCashflowObject.ActualCashflowObjectBuilder()
                        .setCategoryName(title)
                        .setAmount(amount)
                        .setDateOfTransaction(date)
                        .build();
                ReminderEvent<FinanceObjects<BigDecimal>, Integer> event =
                        new ReminderEvent<>(actualCashflowObject, duration);
                reminderSet.add(event);
            }
            else {
                ExpectedCashflowObject expectedCashflowObject =
                        new ExpectedCashflowObject.ExpectedCashflowBuilder()
                                .setCategoryName(title)
                                .setAmount(amount)
                                .setTimePeriod(timePeriodString)
                                .build();
                ReminderEvent<FinanceObjects<BigDecimal>, Integer> event =
                        new ReminderEvent<>(expectedCashflowObject, duration);
                reminderSet.add(event);
            }
        }
    }

    public static Boolean checkIfReminderTitleExists(String title){
        Boolean reminderNameExists = false;
        try(Connection connection = connectToDatabase()){
            String searchQuery = "SELECT * FROM REMINDERS WHERE 1=1 AND TITLE=?";
            PreparedStatement pstmt = connection.prepareStatement(searchQuery);
            pstmt.setString(1, title);
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            if (rs.isBeforeFirst()){
                reminderNameExists=true;
            }
        }catch (SQLException | IOException e){
            logger.error("Pogreška u povezivanju s bazom podataka");
        }
        return reminderNameExists;
    }

    public static Boolean checkIfReminderNeedsShowing(ReminderEvent<FinanceObjects<BigDecimal>, Integer> event){
        Boolean reminderNeedsShowing = false;
        LocalDate currentDate = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(currentDate);
        if(event.getEventObject() instanceof ActualCashflowObject ac){
            if (ChronoUnit.DAYS.between(currentDate, ac.getDateOfTransaction())<=event.getDaysBeforeReminder()
                    && ChronoUnit.DAYS.between(currentDate, ac.getDateOfTransaction())>=0){
                reminderNeedsShowing = true;
            }
        }
        else if (event.getEventObject() instanceof ExpectedCashflowObject ec){
            if(ec.getTimePeriod().equals(TimePeriodOptions.MONTHLY.getString())){
                LocalDate endOfMonth = currentYearMonth.atEndOfMonth();
                if(ChronoUnit.DAYS.between(currentDate, endOfMonth)<=event.getDaysBeforeReminder()
                    && ChronoUnit.DAYS.between(currentDate, endOfMonth)>=0){
                    reminderNeedsShowing = true;
                }
            }
            else if (ec.getTimePeriod().equals(TimePeriodOptions.BIANNUAL.getString())){
                LocalDate firstShowing = LocalDate.of(currentYearMonth.getYear(), 6, 30);
                LocalDate secondShowing = LocalDate.of(currentYearMonth.getYear(), 12, 31);
                if((ChronoUnit.DAYS.between(currentDate, firstShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, firstShowing)>=0)
                        || (ChronoUnit.DAYS.between(currentDate, secondShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, secondShowing)>=0)){
                    reminderNeedsShowing = true;
                }
            }
            else if (ec.getTimePeriod().equals(TimePeriodOptions.QUARTERLY.getString())){
                LocalDate firstShowing = LocalDate.of(currentYearMonth.getYear(), 3, 31);
                LocalDate secondShowing = LocalDate.of(currentYearMonth.getYear(), 6, 30);
                LocalDate thirdShowing = LocalDate.of(currentYearMonth.getYear(), 9, 30);
                LocalDate fourthShowing = LocalDate.of(currentYearMonth.getYear(), 12, 31);
                if((ChronoUnit.DAYS.between(currentDate, firstShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, firstShowing)>=0)
                        || (ChronoUnit.DAYS.between(currentDate, secondShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, secondShowing)>=0)
                        || (ChronoUnit.DAYS.between(currentDate, thirdShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, thirdShowing)>=0)
                        || (ChronoUnit.DAYS.between(currentDate, fourthShowing)<=event.getDaysBeforeReminder()
                        && ChronoUnit.DAYS.between(currentDate, fourthShowing)>=0)){
                    reminderNeedsShowing = true;
                }
            }
        }
        return reminderNeedsShowing;
    }
}
