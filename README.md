MotorPH Payroll System (Milestone 2)
A console-based Java application developed for MotorPH to handle employee login, personal detail lookup, and semi-monthly payroll processing.

Project Description
This system automates the computation of employee hours and salary based on raw CSV data. It features a role-based login system routing users to either an Employee Portal or a Payroll Staff Portal. The payroll processing covers records from June to December and automatically splits salaries into two cutoffs, applying government-mandated deductions to the second cutoff.

Key Features
Role-Based Access Control: * employee role: Can look up their own basic information using their Employee ID.

payroll_staff role: Can process detailed payroll reports for a single employee or batch process all employees.

Smart Attendance Tracking:

Enforces an 8:00 AM to 5:00 PM work schedule.

Includes a 10-minute morning grace period (logs at 8:10 AM or earlier are counted as 8:00 AM).

Automatically caps overtime (logs after 5:00 PM default to 5:00 PM).

Automatically deducts a 1-hour unpaid lunch break.

Semi-Monthly Cutoffs:

1st Cutoff (Days 1-15): Computes gross salary based on hours worked.

2nd Cutoff (Days 16-31): Computes gross salary and applies all monthly deductions to finalize the net pay.

Automated Government Deductions:

Calculates SSS using the standard bracket table.

Calculates PhilHealth (3% of basic salary, 50/50 employee-employer share).

Calculates Pag-IBIG (1-2% based on salary bracket, capped at PHP 100).

Calculates Withholding Tax based on taxable income (gross minus government contributions).

Requirements
Java Development Kit (JDK): Version 8 or higher.

IDE: NetBeans (or any standard Java IDE).

Data Files: The system requires two specific CSV files to run successfully:

Employee Details.csv

Attendance Record.csv

How to Run the Program
Ensure that Employee Details.csv and Attendance Record.csv are placed directly in the root directory of your project (outside the src folder if you are using an IDE like NetBeans).

Compile and run Main.java.

When prompted, log in using the default credentials:

Employee Access: Username: employee | Password: 12345

Staff Access: Username: payroll_staff | Password: 12345

Technical Notes
The program utilizes native Java libraries (java.io.*, java.util.Scanner).

All logic is contained within static methods in a single Main.java file. No Object-Oriented Programming (OOP) concepts were utilized for this specific milestone.

Decimal computations are kept exact to prevent rounding errors in salary payouts.
