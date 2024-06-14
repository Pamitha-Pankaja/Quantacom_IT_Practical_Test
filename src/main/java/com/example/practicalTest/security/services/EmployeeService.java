package com.example.practicalTest.security.services;

import com.example.practicalTest.models.Employee;
import com.example.practicalTest.repository.EmployeeRepository;
import com.example.practicalTest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    public ResponseEntity<?> saveEmployee(Employee employee) {
        employeeRepository.save(employee);
        return ResponseEntity.ok(employee);
    }
    public ResponseEntity<?> saveEmployee(Employee employee, UserDetailsImpl userDetails) {
        Optional<Employee> employee1 = employeeRepository.findByUserId(userDetails.getId());
        if (!employee1.isEmpty()){
            employee1.get().setName(employee.getName());
            employee1.get().setBirthDate(employee.getBirthDate());
            employee1.get().setEmail(employee.getEmail());
            employeeRepository.save(employee1.get());
            return  ResponseEntity.ok(employee);
        }
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: User not found.");
    }



    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
