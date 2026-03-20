import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // System Login
        System.out.println("========================================");
        System.out.println("       MOTORPH PORTAL LOGIN");
        System.out.println("========================================");
        System.out.print("Username: ");
        String user = input.nextLine();
        System.out.print("Password: ");
        String pass = input.nextLine();

        // Credential Validation
        if (!((user.equals("employee") || user.equals("payroll_staff")) && pass.equals("12345"))) {
            System.out.println("Incorrect username and/or password.");
            System.out.println("Terminating program...");
            return;
        }

        System.out.println("\nLogin Successful!");

        // Menu routing based on user type
        if (user.equals("employee")) {
            showEmployeeMenu(input);
        } else {
            showStaffMenu(input);
        }
    }

    public static void showEmployeeMenu(Scanner input) {
        while (true) {
            System.out.println("\n[1] Enter your employee number");
            System.out.println("[2] Exit the program");
            System.out.print("Choice: ");
            String choice = input.nextLine();

            if (choice.equals("2")) {
                System.out.println("Terminating the program.");
                break;
            } else if (choice.equals("1")) {
                System.out.print("Enter Employee Number: ");
                String id = input.nextLine().trim();
                generatePayrollReport(id);
            }
        }
    }

    public static void showStaffMenu(Scanner input) {
        while (true) {
            System.out.println("\n[1] Process Payroll");
            System.out.println("[2] Exit the program");
            System.out.print("Choice: ");
            String choice = input.nextLine();

            if (choice.equals("2")) break;
            if (choice.equals("1")) {
                System.out.println("\n[1] One employee");
                System.out.println("[2] All employees");
                System.out.println("[3] Exit the program");
                System.out.print("Choice: ");
                String subChoice = input.nextLine();
                
                if (subChoice.equals("1")) {
                    System.out.print("Enter ID: ");
                    generatePayrollReport(input.nextLine().trim());
                } else if (subChoice.equals("2")) {
                    processBatchPayroll();
                } else if (subChoice.equals("3")) {
                    break;
                }
            }
        }
    }

    public static void generatePayrollReport(String targetId) {
        String empName = "", bday = "";
        double rate = 0, basicSalary = 0;
        boolean isFound = false;

        File csvFile = new File("Employee Details.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String currentId = data[0].trim().replace("\"", "");
                
                if (currentId.equals(targetId)) {
                    empName = data[2].trim().replace("\"", "") + " " + data[1].trim().replace("\"", "");
                    bday = data[3].trim().replace("\"", "");
                    
                    // Parse numeric data and handle potential formatting issues
                    String rawBasic = data[13].trim().replace("\"", "").replace(",", "");
                    String rawHourly = data[18].trim().replace("\"", "").replace(",", "");
                    
                    basicSalary = Double.parseDouble(rawBasic); 
                    rate = Double.parseDouble(rawHourly);
                    isFound = true;
                    break;
                }
            }
        } catch (Exception e) { 
            System.out.println("System Error reading employee data: " + e.getMessage()); 
            return;
        }

        if (!isFound) {
            System.out.println("Employee number " + targetId + " does not exist.");
            return;
        }

        System.out.println("\n==========================================");
        System.out.println("EMPLOYEE DETAILS");
        System.out.println("Employee Number: " + targetId);
        System.out.println("Employee Name:   " + empName);
        System.out.println("Birthday:        " + bday);
        System.out.println("==========================================");

        // Process records from June to December
        String[] monthCodes = {"06", "07", "08", "09", "10", "11", "12"};
        String[] monthNames = {"June", "July", "August", "September", "October", "November", "December"};

        for (int i = 0; i < monthCodes.length; i++) {
            printMonthlyCutoffs(targetId, monthCodes[i], monthNames[i], rate, basicSalary);
        }
    }

    public static void printMonthlyCutoffs(String id, String month, String name, double rate, double basic) {
        double hoursCutoff1 = 0, hoursCutoff2 = 0;
        boolean attendanceFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("Attendance Record.csv"))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 4) continue;
                String currentId = data[0].trim().replace("\"", "");
                String workDate = data[3].trim().replace("\"", "");
                
                if (currentId.equals(id) && (workDate.contains("-" + month + "-") || workDate.startsWith(month + "/") || workDate.startsWith(Integer.parseInt(month) + "/"))) {
                    double dailyHours = calculateWorkHours(data[4].trim().replace("\"", ""), data[5].trim().replace("\"", ""));
                    
                    int dayValue = 0;
                    if (workDate.contains("-")) dayValue = Integer.parseInt(workDate.split("-")[2]);
                    else if (workDate.contains("/")) dayValue = Integer.parseInt(workDate.split("/")[1]);

                    if (dayValue <= 15) hoursCutoff1 += dailyHours; else hoursCutoff2 += dailyHours;
                    attendanceFound = true;
                }
            }
        } catch (Exception e) { return; }

        if (attendanceFound) {
            double gross1 = hoursCutoff1 * rate;
            double gross2 = hoursCutoff2 * rate;
            
            // First Cutoff display
            System.out.println("\nCutoff Date: " + name + " 1 to " + name + " 15");
            System.out.printf("Total Hours Worked: %.2f\n", hoursCutoff1);
            System.out.printf("Gross Salary:       PHP %,.2f\n", gross1);
            System.out.printf("Net Salary:         PHP %,.2f\n", gross1);

            // Deductions logic
            double sss = computeSSS(basic);
            double philhealth = (basic * 0.03) / 2;
            double pagibig = computePagIbig(basic);
            double govTotal = sss + philhealth + pagibig;
            
            double taxable = (gross1 + gross2) - govTotal;
            double tax = computeTax(taxable);
            double totalDeductions = govTotal + tax;

            // Second Cutoff display (Net includes all deductions)
            System.out.println("\nCutoff Date: " + name + " 16 to " + name + " 31");
            System.out.printf("Total Hours Worked: %.2f\n", hoursCutoff2);
            System.out.printf("Gross Salary:       PHP %,.2f\n", gross2);
            System.out.println("--- Deductions ---");
            System.out.printf("SSS:                PHP %,.2f\n", sss);
            System.out.printf("Philhealth:         PHP %,.2f\n", philhealth);
            System.out.printf("Pag-ibig:           PHP %,.2f\n", pagibig);
            System.out.printf("Tax:                PHP %,.2f\n", tax);
            System.out.printf("Total Deductions:   PHP %,.2f\n", totalDeductions);
            System.out.printf("Net Salary:         PHP %,.2f\n", (gross2 - totalDeductions));
            System.out.println("------------------------------------------");
        }
    }

    public static double computeSSS(double basic) {
        if (basic < 3250) return 135.00;
        if (basic >= 24750) return 1125.00;
        
        double low = 3250, high = 3750, contribution = 157.50;
        while (high <= 24750) {
            if (basic >= low && basic < high) return contribution;
            low += 500; high += 500; contribution += 22.50;
        }
        return contribution;
    }

    public static double computePagIbig(double basic) {
        double rate = (basic > 1500) ? 0.02 : 0.01;
        double contribution = basic * rate;
        return Math.min(contribution, 100.00); 
    }

    public static double computeTax(double taxable) {
        if (taxable <= 20832) return 0;
        if (taxable < 33333) return (taxable - 20833) * 0.20;
        if (taxable < 66667) return 2500 + (taxable - 33333) * 0.25;
        if (taxable < 166667) return 10833 + (taxable - 66667) * 0.30;
        if (taxable < 666667) return 40833.33 + (taxable - 166667) * 0.32;
        return 200833.33 + (taxable - 666667) * 0.35;
    }

    public static double calculateWorkHours(String timeIn, String timeOut) {
        try {
            String[] in = timeIn.split(":"), out = timeOut.split(":");
            double h1 = Integer.parseInt(in[0]) + Integer.parseInt(in[1])/60.0;
            double h2 = Integer.parseInt(out[0]) + Integer.parseInt(out[1])/60.0;
            return Math.max(0, (h2 - h1) - 1.0); 
        } catch (Exception e) { return 0; }
    }

    public static void processBatchPayroll() {
        try (BufferedReader reader = new BufferedReader(new FileReader("Employee Details.csv"))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String id = line.split(",")[0].trim().replace("\"", "");
                generatePayrollReport(id);
            }
        } catch (Exception e) { System.out.println("Error processing payroll batch."); }
    }
}
