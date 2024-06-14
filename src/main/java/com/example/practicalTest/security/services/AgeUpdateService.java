package com.example.practicalTest.security.services;

import com.example.practicalTest.models.Employee;
import com.example.practicalTest.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AgeUpdateService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Run every midnight
    public void updateAges() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            long ageInDays = ChronoUnit.DAYS.between(employee.getBirthDate(), LocalDate.now());
            employee.setCurrentAgeInDays(ageInDays);
            employeeRepository.save(employee);
        }
    }
}
