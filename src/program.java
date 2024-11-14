import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class program {
    // Database credentials
    final static String HOSTNAME = "nawa0006-sql-server.database.windows.net";
    final static String DBNAME = "cs-dsa-4513-sql-db";
    final static String USERNAME = "nawa0006";
    final static String PASSWORD = "A@n083307";

    // Database connection string
    final static String URL = String.format(
            "jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;"
            + "encrypt=true;trustServerCertificate=false;"
            + "hostNameInCertificate=*.database.windows.net;"
            + "loginTimeout=30;",
            HOSTNAME, DBNAME, USERNAME, PASSWORD
    );

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL);
             Scanner scanner = new Scanner(System.in)) {

            int option = 0;
            while (option != 16) {
                System.out.println("Select an option:");
                System.out.println("1. Enter a new team");
                System.out.println("2. Enter a new client and associate with teams");
                System.out.println("3. Enter a new volunteer and associate with teams");
                System.out.println("4. Enter volunteer hours worked for a team");
                System.out.println("5. Enter a new employee and associate with teams");
                System.out.println("6. Enter an expense charged by an employee");
                System.out.println("7. Enter a new donor and donations");
                System.out.println("8. Retrieve doctor details of a client");
                System.out.println("9. Retrieve total expenses by employee for a period");
                System.out.println("10. Retrieve list of volunteers for a client’s teams");
                System.out.println("11. Retrieve names of teams founded after a date");
                System.out.println("12. Retrieve all people with contact information");
                System.out.println("13. Retrieve name and donations of donor-employees");
                System.out.println("14. Increase salary by 10% for eligible employees");
                System.out.println("15. Delete clients without health insurance and importance < 5");
                System.out.println("16. Import: Enter new teams from a data file");
                System.out.println("17. Export: Retrieve names and mailing addresses of people on the mailing list");
                System.out.println("18. Quit");
                option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> insertNewTeam(connection, scanner);
                    case 2 -> insertNewClient(connection, scanner);
                    case 3 -> insertNewVolunteer(connection, scanner);
                    case 4 -> enterVolunteerHours(connection, scanner);
                    case 5 -> insertNewEmployee(connection, scanner);
                    case 6 -> enterEmployeeExpense(connection, scanner);
                    case 7 -> insertNewDonorAndDonations(connection, scanner);
                    case 8 -> retrieveDoctorDetails(connection, scanner);
                    case 9 -> retrieveTotalExpenses(connection, scanner);
                    case 10 -> retrieveVolunteersForClientTeams(connection, scanner);
                    case 11 -> retrieveTeamsFoundedAfterDate(connection, scanner);
                    case 12 -> retrieveAllPeopleWithContact(connection);
                    case 13 -> retrieveDonorEmployees(connection);
                    case 14 -> increaseEmployeeSalaries(connection);
                    case 15 -> deleteIneligibleClients(connection);
                    case 16 -> importTeamsFromFile(connection, scanner);
                    case 17 -> exportMailingListToFile(connection, scanner);
                    case 18 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid option. Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertNewTeam(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter team name:");
        String name = scanner.next();
        System.out.println("Enter team type:");
        String type = scanner.next();
        System.out.println("Enter formation date (yyyy-mm-dd):");
        String dateFormed = scanner.next();

        String query = "INSERT INTO team (name, type, date_formed) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setDate(3, Date.valueOf(dateFormed));
            stmt.executeUpdate();
            System.out.println("Team inserted successfully.");
        }
    }
    
    private static void insertPersonWithEmergencyContact(Connection connection, Scanner scanner, int ssn) throws SQLException {
        // Check if person already exists
        String checkPersonQuery = "SELECT 1 FROM person WHERE ssn = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkPersonQuery)) {
            checkStmt.setInt(1, ssn);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                // Person does not exist, prompt for details
                System.out.println("Person not found. Enter additional details to add person record.");
                System.out.println("Enter name:");
                String name = scanner.nextLine();
                System.out.println("Enter gender (M/F):");
                String gender = scanner.nextLine();
                System.out.println("Enter profession:");
                String profession = scanner.nextLine();
                System.out.println("Enter mailing address:");
                String mailingAddress = scanner.nextLine();
                System.out.println("Enter email address:");
                String emailAddress = scanner.nextLine();
                System.out.println("Enter phone number:");
                String phoneNumber = scanner.nextLine();
                System.out.println("On mailing list? (true/false):");
                boolean onMailingList = scanner.nextBoolean();
                scanner.nextLine(); // Consume newline

                // Insert person details
                String insertPersonQuery = "INSERT INTO person (ssn, name, gender, profession, mailing_address, email_address, phone_number, on_mailing_list) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertPersonStmt = connection.prepareStatement(insertPersonQuery)) {
                    insertPersonStmt.setInt(1, ssn);
                    insertPersonStmt.setString(2, name);
                    insertPersonStmt.setString(3, gender);
                    insertPersonStmt.setString(4, profession);
                    insertPersonStmt.setString(5, mailingAddress);
                    insertPersonStmt.setString(6, emailAddress);
                    insertPersonStmt.setString(7, phoneNumber);
                    insertPersonStmt.setBoolean(8, onMailingList);
                    insertPersonStmt.executeUpdate();
                    System.out.println("Person record added successfully.");
                }

                // Prompt for emergency contact details
                System.out.println("Enter emergency contact name:");
                String contactName = scanner.nextLine();
                System.out.println("Enter emergency contact phone number:");
                String contactPhone = scanner.nextLine();
                System.out.println("Enter relationship to person:");
                String relationship = scanner.nextLine();

                // Insert emergency contact
                String contactQuery = "INSERT INTO emergency_contact (person_ssn, name, phone_number, relationship) VALUES (?, ?, ?, ?)";
                try (PreparedStatement contactStmt = connection.prepareStatement(contactQuery)) {
                    contactStmt.setInt(1, ssn);
                    contactStmt.setString(2, contactName);
                    contactStmt.setString(3, contactPhone);
                    contactStmt.setString(4, relationship);
                    contactStmt.executeUpdate();
                    System.out.println("Emergency contact inserted successfully.");
                }
            }
        }
    }


    private static void insertNewClient(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter client SSN:");
        int ssn = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Call helper method to ensure person and emergency contact are inserted
        insertPersonWithEmergencyContact(connection, scanner, ssn);

        // Insert specific client details
        System.out.println("Enter doctor name:");
        String doctorName = scanner.nextLine();
        System.out.println("Enter doctor phone number:");
        String doctorPhone = scanner.nextLine();
        System.out.println("Enter date assigned (yyyy-mm-dd):");
        String dateAssigned = scanner.nextLine();

        String clientQuery = "INSERT INTO client (ssn, doctor_name, doctor_phone_number, date_assigned) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(clientQuery)) {
            stmt.setInt(1, ssn);
            stmt.setString(2, doctorName);
            stmt.setString(3, doctorPhone);
            stmt.setDate(4, Date.valueOf(dateAssigned));
            stmt.executeUpdate();
            System.out.println("Client inserted successfully.");
        }

        // Add client needs
        System.out.println("Enter number of needs for the client:");
        int numberOfNeeds = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String needQuery = "INSERT INTO need (person_ssn, need_type, importance) VALUES (?, ?, ?)";
        try (PreparedStatement needStmt = connection.prepareStatement(needQuery)) {
            for (int i = 0; i < numberOfNeeds; i++) {
                System.out.println("Enter need type (e.g., visiting, shopping, housekeeping):");
                String needType = scanner.nextLine();
                System.out.println("Enter importance (1-10):");
                int importance = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                needStmt.setInt(1, ssn);
                needStmt.setString(2, needType);
                needStmt.setInt(3, importance);
                needStmt.executeUpdate();
                System.out.println("Need added successfully.");
            }
        }

        // Add insurance policies for the client
        System.out.println("Enter number of insurance policies for the client:");
        int numberOfPolicies = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String insuranceQuery = "INSERT INTO insurance_policy (id, provider_name, provider_address, type) VALUES (?, ?, ?, ?)";
        String hasPolicyQuery = "INSERT INTO has_policy (client_ssn, policy_id) VALUES (?, ?)";

        try (PreparedStatement insuranceStmt = connection.prepareStatement(insuranceQuery);
             PreparedStatement hasPolicyStmt = connection.prepareStatement(hasPolicyQuery)) {
            for (int i = 0; i < numberOfPolicies; i++) {
                System.out.println("Enter unique policy ID:");
                int policyId = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                System.out.println("Enter provider name:");
                String providerName = scanner.nextLine();
                System.out.println("Enter provider address:");
                String providerAddress = scanner.nextLine();
                System.out.println("Enter policy type (e.g., life, health, home, auto):");
                String policyType = scanner.nextLine();

                // Insert policy details
                insuranceStmt.setInt(1, policyId);
                insuranceStmt.setString(2, providerName);
                insuranceStmt.setString(3, providerAddress);
                insuranceStmt.setString(4, policyType);
                insuranceStmt.executeUpdate();
                System.out.println("Insurance policy added successfully.");

                // Link the policy to the client
                hasPolicyStmt.setInt(1, ssn);
                hasPolicyStmt.setInt(2, policyId);
                hasPolicyStmt.executeUpdate();
                System.out.println("Client linked with insurance policy.");
            }
        }

        // Associate the client with a team
        System.out.println("Enter team name to associate with the client:");
        String teamName = scanner.nextLine();
        System.out.println("Is this team currently serving the client? (true/false):");
        boolean isActive = scanner.nextBoolean();
        scanner.nextLine(); // Consume newline
        
        // Check if the team exists
        String checkTeamQuery = "SELECT 1 FROM team WHERE name = ?";
        try (PreparedStatement checkTeamStmt = connection.prepareStatement(checkTeamQuery)) {
            checkTeamStmt.setString(1, teamName);
            ResultSet teamResult = checkTeamStmt.executeQuery();

            if (!teamResult.next()) {
                // If team does not exist, prompt the user to enter details to create a new team
                System.out.println("Team not found. Enter details to add a new team.");
                System.out.println("Enter team type:");
                String teamType = scanner.nextLine();
                System.out.println("Enter date formed (yyyy-mm-dd):");
                String dateFormed = scanner.nextLine();

                String insertTeamQuery = "INSERT INTO team (name, type, date_formed) VALUES (?, ?, ?)";
                try (PreparedStatement insertTeamStmt = connection.prepareStatement(insertTeamQuery)) {
                    insertTeamStmt.setString(1, teamName);
                    insertTeamStmt.setString(2, teamType);
                    insertTeamStmt.setDate(3, Date.valueOf(dateFormed));
                    insertTeamStmt.executeUpdate();
                    System.out.println("Team record added successfully.");
                }
            }
        }

        String teamServesQuery = "INSERT INTO team_serves (client_ssn, team_name, is_active) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(teamServesQuery)) {
            stmt.setInt(1, ssn);
            stmt.setString(2, teamName);
            stmt.setBoolean(3, isActive);
            stmt.executeUpdate();
            System.out.println("Client associated with the team successfully.");
        }
    }


 

    private static void enterVolunteerHours(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter volunteer SSN:");
        int volunteerSSN = scanner.nextInt();
        System.out.println("Enter team name:");
        String teamName = scanner.next();
        System.out.println("Enter hours worked:");
        int hoursWorked = scanner.nextInt();

        String query = "UPDATE belongs_to SET hours_worked = ? WHERE volunteer_ssn = ? AND team_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, hoursWorked);
            stmt.setInt(2, volunteerSSN);
            stmt.setString(3, teamName);
            stmt.executeUpdate();
            System.out.println("Volunteer hours updated successfully.");
        }
    }

    private static void retrieveDoctorDetails(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter client SSN:");
        int ssn = scanner.nextInt();

        String query = "SELECT doctor_name, doctor_phone_number FROM client WHERE ssn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Doctor Name: " + rs.getString("doctor_name"));
                    System.out.println("Doctor Phone: " + rs.getString("doctor_phone_number"));
                } else {
                    System.out.println("No client found with the given SSN.");
                }
            }
        }
    }

    private static void retrieveTeamsFoundedAfterDate(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter date (yyyy-mm-dd):");
        String date = scanner.next();

        String query = "SELECT name FROM team WHERE date_formed > ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Team Name: " + rs.getString("name"));
                }
            }
        }
    }

    private static void retrieveAllPeopleWithContact(Connection connection) throws SQLException {
        String query = "SELECT person.ssn, person.name AS person_name, person.phone_number AS person_phone, " +
                       "person.email_address, emergency_contact.name AS contact_name, emergency_contact.phone_number AS contact_phone, " +
                       "emergency_contact.relationship " +
                       "FROM person " +
                       "LEFT JOIN emergency_contact ON person.ssn = emergency_contact.person_ssn";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int ssn = rs.getInt("ssn");
                String personName = rs.getString("person_name");
                String personPhone = rs.getString("person_phone");
                String emailAddress = rs.getString("email_address");
                String contactName = rs.getString("contact_name");
                String contactPhone = rs.getString("contact_phone");
                String relationship = rs.getString("relationship");

                System.out.println("SSN: " + ssn + ", Name: " + personName + ", Phone: " + personPhone + 
                                   ", Email: " + emailAddress + ", Emergency Contact: " + contactName + 
                                   ", Contact Phone: " + contactPhone + ", Relationship: " + relationship);
            }
        }
    }


    private static void insertNewVolunteer(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter volunteer SSN:");
        int ssn = scanner.nextInt();
        scanner.nextLine();

        // Check if the person already exists, insert if not
        insertPersonWithEmergencyContact(connection, scanner, ssn);

        // Now, insert into the volunteer table
        System.out.println("Enter date joined (yyyy-mm-dd):");
        String dateJoined = scanner.nextLine();
        System.out.println("Enter last training date (yyyy-mm-dd):");
        String lastTrainingDate = scanner.nextLine();
        System.out.println("Enter last training location:");
        String lastTrainingLocation = scanner.nextLine();

        String query = "INSERT INTO volunteer (ssn, date_joined, last_training_date, last_training_location) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ssn);
            stmt.setDate(2, Date.valueOf(dateJoined));
            stmt.setDate(3, Date.valueOf(lastTrainingDate));
            stmt.setString(4, lastTrainingLocation);
            stmt.executeUpdate();
            System.out.println("Volunteer inserted successfully.");
        }

        // Associate the volunteer with a team
        System.out.println("Enter team name to associate with the volunteer:");
        String teamName = scanner.nextLine();

        // Check if the team exists
        String checkTeamQuery = "SELECT 1 FROM team WHERE name = ?";
        try (PreparedStatement checkTeamStmt = connection.prepareStatement(checkTeamQuery)) {
            checkTeamStmt.setString(1, teamName);
            ResultSet teamResult = checkTeamStmt.executeQuery();

            if (!teamResult.next()) {
                // If team does not exist, prompt the user to enter details to create a new team
                System.out.println("Team not found. Enter details to add a new team.");
                System.out.println("Enter team type:");
                String teamType = scanner.nextLine();
                System.out.println("Enter date formed (yyyy-mm-dd):");
                String dateFormed = scanner.nextLine();

                String insertTeamQuery = "INSERT INTO team (name, type, date_formed) VALUES (?, ?, ?)";
                try (PreparedStatement insertTeamStmt = connection.prepareStatement(insertTeamQuery)) {
                    insertTeamStmt.setString(1, teamName);
                    insertTeamStmt.setString(2, teamType);
                    insertTeamStmt.setDate(3, Date.valueOf(dateFormed));
                    insertTeamStmt.executeUpdate();
                    System.out.println("Team record added successfully.");
                }
            }
        }

        System.out.println("Is the volunteer active on this team? (true/false):");
        boolean isActive = scanner.nextBoolean();
        scanner.nextLine();
        System.out.println("Enter hours worked by volunteer for this team:");
        int hoursWorked = scanner.nextInt();
        scanner.nextLine();

        String associateQuery = "INSERT INTO belongs_to (volunteer_ssn, team_name, is_active, hours_worked) VALUES (?, ?, ?, ?)";
        try (PreparedStatement associateStmt = connection.prepareStatement(associateQuery)) {
            associateStmt.setInt(1, ssn);
            associateStmt.setString(2, teamName);
            associateStmt.setBoolean(3, isActive);
            associateStmt.setInt(4, hoursWorked);
            associateStmt.executeUpdate();
            System.out.println("Volunteer successfully associated with the team.");
        }
    }





 // Function to insert a new employee and associate with teams
    private static void insertNewEmployee(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter employee SSN:");
        int ssn = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Call helper method to ensure person and emergency contact are inserted
        insertPersonWithEmergencyContact(connection, scanner, ssn);

        // Check if the employee already exists in the employee table
        String checkEmployeeQuery = "SELECT 1 FROM employee WHERE ssn = ?";
        boolean employeeExists;
        try (PreparedStatement checkStmt = connection.prepareStatement(checkEmployeeQuery)) {
            checkStmt.setInt(1, ssn);
            ResultSet rs = checkStmt.executeQuery();
            employeeExists = rs.next(); // true if employee already exists
        }

        // Insert specific employee details only if they do not exist
        if (!employeeExists) {
            System.out.println("Enter salary:");
            double salary = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            System.out.println("Enter marital status:");
            String maritalStatus = scanner.nextLine();
            System.out.println("Enter hire date (yyyy-mm-dd):");
            String hireDate = scanner.nextLine();

            String employeeQuery = "INSERT INTO employee (ssn, salary, marital_status, hire_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(employeeQuery)) {
                stmt.setInt(1, ssn);
                stmt.setDouble(2, salary);
                stmt.setString(3, maritalStatus);
                stmt.setDate(4, Date.valueOf(hireDate));
                stmt.executeUpdate();
                System.out.println("Employee inserted successfully.");
            }
        } else {
            System.out.println("Employee already exists, proceeding to team association.");
        }

        // Associate the employee with a team
        System.out.println("Enter team name to associate with the employee:");
        String teamName = scanner.nextLine();

        // Check if the team exists
        String checkTeamQuery = "SELECT 1 FROM team WHERE name = ?";
        try (PreparedStatement checkTeamStmt = connection.prepareStatement(checkTeamQuery)) {
            checkTeamStmt.setString(1, teamName);
            ResultSet teamResult = checkTeamStmt.executeQuery();

            if (!teamResult.next()) {
                // If team does not exist, prompt the user to enter details to create a new team
                System.out.println("Team not found. Enter details to add a new team.");
                System.out.println("Enter team type:");
                String teamType = scanner.nextLine();
                System.out.println("Enter date formed (yyyy-mm-dd):");
                String dateFormed = scanner.nextLine();

                String insertTeamQuery = "INSERT INTO team (name, type, date_formed) VALUES (?, ?, ?)";
                try (PreparedStatement insertTeamStmt = connection.prepareStatement(insertTeamQuery)) {
                    insertTeamStmt.setString(1, teamName);
                    insertTeamStmt.setString(2, teamType);
                    insertTeamStmt.setDate(3, Date.valueOf(dateFormed));
                    insertTeamStmt.executeUpdate();
                    System.out.println("Team record added successfully.");
                }
            }
        }

        System.out.println("Enter the date of reporting (yyyy-mm-dd):");
        String reportingDate = scanner.nextLine();
        System.out.println("Enter description of the report:");
        String reportDescription = scanner.nextLine();

        String teamReportsToQuery = "INSERT INTO team_reports_to (team_name, employee_ssn, date, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(teamReportsToQuery)) {
            stmt.setString(1, teamName);
            stmt.setInt(2, ssn);
            stmt.setDate(3, Date.valueOf(reportingDate));
            stmt.setString(4, reportDescription);
            stmt.executeUpdate();
            System.out.println("Employee associated with the team successfully.");
        }
    }




    // Function to enter an expense charged by an employee
    private static void enterEmployeeExpense(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter employee SSN:");
        int employeeSSN = scanner.nextInt();
        System.out.println("Enter expense date (yyyy-mm-dd):");
        String date = scanner.next();
        System.out.println("Enter amount:");
        double amount = scanner.nextDouble();
        System.out.println("Enter description:");
        String description = scanner.next();

        String query = "INSERT INTO expense (employee_ssn, date, amount, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeSSN);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setDouble(3, amount);
            stmt.setString(4, description);
            stmt.executeUpdate();
            System.out.println("Expense recorded successfully.");
        }
    }

    // Function to insert a new donor and associate with donations
    private static void insertNewDonorAndDonations(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter donor SSN:");
        int ssn = scanner.nextInt();
        scanner.nextLine(); // Consume the leftover newline character

        // Check if the person already exists, insert if not
        insertPersonWithEmergencyContact(connection, scanner, ssn);

        // Now, insert into the donor table
        System.out.println("Is the donor anonymous? (true/false):");
        boolean isAnonymous = scanner.nextBoolean();
        scanner.nextLine(); // Consume newline

        String donorQuery = "INSERT INTO donor (ssn, is_anonymous) VALUES (?, ?)";
        try (PreparedStatement donorStmt = connection.prepareStatement(donorQuery)) {
            donorStmt.setInt(1, ssn);
            donorStmt.setBoolean(2, isAnonymous);
            donorStmt.executeUpdate();
            System.out.println("Donor inserted successfully.");
        }

        // Enter donations for the donor
        System.out.println("Enter the number of donations:");
        int donationCount = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String donationQuery = "INSERT INTO donation (donor_ssn, date, type, campaign_name, amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement donationStmt = connection.prepareStatement(donationQuery)) {
            for (int i = 0; i < donationCount; i++) {
                System.out.println("Enter donation date (yyyy-mm-dd):");
                String date = scanner.nextLine();
                System.out.println("Enter donation type:");
                String type = scanner.nextLine();
                System.out.println("Enter campaign name:");
                String campaignName = scanner.nextLine();
                System.out.println("Enter donation amount:");
                double amount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline

                // Insert donation details
                donationStmt.setInt(1, ssn);
                donationStmt.setDate(2, Date.valueOf(date));
                donationStmt.setString(3, type);
                donationStmt.setString(4, campaignName);
                donationStmt.setDouble(5, amount);
                donationStmt.executeUpdate();

                // Ask for payment method and store accordingly
                System.out.println("Was this donation made by credit card or check? (Enter 'credit' or 'check')");
                String paymentMethod = scanner.nextLine().toLowerCase();

                if (paymentMethod.equals("credit")) {
                    System.out.println("Enter credit card number:");
                    String cardNumber = scanner.nextLine();
                    System.out.println("Enter credit card type:");
                    String cardType = scanner.nextLine();
                    System.out.println("Enter expiration date (yyyy-mm-dd):");
                    String expirationDate = scanner.nextLine();

                    String creditCardQuery = "INSERT INTO credit_card (donor_ssn, card_number, card_type, expiration_date) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement creditStmt = connection.prepareStatement(creditCardQuery)) {
                        creditStmt.setInt(1, ssn);
                        creditStmt.setString(2, cardNumber);
                        creditStmt.setString(3, cardType);
                        creditStmt.setDate(4, Date.valueOf(expirationDate));
                        creditStmt.executeUpdate();
                        System.out.println("Credit card payment recorded successfully.");
                    }

                } else if (paymentMethod.equals("check")) {
                    System.out.println("Enter check number:");
                    String checkNumber = scanner.nextLine();

                    String checkPaymentQuery = "INSERT INTO check_payment (donor_ssn, check_number) VALUES (?, ?)";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkPaymentQuery)) {
                        checkStmt.setInt(1, ssn);
                        checkStmt.setString(2, checkNumber);
                        checkStmt.executeUpdate();
                        System.out.println("Check payment recorded successfully.");
                    }
                } else {
                    System.out.println("Invalid payment method. Skipping payment details for this donation.");
                }
            }
            System.out.println("Donations recorded successfully.");
        }
    }



    // Function to retrieve the total amount of expenses by each employee for a given period
    private static void retrieveTotalExpenses(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter start date (yyyy-mm-dd):");
        String startDate = scanner.next();
        System.out.println("Enter end date (yyyy-mm-dd):");
        String endDate = scanner.next();

        String query = "SELECT employee_ssn, SUM(amount) AS total_expense FROM expense " +
                       "WHERE date BETWEEN ? AND ? GROUP BY employee_ssn ORDER BY total_expense DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int employeeSSN = rs.getInt("employee_ssn");
                    double totalExpense = rs.getDouble("total_expense");
                    System.out.println("Employee SSN: " + employeeSSN + ", Total Expense: " + totalExpense);
                }
            }
        }
    }

    // Function to retrieve the list of volunteers that are members of teams that support a particular client
    private static void retrieveVolunteersForClientTeams(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter client SSN:");
        int clientSSN = scanner.nextInt();

        String query = "SELECT DISTINCT volunteer.ssn, volunteer.date_joined FROM volunteer " +
                       "JOIN belongs_to ON volunteer.ssn = belongs_to.volunteer_ssn " +
                       "JOIN team_serves ON belongs_to.team_name = team_serves.team_name " +
                       "WHERE team_serves.client_ssn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, clientSSN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int volunteerSSN = rs.getInt("ssn");
                    Date dateJoined = rs.getDate("date_joined");
                    System.out.println("Volunteer SSN: " + volunteerSSN + ", Date Joined: " + dateJoined);
                }
            }
        }
    }

    // Function to retrieve donor-employees' names and total donations
    private static void retrieveDonorEmployees(Connection connection) throws SQLException {
        String query = "SELECT person.name, SUM(donation.amount) AS total_donated, donor.is_anonymous " +
                       "FROM donor JOIN employee ON donor.ssn = employee.ssn " +
                       "JOIN donation ON donor.ssn = donation.donor_ssn " +
                       "JOIN person ON donor.ssn = person.ssn " +
                       "GROUP BY person.name, donor.is_anonymous " +
                       "ORDER BY total_donated DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double totalDonated = rs.getDouble("total_donated");
                boolean isAnonymous = rs.getBoolean("is_anonymous");
                System.out.println("Name: " + name + ", Total Donated: " + totalDonated + 
                                   ", Anonymous: " + isAnonymous);
            }
        }
    }

    // Function to increase the salary by 10% for employees to whom more than one team reports
    private static void increaseEmployeeSalaries(Connection connection) throws SQLException {
        String query = "UPDATE employee SET salary = salary * 1.1 " +
                       "WHERE ssn IN (SELECT employee_ssn FROM team_reports_to GROUP BY employee_ssn HAVING COUNT(team_name) > 1)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Salaries increased for " + rowsUpdated + " employees.");
        }
    }

    // Function to delete clients who do not have health insurance and whose importance for transportation is less than 5
    private static void deleteIneligibleClients(Connection connection) throws SQLException {
        // Step 1: Delete related records from has_policy for clients matching the criteria
        String deleteHasPolicyQuery = "DELETE FROM has_policy WHERE client_ssn IN (" +
                                      "SELECT client.ssn FROM client " +
                                      "JOIN need ON client.ssn = need.person_ssn " +
                                      "WHERE need.need_type = 'transportation' AND need.importance < 5 " +
                                      "AND client.ssn NOT IN (" +
                                      "   SELECT has_policy.client_ssn FROM has_policy " +
                                      "   JOIN insurance_policy ON has_policy.policy_id = insurance_policy.id " +
                                      "   WHERE insurance_policy.type = 'health'" +
                                      "))";
        try (PreparedStatement deleteHasPolicyStmt = connection.prepareStatement(deleteHasPolicyQuery)) {
            int policiesDeleted = deleteHasPolicyStmt.executeUpdate();
            System.out.println("Deleted " + policiesDeleted + " policies associated with ineligible clients.");
        }

        // Step 2: Delete related records from team_serves for clients matching the criteria
        String deleteTeamServesQuery = "DELETE FROM team_serves WHERE client_ssn IN (" +
                                       "SELECT client.ssn FROM client " +
                                       "JOIN need ON client.ssn = need.person_ssn " +
                                       "WHERE need.need_type = 'transportation' AND need.importance < 5 " +
                                       "AND client.ssn NOT IN (" +
                                       "   SELECT has_policy.client_ssn FROM has_policy " +
                                       "   JOIN insurance_policy ON has_policy.policy_id = insurance_policy.id " +
                                       "   WHERE insurance_policy.type = 'health'" +
                                       "))";
        try (PreparedStatement deleteTeamServesStmt = connection.prepareStatement(deleteTeamServesQuery)) {
            int teamServesDeleted = deleteTeamServesStmt.executeUpdate();
            System.out.println("Deleted " + teamServesDeleted + " team associations for ineligible clients.");
        }

        // Step 3: Delete the ineligible clients
        String deleteClientQuery = "DELETE FROM client WHERE ssn IN (" +
                                   "SELECT client.ssn FROM client " +
                                   "JOIN need ON client.ssn = need.person_ssn " +
                                   "WHERE need.need_type = 'transportation' AND need.importance < 5 " +
                                   "AND client.ssn NOT IN (" +
                                   "   SELECT has_policy.client_ssn FROM has_policy " +
                                   "   JOIN insurance_policy ON has_policy.policy_id = insurance_policy.id " +
                                   "   WHERE insurance_policy.type = 'health'" +
                                   "))";
        try (PreparedStatement deleteClientStmt = connection.prepareStatement(deleteClientQuery)) {
            int clientsDeleted = deleteClientStmt.executeUpdate();
            System.out.println("Deleted " + clientsDeleted + " ineligible clients.");
        }
    }
    
    private static void importTeamsFromFile(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter the input file name:");
        String fileName = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            String insertTeamQuery = "INSERT INTO team (name, type, date_formed) VALUES (?, ?, ?)";

            while ((line = reader.readLine()) != null) {
                String[] teamData = line.split(",");
                if (teamData.length != 3) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                String name = teamData[0].trim();
                String type = teamData[1].trim();
                String dateFormed = teamData[2].trim();

                try (PreparedStatement stmt = connection.prepareStatement(insertTeamQuery)) {
                    stmt.setString(1, name);
                    stmt.setString(2, type);
                    stmt.setDate(3, Date.valueOf(dateFormed));
                    stmt.executeUpdate();
                    System.out.println("Inserted team: " + name);
                } catch (SQLException e) {
                    System.out.println("Error inserting team " + name + ": " + e.getMessage());
                }
            }
            System.out.println("Finished importing teams from file.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static void exportMailingListToFile(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter the output file name:");
        String fileName = scanner.nextLine();

        String query = "SELECT name, mailing_address FROM person WHERE on_mailing_list = 1";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            while (rs.next()) {
                String name = rs.getString("name");
                String mailingAddress = rs.getString("mailing_address");
                writer.write(name + ", " + mailingAddress);
                writer.newLine();
            }
            System.out.println("Mailing list exported to file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

}
