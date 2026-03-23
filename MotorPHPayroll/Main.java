import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;

/**
 * =============================================================================
 * PROJECT      : MotorPH Payroll System
 * MILESTONE    : Milestone 2 - Initial Code Submission
 * FILE         : Main.java
 * DESCRIPTION  : A console-based payroll system for MotorPH that handles
 *                employee login, employee detail lookup, and semi-monthly
 *                payroll processing from June to December.
 *
 * HOW THE PROGRAM WORKS:
 *   1. The user logs in using a valid username and password.
 *   2. If the user is "employee", they may look up their own details.
 *   3. If the user is "payroll_staff", they may process payroll for one
 *      employee or all employees.
 *   4. Payroll is split into two cutoff periods per month:
 *        - 1st Cutoff : Days 1 to 15  (gross salary only, no deductions yet)
 *        - 2nd Cutoff : Days 16 to 31 (gross salary minus all deductions)
 *   5. Government deductions are computed from the combined monthly gross
 *      (1st + 2nd cutoff) and are applied only on the 2nd cutoff payout.
 *
 * COMPUTATION RULES:
 *   - Work hours are counted strictly between 8:00 AM and 5:00 PM only.
 *   - A 10-minute grace period applies: employees who log in at 8:10 AM
 *     or earlier are treated as having logged in at exactly 8:00 AM.
 *   - Employees who log in after 8:10 AM are counted from their actual time.
 *   - Time-out beyond 5:00 PM is capped at 5:00 PM (no overtime counted).
 *   - A 1-hour unpaid lunch break is deducted from each day's hours.
 *   - No rounding of any computed values; exact results are displayed.
 *   - No OOP concepts are used; all logic is in static methods.
 *   - This is a single Java file.
 *
 * GOVERNMENT DEDUCTION RULES (based on provided tables):
 *   - SSS        : Based on monthly basic salary bracket table.
 *   - PhilHealth : 3% of monthly basic salary; employee pays 50% of total.
 *                  Minimum premium = PHP 300 (for salary below PHP 10,000).
 *                  Maximum premium = PHP 1,800 (for salary PHP 60,000 and above).
 *   - Pag-IBIG   : 1% if salary is PHP 1,000-1,500; 2% if above PHP 1,500.
 *                  Maximum employee contribution is PHP 100.
 *   - Withholding Tax : Computed AFTER subtracting SSS, PhilHealth, and
 *                       Pag-IBIG from the combined monthly gross salary.
 *
 * DATA FILES REQUIRED (must be in the same directory as this program):
 *   - "Employee Details.csv"  : Employee information records.
 *   - "Attendance Record.csv" : Daily time-in and time-out logs.
 * =============================================================================
 */
public class Main {

    // =========================================================================
    // SECTION 1: PROGRAM ENTRY POINT
    // =========================================================================

    /**
     * main() - The starting point of the MotorPH Payroll System.
     *
     * This method displays the login screen, collects credentials from the
     * user, and validates them against the two accepted accounts. If the
     * credentials are correct, the user is directed to their respective menu.
     * If they are incorrect, an error message is shown and the program ends.
     *
     * Valid usernames : "employee" or "payroll_staff"
     * Valid password  : "12345"
     *
     * @param args  Command-line arguments (not used in this program).
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Display the portal login header
        System.out.println("============================================");
        System.out.println("        MOTORPH EMPLOYEE PORTAL");
        System.out.println("============================================");
        System.out.print("Username : ");
        String username = input.nextLine().trim();
        System.out.print("Password : ");
        String password = input.nextLine().trim();

        // Validate the entered username against the two accepted user roles
        boolean isValidUsername = username.equals("employee") || username.equals("payroll_staff");

        // Validate the entered password against the system password
        boolean isValidPassword = password.equals("12345");

        // Deny access if either the username or password is incorrect
        if (!isValidUsername || !isValidPassword) {
            System.out.println("\nIncorrect username and/or password.");
            System.out.println("Terminating program...");
            return;
        }

        System.out.println("\nLogin successful! Welcome, " + username + ".");

        // Route the authenticated user to the menu that matches their role
        if (username.equals("employee")) {
            showEmployeeMenu(input);
        } else {
            showPayrollStaffMenu(input);
        }
    }

    // =========================================================================
    // SECTION 2: MENUS
    // =========================================================================

    /**
     * showEmployeeMenu() - Displays and handles the menu for the "employee" role.
     *
     * An employee may enter their employee number to view their basic personal
     * details (employee number, full name, and birthday). Payroll data is not
     * accessible from this menu. The menu loops until the user chooses to exit.
     *
     * @param input  The Scanner used to read user input from the console.
     */
    public static void showEmployeeMenu(Scanner input) {
        while (true) {
            System.out.println("\n============================================");
            System.out.println("             EMPLOYEE MENU");
            System.out.println("============================================");
            System.out.println("[1] Enter your employee number");
            System.out.println("[2] Exit the program");
            System.out.print("Choice : ");
            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                // Prompt for employee number and display the matching employee's details
                System.out.print("Enter Employee Number : ");
                String employeeId = input.nextLine().trim();
                displayEmployeeDetails(employeeId);

            } else if (choice.equals("2")) {
                // Gracefully exit the program
                System.out.println("Terminating the program. Goodbye!");
                break;

            } else {
                // Reject any input that is not a valid menu option
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    /**
     * showPayrollStaffMenu() - Displays and handles the menu for "payroll_staff".
     *
     * Payroll staff can process payroll for a single employee (by employee number)
     * or for all employees at once. The payroll computation covers June through
     * December and displays both cutoff periods per month. Allowances are excluded
     * from payroll processing per mentor instructions.
     *
     * @param input  The Scanner used to read user input from the console.
     */
    public static void showPayrollStaffMenu(Scanner input) {
        while (true) {
            System.out.println("\n============================================");
            System.out.println("          PAYROLL STAFF MENU");
            System.out.println("============================================");
            System.out.println("[1] Process Payroll");
            System.out.println("[2] Exit the program");
            System.out.print("Choice : ");
            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                // Show the payroll sub-menu for selecting one or all employees
                System.out.println("\n[1] One employee");
                System.out.println("[2] All employees");
                System.out.println("[3] Exit the program");
                System.out.print("Choice : ");
                String subChoice = input.nextLine().trim();

                if (subChoice.equals("1")) {
                    // Process payroll for a single employee by their ID
                    System.out.print("Enter Employee Number : ");
                    String employeeId = input.nextLine().trim();
                    generatePayrollReport(employeeId);

                } else if (subChoice.equals("2")) {
                    // Process payroll for every employee found in the CSV file
                    processBatchPayroll();

                } else if (subChoice.equals("3")) {
                    System.out.println("Terminating the program. Goodbye!");
                    break;

                } else {
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                }

            } else if (choice.equals("2")) {
                System.out.println("Terminating the program. Goodbye!");
                break;

            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    // =========================================================================
    // SECTION 3: EMPLOYEE DETAIL DISPLAY (Employee Role)
    // =========================================================================

    /**
     * displayEmployeeDetails() - Shows basic information for a given employee ID.
     *
     * Searches "Employee Details.csv" for the employee whose ID matches the
     * given targetId. If found, displays the employee number, full name, and
     * birthday. If no match is found, a "does not exist" message is shown.
     *
     * This method is used exclusively by the "employee" menu role, where only
     * basic personal details (not payroll data) are accessible.
     *
     * @param targetId  The employee number entered by the user.
     */
    public static void displayEmployeeDetails(String targetId) {
        boolean employeeFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("Employee Details.csv"))) {
            reader.readLine(); // Skip the header row before processing data rows

            String line;
            while ((line = reader.readLine()) != null) {
                // Split by comma while respecting quoted fields that may contain commas
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (columns.length < 4) continue;

                String currentId = cleanField(columns[0]);

                // Check if this row matches the requested employee ID
                if (currentId.equals(targetId)) {
                    // Name is stored as Last Name (col 1), First Name (col 2)
                    String fullName  = cleanField(columns[2]) + " " + cleanField(columns[1]);
                    String birthday  = cleanField(columns[3]);

                    System.out.println("\n============================================");
                    System.out.println("           EMPLOYEE DETAILS");
                    System.out.println("============================================");
                    System.out.println("Employee Number : " + targetId);
                    System.out.println("Employee Name   : " + fullName);
                    System.out.println("Birthday        : " + birthday);
                    System.out.println("============================================");

                    employeeFound = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("System error while reading employee data: " + e.getMessage());
            return;
        }

        // Inform the user if no employee record matched the entered ID
        if (!employeeFound) {
            System.out.println("Employee number does not exist.");
        }
    }

    // =========================================================================
    // SECTION 4: PAYROLL REPORT GENERATION (Payroll Staff Role)
    // =========================================================================

    /**
     * generatePayrollReport() - Generates the full payroll report for one employee.
     *
     * This method first retrieves the employee's information from "Employee Details.csv"
     * (name, birthday, basic salary, and hourly rate). It then loops through each month
     * from June to December and calls printMonthlyCutoffs() to display both cutoff
     * periods for that month.
     *
     * @param targetId  The employee number for whom payroll will be computed.
     */
    public static void generatePayrollReport(String targetId) {
        // Variables to hold the employee's information read from the CSV
        String fullName    = "";
        String birthday    = "";
        double basicSalary = 0;
        double hourlyRate  = 0;
        boolean employeeFound = false;

        // Read employee details from the CSV file
        try (BufferedReader reader = new BufferedReader(new FileReader("Employee Details.csv"))) {
            reader.readLine(); // Skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (columns.length < 19) continue;

                String currentId = cleanField(columns[0]);

                if (currentId.equals(targetId)) {
                    // Build full name from Last Name (col 1) and First Name (col 2)
                    fullName    = cleanField(columns[2]) + " " + cleanField(columns[1]);
                    birthday    = cleanField(columns[3]);
                    basicSalary = parseAmount(cleanField(columns[13])); // Column 13 = Basic Salary
                    hourlyRate  = parseAmount(cleanField(columns[18])); // Column 18 = Hourly Rate
                    employeeFound = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("System error while reading employee data: " + e.getMessage());
            return;
        }

        if (!employeeFound) {
            System.out.println("Employee number does not exist.");
            return;
        }

        // Display the payroll report header with the employee's basic information
        System.out.println("\n============================================");
        System.out.println("            PAYROLL REPORT");
        System.out.println("============================================");
        System.out.println("Employee Number : " + targetId);
        System.out.println("Employee Name   : " + fullName);
        System.out.println("Birthday        : " + birthday);
        System.out.println("============================================");

        // Define the months to process: June (06) through December (12)
        String[] monthCodes = {"06", "07", "08", "09", "10", "11", "12"};
        String[] monthNames = {"June", "July", "August", "September", "October", "November", "December"};

        // Process and display payroll for each month in the range
        for (int monthIndex = 0; monthIndex < monthCodes.length; monthIndex++) {
            printMonthlyCutoffs(targetId, monthCodes[monthIndex], monthNames[monthIndex],
                                hourlyRate, basicSalary);
        }
    }

    // =========================================================================
    // SECTION 5: MONTHLY CUTOFF DISPLAY
    // =========================================================================

    /**
     * printMonthlyCutoffs() - Displays the payroll for both cutoff periods of a month.
     *
     * This method separates the payroll display responsibilities into three steps:
     *   1. Read attendance hours from the CSV (via readAttendanceHours).
     *   2. Compute gross salary and all deductions (via dedicated compute methods).
     *   3. Display the formatted output for each cutoff period.
     *
     * The 1st cutoff (days 1-15) shows gross salary and net salary with no deductions.
     * The 2nd cutoff (days 16-31) shows gross salary, each deduction itemized,
     * total deductions, and the resulting net salary after all deductions.
     *
     * Deduction basis:
     *   - SSS, PhilHealth, and Pag-IBIG are based on the employee's monthly basic salary.
     *   - Withholding tax is computed on the taxable income:
     *     Taxable Income = (Gross Cutoff 1 + Gross Cutoff 2) - (SSS + PhilHealth + Pag-IBIG)
     *   - All deductions are applied only on the 2nd cutoff payout.
     *
     * @param employeeId   The employee's ID number, used to match attendance records.
     * @param monthCode    Two-digit month string (e.g., "06" for June).
     * @param monthName    Full month name for display (e.g., "June").
     * @param hourlyRate   The employee's hourly rate used to compute gross salary.
     * @param basicSalary  The employee's monthly basic salary used as deduction basis.
     */
    public static void printMonthlyCutoffs(String employeeId, String monthCode,
                                           String monthName, double hourlyRate,
                                           double basicSalary) {
        // Step 1: Read attendance data and get hours worked for each cutoff period
        double[] cutoffHours  = readAttendanceHours(employeeId, monthCode);
        double hoursCutoff1   = cutoffHours[0]; // Total hours worked from day 1 to day 15
        double hoursCutoff2   = cutoffHours[1]; // Total hours worked from day 16 to end of month

        // Skip this month entirely if there are no attendance records
        if (hoursCutoff1 == 0 && hoursCutoff2 == 0) {
            return;
        }

        // Step 2: Compute gross salary for each cutoff period
        // Gross salary = total hours worked in that cutoff x hourly rate
        double grossCutoff1 = computeGrossSalary(hoursCutoff1, hourlyRate);
        double grossCutoff2 = computeGrossSalary(hoursCutoff2, hourlyRate);

        // The combined monthly gross is the basis for withholding tax computation
        // Per rule: add 1st and 2nd cutoff amounts first before computing deductions
        double monthlyGross = grossCutoff1 + grossCutoff2;

        // Step 3: Compute government deductions using the provided contribution tables
        // SSS, PhilHealth, and Pag-IBIG are based on the employee's monthly basic salary
        double sssContribution       = computeSSS(basicSalary);
        double philHealthContribution = computePhilHealth(basicSalary);
        double pagIbigContribution    = computePagIbig(basicSalary);

        // Total of the three government contributions (excluding withholding tax)
        double totalGovContributions = sssContribution + philHealthContribution + pagIbigContribution;

        // Taxable income = combined monthly gross minus government contributions
        // Withholding tax is calculated AFTER applying the three deductions above
        double taxableIncome   = monthlyGross - totalGovContributions;
        double withholdingTax  = computeWithholdingTax(taxableIncome);

        // Total deductions = all government contributions + withholding tax
        double totalDeductions = totalGovContributions + withholdingTax;

        // Net salaries:
        //   1st cutoff: no deductions yet; net equals gross
        //   2nd cutoff: gross minus all deductions applied this cutoff
        double netCutoff1 = grossCutoff1;
        double netCutoff2 = grossCutoff2 - totalDeductions;

        // Step 4: Display the 1st cutoff summary (no deductions applied yet)
        System.out.println("\n--------------------------------------------");
        System.out.println("Cutoff Date     : " + monthName + " 1 to " + monthName + " 15");
        System.out.printf( "Total Hours Worked : %.2f hrs%n", hoursCutoff1);
        System.out.printf( "Gross Salary       : PHP %,.2f%n", grossCutoff1);
        System.out.printf( "Net Salary         : PHP %,.2f%n", netCutoff1);

        // Step 5: Display the 2nd cutoff summary with itemized deductions
        System.out.println("\nCutoff Date     : " + monthName + " 16 to " + monthName + " 31");
        System.out.printf( "Total Hours Worked : %.2f hrs%n", hoursCutoff2);
        System.out.printf( "Gross Salary       : PHP %,.2f%n", grossCutoff2);
        System.out.println("  --- Deductions ---");
        System.out.printf( "  SSS              : PHP %,.2f%n", sssContribution);
        System.out.printf( "  PhilHealth       : PHP %,.2f%n", philHealthContribution);
        System.out.printf( "  Pag-IBIG         : PHP %,.2f%n", pagIbigContribution);
        System.out.printf( "  Withholding Tax  : PHP %,.2f%n", withholdingTax);
        System.out.printf( "Total Deductions   : PHP %,.2f%n", totalDeductions);
        System.out.printf( "Net Salary         : PHP %,.2f%n", netCutoff2);
        System.out.println("--------------------------------------------");
    }

    // =========================================================================
    // SECTION 6: ATTENDANCE READING
    // =========================================================================

    /**
     * readAttendanceHours() - Reads and totals work hours per cutoff from attendance records.
     *
     * This method scans "Attendance Record.csv" for all rows matching the given
     * employee ID and month. For each matching row, it calls calculateWorkHours()
     * to determine the hours worked that day, then adds the result to either the
     * 1st cutoff (days 1-15) or 2nd cutoff (days 16 and above) total.
     *
     * The method supports two date formats in the CSV:
     *   - "MM/DD/YYYY" (e.g., "06/15/2024")
     *   - "YYYY-MM-DD" (e.g., "2024-06-15")
     *
     * @param employeeId  The employee ID to match in the attendance records.
     * @param monthCode   Two-digit month string (e.g., "06" for June).
     * @return            A double array where index 0 = hours for cutoff 1,
     *                    and index 1 = hours for cutoff 2.
     */
    public static double[] readAttendanceHours(String employeeId, String monthCode) {
        double hoursCutoff1 = 0; // Accumulates hours for days 1 to 15
        double hoursCutoff2 = 0; // Accumulates hours for days 16 to end of month
        int targetMonth     = Integer.parseInt(monthCode); // Convert "06" to 6 for comparison

        try (BufferedReader reader = new BufferedReader(new FileReader("Attendance Record.csv"))) {
            reader.readLine(); // Skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 6) continue; // Skip rows that are missing required fields

                String currentId = cleanField(columns[0]);

                // Only process rows that belong to the target employee
                if (!currentId.equals(employeeId)) continue;

                // Parse the work date from column 3
                String workDate    = cleanField(columns[3]);
                int[]  dateParts   = parseDate(workDate);
                if (dateParts == null) continue; // Skip rows with unrecognized date format

                int recordMonth = dateParts[0];
                int recordDay   = dateParts[1];

                // Only process records that fall in the target month
                if (recordMonth != targetMonth) continue;

                // Calculate the hours worked for this specific day
                String timeIn      = cleanField(columns[4]);
                String timeOut     = cleanField(columns[5]);
                double dailyHours  = calculateWorkHours(timeIn, timeOut);

                // Add daily hours to the correct cutoff period based on the day of month
                if (recordDay <= 15) {
                    hoursCutoff1 += dailyHours;
                } else {
                    hoursCutoff2 += dailyHours;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance records: " + e.getMessage());
        }

        // Return hours for both cutoff periods as a two-element array
        return new double[]{hoursCutoff1, hoursCutoff2};
    }

    // =========================================================================
    // SECTION 7: WORK HOURS CALCULATION
    // =========================================================================

    /**
     * calculateWorkHours() - Computes the number of valid work hours for one day.
     *
     * This method enforces the following rules for counting work hours:
     *
     *   GRACE PERIOD:
     *     Employees who log in at or before 8:10 AM are treated as having logged
     *     in at exactly 8:00 AM. They are not penalized for being slightly late.
     *     Employees who log in after 8:10 AM are counted from their actual time in.
     *
     *   TIME CAP:
     *     Work hours are only counted between 8:00 AM and 5:00 PM.
     *     Any time logged after 5:00 PM is not counted (no overtime).
     *
     *   LUNCH BREAK:
     *     A 1-hour unpaid lunch break is always deducted from the total.
     *
     * Example scenarios (from professor's instructions):
     *   - Log in 8:30 AM, log out 5:30 PM:
     *     After 8:10 grace → actual in = 8:30. Out capped at 5:00.
     *     (5:00 - 8:30) - 1 hr lunch = 7.5 hrs ✓
     *   - Log in 8:05 AM, log out 5:00 PM:
     *     Within grace → treated as 8:00. Out = 5:00.
     *     (5:00 - 8:00) - 1 hr lunch = 8.0 hrs ✓
     *   - Log in 8:05 AM, log out 4:30 PM:
     *     Within grace → treated as 8:00. Out = 4:30 (before 5 PM, no cap).
     *     (4:30 - 8:00) - 1 hr lunch = 7.5 hrs ✓
     *
     * @param timeIn   The employee's log-in time as a "HH:MM" string.
     * @param timeOut  The employee's log-out time as a "HH:MM" string.
     * @return         The number of valid work hours for the day,
     *                 or 0 if the time values are invalid or result in negative hours.
     */
    public static double calculateWorkHours(String timeIn, String timeOut) {
        try {
            // Parse time-in into hours and minutes, then convert to decimal hours
            String[] inParts       = timeIn.split(":");
            double timeInHours     = Integer.parseInt(inParts[0]) + Integer.parseInt(inParts[1]) / 60.0;

            // Parse time-out into hours and minutes, then convert to decimal hours
            String[] outParts      = timeOut.split(":");
            double timeOutHours    = Integer.parseInt(outParts[0]) + Integer.parseInt(outParts[1]) / 60.0;

            // Define the official work window boundaries
            double officialStart   = 8.0;                  // 8:00 AM in decimal hours
            double officialEnd     = 17.0;                 // 5:00 PM in decimal hours
            double gracePeriodEnd  = 8.0 + (10.0 / 60.0); // 8:10 AM in decimal hours

            // Apply grace period: treat any log-in at or before 8:10 AM as 8:00 AM
            if (timeInHours <= gracePeriodEnd) {
                timeInHours = officialStart;
            }
            // If the employee logged in after 8:10 AM, their actual log-in time is used as-is

            // Cap the log-out time at 5:00 PM; hours beyond this are not counted
            if (timeOutHours > officialEnd) {
                timeOutHours = officialEnd;
            }

            // Compute total time span between effective log-in and capped log-out
            double totalHours = timeOutHours - timeInHours;

            // Deduct the mandatory 1-hour unpaid lunch break
            double hoursWorked = totalHours - 1.0;

            // Return 0 if the result is negative (e.g., employee left very early)
            return Math.max(0, hoursWorked);

        } catch (Exception e) {
            // Return 0 if the time strings cannot be parsed (invalid or missing data)
            return 0;
        }
    }

    // =========================================================================
    // SECTION 8: SALARY AND DEDUCTION COMPUTATIONS
    // =========================================================================

    /**
     * computeGrossSalary() - Calculates gross salary for one cutoff period.
     *
     * Gross salary is the product of total hours worked and the employee's
     * hourly rate. No rounding is applied; the exact decimal result is returned.
     *
     * Formula: Gross Salary = Hours Worked x Hourly Rate
     *
     * @param hoursWorked  Total hours worked within the cutoff period.
     * @param hourlyRate   The employee's hourly rate from the employee CSV.
     * @return             The computed gross salary for that cutoff period.
     */
    public static double computeGrossSalary(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }

    /**
     * computeSSS() - Computes the employee's SSS contribution for the month.
     *
     * The SSS contribution is determined by looking up the employee's monthly
     * basic salary in a bracketed contribution table. The table increments by
     * PHP 500 per bracket, with each bracket's contribution increasing by PHP 22.50.
     *
     * Contribution table (from provided SSS table image):
     *   Basic Salary Range          | Employee Contribution
     *   Below PHP 3,250             | PHP 135.00
     *   PHP 3,250 – PHP 3,749.99    | PHP 157.50
     *   PHP 3,750 – PHP 4,249.99    | PHP 180.00
     *   PHP 4,250 – PHP 4,749.99    | PHP 202.50
     *   PHP 4,750 – PHP 5,249.99    | PHP 225.00
     *   ...  (increments of PHP 22.50 per PHP 500 bracket)
     *   PHP 24,750 and above        | PHP 1,125.00
     *
     * @param basicSalary  The employee's monthly basic salary.
     * @return             The employee's SSS contribution amount.
     */
    public static double computeSSS(double basicSalary) {
        // Minimum contribution for salaries below the first bracket
        if (basicSalary < 3250) {
            return 135.00;
        }

        // Maximum contribution for salaries at the top bracket and above
        if (basicSalary >= 24750) {
            return 1125.00;
        }

        // Iterate through salary brackets in PHP 500 increments to find the correct bracket
        // Starting bracket: PHP 3,250 to PHP 3,749.99 → contribution: PHP 157.50
        double bracketFloor       = 3250;
        double bracketCeiling     = 3750;
        double bracketContribution = 157.50;

        while (bracketCeiling <= 24750) {
            // Check if the salary falls within the current bracket range
            if (basicSalary >= bracketFloor && basicSalary < bracketCeiling) {
                return bracketContribution;
            }
            // Move to the next bracket by shifting both bounds and increasing contribution
            bracketFloor       += 500;
            bracketCeiling     += 500;
            bracketContribution += 22.50;
        }

        return bracketContribution;
    }

    /**
     * computePhilHealth() - Computes the employee's PhilHealth premium contribution.
     *
     * PhilHealth uses a flat 3% premium rate applied to the employee's monthly
     * basic salary. The total premium is split equally between the employee and
     * the employer, so the employee pays 50% of the total computed premium.
     *
     * Premium table (from provided PhilHealth table image):
     *   Monthly Basic Salary         | Premium Rate | Monthly Premium
     *   Below PHP 10,000             | 3%           | PHP 300.00 (minimum)
     *   PHP 10,000.01 to PHP 59,999.99 | 3%         | PHP 300.00 up to PHP 1,800.00
     *   PHP 60,000 and above         | 3%           | PHP 1,800.00 (maximum)
     *
     * Employee share = 50% of the computed monthly premium.
     *
     * @param basicSalary  The employee's monthly basic salary.
     * @return             The employee's share of the PhilHealth monthly premium.
     */
    public static double computePhilHealth(double basicSalary) {
        double monthlyPremium;

        if (basicSalary < 10000) {
            // Below the minimum salary floor: apply the minimum flat premium
            monthlyPremium = 300.00;

        } else if (basicSalary <= 59999.99) {
            // Within the standard range: compute 3% of the basic salary
            monthlyPremium = basicSalary * 0.03;

        } else {
            // At or above the ceiling: apply the maximum flat premium
            monthlyPremium = 1800.00;
        }

        // The employee pays only 50% of the total monthly premium
        return monthlyPremium / 2;
    }

    /**
     * computePagIbig() - Computes the employee's Pag-IBIG (HDMF) monthly contribution.
     *
     * The contribution rate depends on the employee's monthly basic salary:
     *   - PHP 1,000 to PHP 1,500 : Employee rate = 1% of basic salary
     *   - Above PHP 1,500         : Employee rate = 2% of basic salary
     *
     * The maximum employee contribution is capped at PHP 100.00, regardless of
     * how large the computed amount would be.
     *
     * Contribution table (from provided Pag-IBIG table image):
     *   Monthly Basic Salary       | Employee Rate | Employer Rate | Total
     *   PHP 1,000 – PHP 1,500      | 1%            | 2%            | 3%
     *   Above PHP 1,500            | 2%            | 2%            | 4%
     *   NOTE: Maximum employee contribution is PHP 100.00
     *
     * @param basicSalary  The employee's monthly basic salary.
     * @return             The employee's Pag-IBIG contribution, capped at PHP 100.
     */
    public static double computePagIbig(double basicSalary) {
        double employeeRate;

        if (basicSalary >= 1000 && basicSalary <= 1500) {
            // Lower salary bracket: employee contributes 1% of basic salary
            employeeRate = 0.01;
        } else {
            // Higher salary bracket (above PHP 1,500): employee contributes 2%
            employeeRate = 0.02;
        }

        double contribution = basicSalary * employeeRate;

        // Cap the employee's contribution at PHP 100.00 per the Pag-IBIG table rules
        return Math.min(contribution, 100.00);
    }

    /**
     * computeWithholdingTax() - Computes the monthly withholding tax on taxable income.
     *
     * Withholding tax is computed AFTER subtracting SSS, PhilHealth, and Pag-IBIG
     * from the combined monthly gross salary. The resulting taxable income is then
     * matched to the correct bracket in the BIR withholding tax table.
     *
     * Withholding tax table (from provided tax table image):
     *   Taxable Income (Monthly)    | Tax Formula
     *   PHP 20,832 and below        | No tax (PHP 0)
     *   PHP 20,833 to below 33,333  | 20% of excess over PHP 20,833
     *   PHP 33,333 to below 66,667  | PHP 2,500 + 25% of excess over PHP 33,333
     *   PHP 66,667 to below 166,667 | PHP 10,833 + 30% of excess over PHP 66,667
     *   PHP 166,667 to below 666,667| PHP 40,833.33 + 32% of excess over PHP 166,667
     *   PHP 666,667 and above       | PHP 200,833.33 + 35% of excess over PHP 666,667
     *
     * Verification using professor's sample (from Image 1):
     *   Monthly Salary   = PHP 25,000
     *   SSS              = PHP 1,125
     *   PhilHealth       = PHP 375
     *   Pag-IBIG         = PHP 100
     *   Total Deductions = PHP 1,600
     *   Taxable Income   = PHP 25,000 - PHP 1,600 = PHP 23,400
     *   Tax              = (PHP 23,400 - PHP 20,833) x 20% = PHP 513.40 ✓
     *
     * @param taxableIncome  Monthly gross salary minus total government contributions.
     * @return               The computed withholding tax amount.
     */
    public static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) {
            // 1st bracket: income is within the tax-exempt threshold; no tax is due
            return 0;

        } else if (taxableIncome < 33333) {
            // 2nd bracket: 20% of the amount exceeding PHP 20,833
            return (taxableIncome - 20833) * 0.20;

        } else if (taxableIncome < 66667) {
            // 3rd bracket: PHP 2,500 fixed amount plus 25% of excess over PHP 33,333
            return 2500 + (taxableIncome - 33333) * 0.25;

        } else if (taxableIncome < 166667) {
            // 4th bracket: PHP 10,833 fixed amount plus 30% of excess over PHP 66,667
            return 10833 + (taxableIncome - 66667) * 0.30;

        } else if (taxableIncome < 666667) {
            // 5th bracket: PHP 40,833.33 fixed amount plus 32% of excess over PHP 166,667
            return 40833.33 + (taxableIncome - 166667) * 0.32;

        } else {
            // 6th bracket: PHP 200,833.33 fixed amount plus 35% of excess over PHP 666,667
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    // =========================================================================
    // SECTION 9: BATCH PAYROLL PROCESSING
    // =========================================================================

    /**
     * processBatchPayroll() - Generates payroll reports for all employees in the CSV.
     *
     * This method reads every employee record from "Employee Details.csv" and
     * calls generatePayrollReport() for each employee found. It produces a
     * complete company-wide payroll report covering June through December.
     *
     * Rows with empty or unreadable employee IDs are skipped automatically.
     */
    public static void processBatchPayroll() {
        System.out.println("\nGenerating payroll report for all employees...");

        try (BufferedReader reader = new BufferedReader(new FileReader("Employee Details.csv"))) {
            reader.readLine(); // Skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                // Split the row and extract only the employee ID (first column)
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (columns.length < 1) continue;

                String employeeId = cleanField(columns[0]);

                // Skip any rows where the employee ID is missing or blank
                if (employeeId.isEmpty()) continue;

                generatePayrollReport(employeeId);
            }
        } catch (Exception e) {
            System.out.println("Error during batch payroll processing: " + e.getMessage());
        }
    }

    // =========================================================================
    // SECTION 10: UTILITY / HELPER METHODS
    // =========================================================================

    /**
     * cleanField() - Strips quotes and whitespace from a raw CSV field value.
     *
     * CSV files often wrap field values in double-quote characters, especially
     * when a field contains a comma or other special character. This method
     * removes those quotes and trims any surrounding whitespace to give a
     * clean, usable string value.
     *
     * Example: "  \"Santos\"  " → "Santos"
     *
     * @param rawField  The unprocessed field string extracted from a CSV line.
     * @return          The cleaned and trimmed string value.
     */
    public static String cleanField(String rawField) {
        return rawField.trim().replace("\"", "");
    }

    /**
     * parseAmount() - Converts a formatted number string into a double value.
     *
     * Philippine payroll files commonly format large numbers with comma separators
     * (e.g., "25,000.00"). This method removes those commas before parsing so
     * that the value can be used in arithmetic calculations.
     *
     * Returns 0 if the input is null, empty, or cannot be converted to a number.
     *
     * Example: "25,000.00" → 25000.0
     *
     * @param amountString  The number string to parse, possibly containing commas.
     * @return              The parsed double value, or 0 if parsing fails.
     */
    public static double parseAmount(String amountString) {
        try {
            return Double.parseDouble(amountString.replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * parseDate() - Extracts the month and day from a date string.
     *
     * This method handles the two date formats commonly found in Philippine
     * payroll CSV files:
     *   - "MM/DD/YYYY" → returns {month, day}  (e.g., "06/15/2024" → {6, 15})
     *   - "YYYY-MM-DD" → returns {month, day}  (e.g., "2024-06-15" → {6, 15})
     *
     * Returns null if the format cannot be recognized or the string cannot be
     * parsed. The caller should check for null before using the result.
     *
     * @param dateString  The date string to parse from the attendance CSV.
     * @return            An int array {month, day}, or null if parsing fails.
     */
    public static int[] parseDate(String dateString) {
        try {
            if (dateString.contains("/")) {
                // Format detected: MM/DD/YYYY — month is the first segment
                String[] dateParts = dateString.split("/");
                int month = Integer.parseInt(dateParts[0]);
                int day   = Integer.parseInt(dateParts[1]);
                return new int[]{month, day};

            } else if (dateString.contains("-")) {
                // Format detected: YYYY-MM-DD — month is the second segment
                String[] dateParts = dateString.split("-");
                int month = Integer.parseInt(dateParts[1]);
                int day   = Integer.parseInt(dateParts[2]);
                return new int[]{month, day};
            }
        } catch (Exception e) {
            // Return null to signal that this date record should be skipped
        }

        return null; // Date format was unrecognized or could not be parsed
    }
}
