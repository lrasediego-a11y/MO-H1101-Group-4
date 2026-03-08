package motorphpayroll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MotorPHPayroll {

    public static void main(String[] args) {
        // Task 10: Read Employee Data from a Text/CSV File
        String filePath = "employee_data.csv"; // Make sure this file is in your project folder
        String line;
        
        System.out.println("--- MotorPH Payroll System (June to December) ---");
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip the header row in CSV
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // Assuming comma-separated values
                
                // Extracting variables
                String empId = data[0];
                String empName = data[1];
                String birthday = data[2];
                double hourlyRate = Double.parseDouble(data[3]);
                
                // For demonstration, let's assume columns 4 and 5 are Time-In and Time-Out
                // In a real loop, you would iterate through the attendance records from June to Dec
                String timeIn = data[4]; 
                String timeOut = data[5]; 
                
                // Task 7: Calculate Hours Worked
                double hoursWorked = calculateHoursWorked(timeIn, timeOut);
                
                // Task 8: Compute Semi-Monthly Salary (Assuming 1st & 2nd Cutoffs)
                // For simulation, we'll pretend the computed hours apply to half a month
                double firstCutoffSalary = hoursWorked * hourlyRate; 
                double secondCutoffSalary = hoursWorked * hourlyRate; 
                
                // Mentor Rule: Add 1st and 2nd cutoff before computing deductions
                double monthlyGrossSalary = firstCutoffSalary + secondCutoffSalary;
                
                // Task 9: Apply Government Deductions
                double sss = calculateSSS(monthlyGrossSalary);
                double philHealth = calculatePhilHealth(monthlyGrossSalary);
                double pagIbig = calculatePagIBIG(monthlyGrossSalary);
                double tax = calculateTax(monthlyGrossSalary, sss, philHealth, pagIbig);
                
                double totalDeductions = sss + philHealth + pagIbig + tax;
                
                // Deductions only applied to 2nd cutoff
                double finalSecondCutoffSalary = secondCutoffSalary - totalDeductions;
                
                // Output Results (No rounding off as per mentor's rules)
                System.out.println("Employee: " + empName + " (" + empId + ")");
                System.out.println("Total Hours Worked: " + hoursWorked);
                System.out.println("1st Cutoff Salary: " + firstCutoffSalary);
                System.out.println("2nd Cutoff Salary (After Deductions): " + finalSecondCutoffSalary);
                System.out.println("-------------------------------------------------");
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    // Task 7 Method: Calculate Hours Worked
    public static double calculateHoursWorked(String timeIn, String timeOut) {
        if (timeIn.isEmpty() || timeOut.isEmpty()) return 0.0;
        
        String[] inParts = timeIn.split(":");
        int inHour = Integer.parseInt(inParts[0]);
        int inMin = Integer.parseInt(inParts[1]);
        
        String[] outParts = timeOut.split(":");
        int outHour = Integer.parseInt(outParts[0]);
        int outMin = Integer.parseInt(outParts[1]);
        
        // Rule: If log in at 8:05 AM, treat as 8:00 AM (grace period up to 8:10 AM)
        if (inHour == 8 && inMin <= 10) {
            inMin = 0;
        }
        
        // Rule: Do not include extra hours (Cap time out at 5:00 PM / 17:00)
        if (outHour >= 17) {
            outHour = 17;
            outMin = 0;
        }
        
        int totalInMinutes = (inHour * 60) + inMin;
        int totalOutMinutes = (outHour * 60) + outMin;
        int elapsedMinutes = totalOutMinutes - totalInMinutes;
        
        // Deduct 1 hour (60 mins) unpaid break if they worked a full shift (over 4 hours)
        if (elapsedMinutes > 240) {
            elapsedMinutes -= 60;
        }
        
        return elapsedMinutes / 60.0; // Return exactly as double (no rounding)
    }

    // Task 9 Methods: Government Deductions (Using standard placeholders, adjust to actual PH tables if needed)
    public static double calculateSSS(double monthlyGross) {
        // Provide the exact SSS bracket calculation here. Example flat rate for simulation:
        return monthlyGross * 0.045; 
    }

    public static double calculatePhilHealth(double monthlyGross) {
        // PhilHealth is usually 4% / 2 for the employee share
        return monthlyGross * 0.02; 
    }

    public static double calculatePagIBIG(double monthlyGross) {
        // Maximum contribution is usually capped at 100 or 200
        double contribution = monthlyGross * 0.02;
        return (contribution > 100) ? 100.0 : contribution; 
    }

    public static double calculateTax(double monthlyGross, double sss, double philHealth, double pagIbig) {
        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        // Simple withholding tax logic placeholder (Adjust based on BIR Table)
        if (taxableIncome <= 20833) return 0;
        return (taxableIncome - 20833) * 0.20;
    }
}