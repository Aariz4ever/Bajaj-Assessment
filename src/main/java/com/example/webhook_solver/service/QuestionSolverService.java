package com.example.webhook_solver.service;

import org.springframework.stereotype.Service;

@Service
public class QuestionSolverService {

    /**
     * Returns the final SQL query string to submit based on regNo last two digits
     * odd/even.
     * Replace the SQL strings below if you want to change them.
     */
    public String getFinalQueryForRegNo(String regNo) {
        // parse last two digits
        if (regNo == null || regNo.length() < 2) {
            throw new IllegalArgumentException("regNo must have at least 2 characters");
        }

        String lastTwo = regNo.substring(regNo.length() - 2);
        int lastVal;
        try {
            lastVal = Integer.parseInt(lastTwo);
        } catch (NumberFormatException e) {
            // fallback: if not numeric, treat as odd (choose one)
            lastVal = 1;
        }

        // Sample final query (the one I provided earlier)
        String finalQuery = """
                WITH filtered_payments AS (
                    SELECT p.emp_id, p.amount
                    FROM payments p
                    WHERE EXTRACT(DAY FROM p.payment_time) <> 1
                ),
                employee_totals AS (
                    SELECT e.emp_id, e.first_name, e.last_name, e.dob, e.department,
                           SUM(fp.amount) AS total_salary
                    FROM employee e
                    JOIN filtered_payments fp ON e.emp_id = fp.emp_id
                    GROUP BY e.emp_id, e.first_name, e.last_name, e.dob, e.department
                ),
                ranked_employees AS (
                    SELECT et.*, d.department_name,
                           RANK() OVER (PARTITION BY et.department ORDER BY et.total_salary DESC) AS rnk
                    FROM employee_totals et
                    JOIN department d ON et.department = d.department_id
                )
                SELECT department_name AS DEPARTMENT_NAME,
                       total_salary AS SALARY,
                       CONCAT(first_name, ' ', last_name) AS EMPLOYEE_NAME,
                       FLOOR(DATEDIFF(CURRENT_DATE, dob) / 365) AS AGE
                FROM ranked_employees
                WHERE rnk = 1
                ORDER BY department_name;
                """;

        // If odd -> question1 (we use same SQL), if even -> question2 (same for now)
        // If you have two different final queries, return them accordingly.
        if (lastVal % 2 != 0) {
            return finalQuery; // question 1 finalSQL
        } else {
            return finalQuery; // question 2 finalSQL (replace if different)
        }
    }
}
