package com.example.practicalTest.repository;

import com.example.practicalTest.models.ERole;
import com.example.practicalTest.models.Employee;
import com.example.practicalTest.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long id);
}

