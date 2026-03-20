package motorphpayroll;

// Final submission ready

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class MotorPHPayroll {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "employee_data.csv"; 

        // --- LOGIN SYSTEM ---
        System.out.println("=== MotorPH Basic Payroll System ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Validate Credentials
        if (!password.equals("12345") || (!username.equals("employee") && !username.equals("payroll_staff"))) {
            System.out.println("Incorrect username and/or password.");
            System.out.println("Terminating the program.");
            System.exit(0); 
        }

        // --- MAIN MENUS ---
        if (username.equals("employee")) {
            while (true) {
                System.out.println("\n--- Employee Menu ---");
                System.out.println("1. Enter your employee number");
                System.out.println("2. Exit the program");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine();

                if (choice.equals("1")) {
                    System.out.print("Enter your employee number: ");
                    String empNo = scanner.nextLine();
                    displayBasicEmployeeInfo(empNo, filePath);
                } else if (choice.equals("2")) {
                    System.out.println("Terminating program...");
                    System.exit(0);
                } else {
                    System.out.println("Invalid option.");
                }
            }
        } else if (username.equals("payroll_staff")) {
            while (true) {
                System.out.println("\n--- Payroll Staff Menu ---");
                System.out.println("1. Process Payroll");
                System.out.println("2. Exit the program");
                System.out.print("Choose an option: ");
                String staffChoice = scanner.nextLine();

                if (staffChoice.equals("1")) {
                    System.out.println("\n--- Process Payroll ---");
                    System.out.println("1. One employee");
                    System.out.println("2. All employees");
                    System.out.println("3. Exit the program");
                    System.out.print("Choose a sub-option: ");
                    String subChoice = scanner.nextLine();
                    
                    if (subChoice.equals("1")) {
                         System.out.print("Enter the employee number: ");
                         String empNoToProcess = scanner.nextLine();
                         processPayroll(empNoToProcess, filePath, false);
                    } else if (subChoice.equals("2")) {
                         processPayroll(null, filePath, true); // Process all
                    } else if (subChoice.equals("3")) {
                         System.out.println("Terminating program...");
                         System.exit(0);
                    } else {
                         System.out.println("Invalid option.");
                    }
                } else if (staffChoice.equals("2")) {
                    System.out.println("Terminating program...");
                    System.exit(0);
                } else {
                    System.out.println("Invalid option.");
                }
            }
        }
        scanner.close();
    }

    // --- HELPER METHODS FOR CSV READING AND EXACT FORMATTING ---
    public static void displayBasicEmployeeInfo(String targetEmpNo, String filePath) {
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); 
                
                String empId = data[0].replace("\"", "").trim();
                if (empId.equals(targetEmpNo)) {
                    System.out.println("\nEmployee Number: " + empId);
                    System.out.println("Employee Name: " + data[1].replace("\"", "").trim());
                    System.out.println("Birthday: " + data[2].replace("\"", "").trim());
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading the file.");
        }
        if (!found) System.out.println("Employee number does not exist.");
    }

    public static void processPayroll(String targetEmpNo, String filePath, boolean processAll) {
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); 
                
                try {
                    String empId = data[0].replace("\"", "").trim();
                    
                    // If processing one employee and this isn't them, skip
                    if (!processAll && !empId.equals(targetEmpNo)) continue;
                    found = true;

                    String empName = data[1].replace("\"", "").trim();
                    String birthday = data[2].replace("\"", "").trim();
                    
                    // IMPORTANT: You may need to change these numbers (3, 4, 5) if the original file has them in different columns!
                    double hourlyRate = Double.parseDouble(data[3].replace("\"", "").trim());
                    String timeIn = data[4].replace("\"", "").trim(); 
                    String timeOut = data[5].replace("\"", "").trim(); 
                    
                    double hoursWorked = calculateHoursWorked(timeIn, timeOut);
                    double firstCutoffSalary = hoursWorked * hourlyRate; 
                    double secondCutoffSalary = hoursWorked * hourlyRate; 
                    double monthlyGrossSalary = firstCutoffSalary + secondCutoffSalary;
                    
                    double sss = calculateSSS(monthlyGrossSalary);
                    double philHealth = calculatePhilHealth(monthlyGrossSalary);
                    double pagIbig = calculatePagIBIG(monthlyGrossSalary);
                    double tax = calculateTax(monthlyGrossSalary, sss, philHealth, pagIbig);
                    double totalDeductions = sss + philHealth + pagIbig + tax;
                    
                    double finalSecondCutoffSalary = secondCutoffSalary - totalDeductions;
                    
                    // EXACT PROFESSOR FORMAT:
                    System.out.println("\nEmployee #: " + empId);
                    System.out.println("Employee Name: " + empName);
                    System.out.println("Birthday: " + birthday);
                    
                    System.out.println("Cutoff Date: June 1 to June 15");
                    System.out.println("Total Hours Worked: " + hoursWorked);
                    System.out.println("Gross Salary: " + firstCutoffSalary);
                    System.out.println("Net Salary: " + firstCutoffSalary); // No deductions on 1st cutoff
                    
                    System.out.println("\nCutoff Date: June 16 to June 30 (Second payout includes all deductions)");
                    System.out.println("Total Hours Worked: " + hoursWorked);
                    System.out.println("Gross Salary: " + secondCutoffSalary);
                    System.out.println("Each Deduction:");
                    System.out.println("  SSS: " + sss);
                    System.out.println("  PhilHealth: " + philHealth);
                    System.out.println("  Pag-IBIG: " + pagIbig);
                    System.out.println("  Tax: " + tax);
                    System.out.println("Total Deductions: " + totalDeductions);
                    System.out.println("Net Salary: " + finalSecondCutoffSalary);
                    System.out.println("-------------------------------------------------");
                    
                } catch (Exception e) {
                    // Skips messy rows silently without crashing the whole program
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading the file.");
        }
        if (!found && !processAll) System.out.println("Employee number does not exist.");
    }

    // --- YOUR WORKING MATH METHODS STAY HERE ---
    public static double calculateHoursWorked(String timeIn, String timeOut) {
        if (timeIn.isEmpty() || timeOut.isEmpty()) return 0.0;
        String[] inParts = timeIn.split(":");
        int inHour = Integer.parseInt(inParts[0]);
        int inMin = Integer.parseInt(inParts[1]);
        
        String[] outParts = timeOut.split(":");
        int outHour = Integer.parseInt(outParts[0]);
        int outMin = Integer.parseInt(outParts[1]);
        
        if (inHour == 8 && inMin <= 10) inMin = 0;
        if (outHour >= 17) {
            outHour = 17;
            outMin = 0;
        }
        
        int totalInMinutes = (inHour * 60) + inMin;
        int totalOutMinutes = (outHour * 60) + outMin;
        int elapsedMinutes = totalOutMinutes - totalInMinutes;
        
        if (elapsedMinutes > 240) elapsedMinutes -= 60;
        return elapsedMinutes / 60.0; 
    }

    public static double calculateSSS(double monthlyGross) { return monthlyGross * 0.045; }
    public static double calculatePhilHealth(double monthlyGross) { return monthlyGross * 0.02; }
    public static double calculatePagIBIG(double monthlyGross) {
        double contribution = monthlyGross * 0.02;
        return (contribution > 100) ? 100.0 : contribution; 
    }
    public static double calculateTax(double monthlyGross, double sss, double philHealth, double pagIbig) {
        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        if (taxableIncome <= 20833) return 0;
        return (taxableIncome - 20833) * 0.20;
    }
}