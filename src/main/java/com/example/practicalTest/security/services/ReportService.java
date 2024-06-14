package com.example.practicalTest.security.services;

import com.example.practicalTest.models.Employee;
import com.example.practicalTest.repository.EmployeeRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    public byte[] generateEmployeesReport() throws JRException, IOException {
        List<Employee> employees = employeeRepository.findAll();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);

        InputStream inputStream = resourceLoader.getResource("classpath:reports/employees.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}

