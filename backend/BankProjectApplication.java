package com.lakshya.bankproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class BankProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankProjectApplication.class, args);
    }

    @GetMapping("/getSummary")
    public String getSummary(@RequestParam String accountNumber) throws Exception {
        JSONObject respnse = new JSONObject();
        JSONArray transactionList = new JSONArray();
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT * FROM TRANSACTION WHERE Acc_no=? ORDER BY Date DESC, Hour DESC";
            ps = con.prepareStatement(query);
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                JSONObject transaction = new JSONObject();
                transaction.put("Type", rs.getString("Tranc_type"));
                transaction.put("Charge", rs.getDouble("Charge"));
                transaction.put("Amount", rs.getDouble("Amount"));
                transaction.put("Account Number", rs.getString("Acc_no"));
                transaction.put("Date", rs.getString("Date"));
                transaction.put("Hour", rs.getString("Hour"));
                transaction.put("Balance", rs.getDouble("Balance_Snap"));

                transactionList.put(transaction);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            con.close();
        }
        respnse.put("All", transactionList);
        return respnse.toString();
    }

    @GetMapping("/getPersonalBankerDetails")
    public String getPersonalBankerDetails(@RequestParam String customerId) throws Exception{
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;
        JSONObject employee = new JSONObject();

        try {
            String query = "SELECT \n" +
                    "    e.Name AS EmployeeName,\n" +
                    "    e.Telephone AS EmployeeTelephone,\n" +
                    "    m.Name AS ManagerName,\n" +
                    "    m.Telephone AS ManagerTelephone\n" +
                    "FROM \n" +
                    "    EMPLOYEE e\n" +
                    "LEFT JOIN \n" +
                    "    EMPLOYEE m ON e.Super_SSN = m.SSN\n" +
                    "WHERE \n" +
                    "    e.SSN = (SELECT Personal_banker FROM CUSTOMER WHERE SSN = ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                employee.put("EmployeeName", rs.getString("EmployeeName"));
                employee.put("EmployeeTelephone", rs.getString("EmployeeTelephone"));
                employee.put("ManagerName", rs.getString("ManagerName"));
                employee.put("ManagerTelephone", rs.getString("ManagerTelephone"));
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            con.close();
        }
        return employee.toString();
    }

    @GetMapping("/getAllEmployees")
    public String getAllEmployees() throws Exception {
        JSONArray employeeList = new JSONArray();
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT * FROM EMPLOYEE WHERE BranchID=1";
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            int count = -1;
            while(rs.next()) {
                JSONObject employee = new JSONObject();
                employee.put("ssn", rs.getString("SSN"));
                employee.put("name", rs.getString("Name"));
                employee.put("phone", rs.getString("Telephone"));
                employee.put("Dependent", rs.getString("Dependent_Name"));

                String start_date = rs.getString("Start_date");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // Parse the start date
                LocalDate startDate = LocalDate.parse(start_date, formatter);
                // Get the current date
                LocalDate currentDate = LocalDate.now();
                // Calculate the period between the two dates
                Period period = Period.between(startDate, currentDate);
                // Extract the total number of years (length of employment)
                int length_of_employment = period.getYears();

                employee.put("Start_date", rs.getString("Start_date"));
                employee.put("Length_Emp", length_of_employment);
                employeeList.put(employee);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            con.close();
        }
        return employeeList.toString();
    }

    @GetMapping("/getMyEmployees")
    public String getMyEmployees(@RequestParam String employeeId) throws Exception {
        JSONArray employeeList = new JSONArray();
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT * FROM EMPLOYEE WHERE Super_SSN=?";
            ps = con.prepareStatement(query);
            ps.setString(1, employeeId);
            ResultSet rs = ps.executeQuery();
            int count = -1;
            while(rs.next()) {
                JSONObject employee = new JSONObject();
                employee.put("ssn", rs.getString("SSN"));
                employee.put("name", rs.getString("Name"));
                employee.put("phone", rs.getString("Telephone"));
                employeeList.put(employee);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            con.close();
        }
        return employeeList.toString();
    }

    @PostMapping("/updateEmployee")
    public String updateEmployee(@RequestBody String employeeInfo) throws Exception {
        JSONObject employee = new JSONObject(employeeInfo);
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "UPDATE EMPLOYEE SET Name=?, Telephone=?, Dependent_Name=? WHERE SSN=?";
            ps = con.prepareStatement(query);
            ps.setString(1, employee.getString("name"));
            ps.setString(2, employee.getString("phone"));
            ps.setString(3, employee.getString("Dependent_Name"));
            ps.setString(4, employee.getString("ssn"));
            status = !ps.execute();
        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }
        if(status) {
            return "Success";
        }
        return "Error";
    }

    @PostMapping("/deleteEmployee")
    public String deleteEmployee(@RequestBody String empSSN) throws Exception {
        JSONObject employee = new JSONObject(empSSN);
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "CALL delete_employee(\"" + employee.getString("ssn") + "\")";
            ps = con.prepareStatement(query);
            status = !ps.execute();
        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }
        if(status) {
            return "Success";
        }
        return "Error";
    }

    @PostMapping("/makeTransactionDeposit")
    public String makeTransactionDeposit(@RequestBody String transactionRequest) throws Exception {
        JSONObject deposit = new JSONObject(transactionRequest);
        String ssn = deposit.getString("ssn");
        String accountNumber = deposit.getString("accountNumber");
        String amount = deposit.getString("amount");
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;
        ResultSet rs;
        JSONObject response = new JSONObject();
        try {
            // get current balance from account table
            String balanceFromAccountQuery = "SELECT Balance FROM ACCOUNT WHERE Acc_no=?";
            ps = con.prepareStatement(balanceFromAccountQuery);
            ps.setString(1, accountNumber);
            rs = ps.executeQuery();
            rs.next();
            double balance = rs.getDouble("Balance");
            balance = balance + Double.parseDouble(amount);

            // insert into transaction with updated balance
            String query = "INSERT INTO TRANSACTION (Tranc_code, Tranc_type, Charge, Acc_no, Amount, Date, Hour, Balance_Snap)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, UUID.randomUUID().toString().replace("-", "").substring(0, 5));
            ps.setString(2, "CD");
            ps.setDouble(3, 0.0);
            ps.setString(4, accountNumber);
            ps.setDouble(5, Double.parseDouble(amount));

                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = currentDate.format(formatter);
                LocalTime currentTime = LocalTime.now();
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = currentTime.format(formatter);

            ps.setString(6, formattedDate);
            ps.setString(7, formattedTime);
            ps.setDouble(8, balance);
            status = !ps.execute();

            // update the balance in account table
            String accountUpdateQuery = "UPDATE ACCOUNT SET Balance=?, Recent_Access_Date=? WHERE Acc_no=?";
            ps = con.prepareStatement(accountUpdateQuery);
            ps.setDouble(1, balance);
            ps.setString(2, formattedDate);
            ps.setString(3, accountNumber);
            status = !ps.execute();

        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }

        if(status) {
            return "Success";
        }
        return "Error";
    }

    @PostMapping("/makeTransactionWithdraw")
    public String makeTransactionWithdraw(@RequestBody String transactionRequest) throws Exception {
        JSONObject deposit = new JSONObject(transactionRequest);
        String ssn = deposit.getString("ssn");
        String accountNumber = deposit.getString("accountNumber");
        boolean isChecking = accountNumber.startsWith("10");
        String amount = deposit.getString("amount");
        double charge = isChecking ? 1.00 : 10.00;
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;
        ResultSet rs;
        JSONObject response = new JSONObject();
        try {
            // get current balance from account table
            String balanceFromAccountQuery = "SELECT Balance FROM ACCOUNT WHERE Acc_no=?";
            ps = con.prepareStatement(balanceFromAccountQuery);
            ps.setString(1, accountNumber);
            rs = ps.executeQuery();
            rs.next();
            double balance = rs.getDouble("Balance");
            balance = balance - Double.parseDouble(amount) - charge;
            if(balance < 0) {
                return "Error";
            }

            // insert into transactin with updated balance
            String query = "INSERT INTO TRANSACTION (Tranc_code, Tranc_type, Charge, Acc_no, Amount, Date, Hour, Balance_Snap)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(query);
            ps.setString(1, UUID.randomUUID().toString().replace("-", "").substring(0, 5));
            ps.setString(2, "WD");
            ps.setDouble(3, charge);
            ps.setString(4, accountNumber);
            ps.setDouble(5, Double.parseDouble(amount));

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = currentDate.format(formatter);
            LocalTime currentTime = LocalTime.now();
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = currentTime.format(formatter);

            ps.setString(6, formattedDate);
            ps.setString(7, formattedTime);
            ps.setDouble(8, balance);
            status = !ps.execute();

            // update the balance in account table
            String accountUpdateQuery = "UPDATE ACCOUNT SET Balance=?, Recent_Access_Date=? WHERE Acc_no=?";
            ps = con.prepareStatement(accountUpdateQuery);
            ps.setDouble(1, balance);
            ps.setString(2, formattedDate);
            ps.setString(3, accountNumber);
            status = !ps.execute();

        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }

        if(status) {
            return "Success";
        }
        return "Error";
    }



    @GetMapping("/getBalance")
    public String getBalance(@RequestParam String accountNumber) throws Exception {
        JSONObject response = new JSONObject();
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;
        ResultSet rs;
        try {
            String balanceFromAccountQuery = "SELECT Balance FROM ACCOUNT WHERE Acc_no=?";
            ps = con.prepareStatement(balanceFromAccountQuery);
            ps.setString(1, accountNumber);
            rs = ps.executeQuery();
            rs.next();
            double balance = rs.getDouble("Balance");
            response.put("balance", balance);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            con.close();
        }
        return response.toString();
    }

    @PostMapping("/giveLoan")
    public String giveLoan(@RequestBody String data) throws Exception {
        System.out.println(data);
        JSONObject loanData = new JSONObject(data);
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            // get latest loan number
            ps = con.prepareStatement("SELECT MAX(Loan_Number) AS Loan_Number FROM LOAN");
            ResultSet rs = ps.executeQuery();
            rs.next();
            int loanNumber = Integer.valueOf(rs.getInt("Loan_Number"));
            loanNumber = loanNumber + 1;

            // insert loan
            String loanQuery = "INSERT INTO LOAN (Loan_Number, Loan_Amount, Monthly_repay, BranchID) VALUES (?, ?, 150, 1)";
            ps = con.prepareStatement(loanQuery);
            ps.setInt(1, loanNumber);
            ps.setString(2, loanData.getString("amount"));
            status = !ps.execute();

            // update loan number in customer table
            String query = "UPDATE CUSTOMER SET Loan_no=? WHERE SSN=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, loanNumber);
            ps.setString(2, loanData.getString("ssn"));
            status = !ps.execute();

        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }

        if(status) {
            return "Success";
        }
        return "Error";
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody String data) throws Exception {
        JSONObject customer = new JSONObject(data);
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT e.SSN AS EmployeeWithLeastCustomers, \n" +
                    "       COUNT(c.SSN) AS CustomerCount\n" +
                    "FROM EMPLOYEE e\n" +
                    "LEFT JOIN CUSTOMER c ON e.SSN = c.Personal_banker\n" +
                    "WHERE e.Super_SSN IN (\n" +
                    "    SELECT SSN \n" +
                    "    FROM EMPLOYEE \n" +
                    "    WHERE Super_SSN = (\n" +
                    "        SELECT Mgr_SSN \n" +
                    "        FROM BRANCH \n" +
                    "        WHERE BranchId = 1\n" +
                    "    )\n" +
                    ")\n" +
                    "GROUP BY e.SSN, e.Name\n" +
                    "ORDER BY CustomerCount ASC\n" +
                    "LIMIT 1;";

            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String personalBankersSSN = rs.getString("EmployeeWithLeastCustomers");

            // register the user
            query = "INSERT INTO CUSTOMER (SSN, Name, Password, Apt_no, Street, City, State, Zip, Personal_banker, Loan_no)\n" +
                    "VALUES\n" +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)";
            ps = con.prepareStatement(query);
            ps.setString(1, customer.getString("ssn"));
            ps.setString(2, customer.getString("name"));
            ps.setString(3, Base64.getEncoder().encodeToString((customer.getString("password")).getBytes()));
            ps.setInt(4, Integer.parseInt(customer.getString("apartment")));
            ps.setString(5, customer.getString("street"));
            ps.setString(6, customer.getString("city"));
            ps.setString(7, customer.getString("state"));
            ps.setString(8, customer.getString("zip"));
            ps.setString(9, personalBankersSSN);
            status = !ps.execute();

        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }

        if(status) {
            return "Success";
        }
        return "Error";
    }

    @PostMapping("/openAccount")
    public String openAccount(@RequestBody String data) throws Exception {
        // get new personal banker
        JSONObject account = new JSONObject(data);
        String ssn = account.getString("ssn");
        boolean isChecking = account.optBoolean("checking");
        boolean isSaving = account.optBoolean("saving");
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;
        ResultSet rs;
        JSONObject response = new JSONObject();
        try {
            String query = "INSERT INTO ACCOUNT (Acc_no, Acc_type, Balance, Recent_Access_Date, Customer_SSN)\n" +
                    "VALUES (?, ?, ?, ?, ?)";

            if(isChecking) {
                ps = con.prepareStatement(query);
                String generateCheckingAccountNumber = "10" + generateRandomDigits(8);
                ps.setString(1, generateCheckingAccountNumber);
                ps.setString(2, "CHECKING");
                ps.setDouble(3, 0.0);

                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = currentDate.format(formatter);

                ps.setString(4, formattedDate);
                ps.setString(5, ssn);
                response.put("checkingAccountNumber", generateCheckingAccountNumber);
                status = !ps.execute();
            }

            if(isSaving) {
                ps = con.prepareStatement(query);
                String generateSavingAccountNumber = "20" + generateRandomDigits(8);
                ps.setString(1, generateSavingAccountNumber);
                ps.setString(2, "SAVING");
                ps.setDouble(3, 0.0);

                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = currentDate.format(formatter);

                ps.setString(4, formattedDate);
                ps.setString(5, ssn);
                response.put("savingAccountNumber", generateSavingAccountNumber);
                status = !ps.execute();
            }

        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }

        if(status) {
            return response.toString();
        }
        return "Error";
    }

    public static String generateRandomDigits(int length) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            // Generate a random digit between 0 and 9
            int digit = random.nextInt(10);
            result.append(digit);
        }

        return result.toString();
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody String data) throws Exception {
        System.out.println(data);
        StringBuffer result = new StringBuffer();
        JSONObject j = new JSONObject(data);
        String ssn = j.getString("name");
        String password = j.getString("password");
        password = Base64.getEncoder().encodeToString(password.getBytes());
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT COUNT(1) AS Is_Exist FROM CUSTOMER WHERE SSN=\""+ ssn +"\" AND Password=\""+password+"\"";
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            int count = -1;
            while(rs.next()) {
                count = rs.getInt("Is_Exist");
            }
            if(count == 0) {
                result.append("Error");
            }else if(count == 1) {
                result.append("Success");
                result.append("-"+ssn);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }
        return result.toString();
    }

    @PostMapping("/loginEmployee")
    public String loginEmp(@RequestBody String data) throws Exception {
        StringBuffer result = new StringBuffer();
        JSONObject j = new JSONObject(data);
        String employeeId = j.getString("employeeId");
        String password = j.getString("password");
        password = Base64.getEncoder().encodeToString(password.getBytes());
        DatabaseUtility.loadDriver();
        Connection con = DatabaseUtility.getConnection();
        boolean status = false;
        PreparedStatement ps;

        try {
            String query = "SELECT COUNT(1) AS Is_Exist FROM EMPLOYEE WHERE SSN=\""+employeeId+"\" AND Password=\""+password+"\"";
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            int count = -1;
            while(rs.next()) {
                count = rs.getInt("Is_Exist");
            }
            if(count == 0) {
                result.append("Error");
            }else if(count == 1) {
                result.append("Success");
                String checkManagerQuery = "SELECT Mgr_SSN from BRANCH where BranchID=1";
                ps = con.prepareStatement(checkManagerQuery);
                rs = ps.executeQuery();
                while(rs.next()) {
                    String MgrSSN = rs.getString("Mgr_SSN");
                    if(MgrSSN.equals(employeeId)) {
                        result.append("Success_isManager");
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
            return "Error";
        }finally {
            con.close();
        }
        return result.toString();
    }

    private static String getSalt(){
        return "alksdfn;odinvnawkebfofnsldkn;ondvsvmksldmnfl;kds";
    }

}
