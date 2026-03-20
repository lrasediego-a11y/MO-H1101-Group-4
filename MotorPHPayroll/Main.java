import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * MotorPH Payroll System - Milestone 2
 * Description: Automated payroll system with Login, CSV data retrieval, 
 * and comprehensive salary/deduction calculations.
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- STEP 1: LOGIN SYSTEM ---
        System.out.println("========================================");
        System.out.println("       MOTORPH PORTAL LOGIN");
        System.out.println("========================================");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        if ((username.equals("payroll_staff") || username.equals("employee")) && password.equals("12345")) {
            System.out.println("\nLogin Successful! Access Granted.");
        } else {
            System.out.println("\nAccess Denied. Invalid Credentials.");
            return;
        }

        // --- STEP 2: SELECT EMPLOYEE (With Exit Option) ---
        while (true) {
            System.out.print("\nEnter Employee ID (or type 'exit' to terminate): ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Terminating program... Goodbye!");
                break; // Exits the loop and ends the program
            }

            processEmployeePayroll(input);
        }
    }

    public static void processEmployeePayroll(String targetId) {
        String name = "";
        double hourlyRate = 0;
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader("Employee Details.csv"))) {
            String line = br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data[0].trim().equals(targetId)) {
                    name = data[2].trim() + " " + data[1].trim();
                    hourlyRate = Double.parseDouble(data[18].trim());
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: File 'Employee Details.csv' not found.");
            return;
        }

        if (!found) {
            System.out.println("Employee ID " + targetId + " not found. Please try again.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("PAYROLL REPORT: " + name);
        System.out.println("Hourly Rate: " + hourlyRate);
        System.out.println("========================================\n");

        String[] months = {"06", "07", "08", "09", "10", "11", "12"};
        String[] monthNames = {"JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};

        for (int i = 0; i < months.length; i++) {
            calculateMonthly(targetId, months[i], monthNames[i], hourlyRate);
        }
    }

    public static void calculateMonthly(String id, String mNum, String mName, double rate) {
        double hours1 = 0, hours2 = 0;
        boolean foundAny = false;

        try (BufferedReader br = new BufferedReader(new FileReader("Attendance Record.csv"))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String date = data[3].trim();
                if (data[0].trim().equals(id) && (date.contains("-" + mNum + "-") || date.startsWith(mNum + "/"))) {
                    double dayHours = computeHours(data[4].trim(), data[5].trim());
                    int day = Integer.parseInt(date.contains("-") ? date.split("-")[2] : date.split("/")[1]);
                    
                    if (day <= 15) hours1 += dayHours;
                    else hours2 += dayHours;
                    foundAny = true;
                }
            }
        } catch (IOException e) { return; }

        if (foundAny) {
            double gross1 = hours1 * rate;
            double gross2 = hours2 * rate;
            double totalMonthlyGross = gross1 + gross2;

            double sss = getSSS(totalMonthlyGross);
            double ph = totalMonthlyGross * 0.025;
            double pi = (totalMonthlyGross > 1500) ? 100.0 : totalMonthlyGross * 0.01;
            
            double taxableIncome = totalMonthlyGross - (sss + ph + pi);
            double tax = getWithholdingTax(taxableIncome);
            double totalDeductions = sss + ph + pi + tax;

            System.out.println("MONTH: " + mName);
            System.out.printf("  1st Cutoff Gross: PHP %,10.2f\n", gross1);
            System.out.printf("  2nd Cutoff Net:   PHP %,10.2f (Deductions: %,.2f)\n", (gross2 - totalDeductions), totalDeductions);
            System.out.println("----------------------------------------");
        }
    }

    public static double computeHours(String in, String out) {
        try {
            String[] tIn = in.split(":"), tOut = out.split(":");
            double h1 = Integer.parseInt(tIn[0]) + Integer.parseInt(tIn[1])/60.0;
            double h2 = Integer.parseInt(tOut[0]) + Integer.parseInt(tOut[1])/60.0;
            if (h1 <= 8.167) h1 = 8.0; 
            if (h2 > 17.0) h2 = 17.0;
            return Math.max(0, (h2 - h1) - 1.0);
        } catch (Exception e) { return 0; }
    }

    public static double getSSS(double gross) {
        if (gross <= 3250) return 135.0;
        if (gross >= 24750) return 1125.0;
        return 135.0 + (Math.floor((gross - 3250) / 500) * 22.5);
    }

    public static double getWithholdingTax(double taxable) {
        if (taxable <= 20833) return 0;
        if (taxable <= 33332) return (taxable - 20833) * 0.20;
        if (taxable <= 66666) return 2500 + (taxable - 33333) * 0.25;
        if (taxable <= 166666) return 10833.33 + (taxable - 66667) * 0.30;
        if (taxable <= 666666) return 40833.33 + (taxable - 166667) * 0.32;
        return 200833.33 + (taxable - 666667) * 0.35;
    }
}