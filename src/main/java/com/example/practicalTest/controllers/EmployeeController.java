package com.example.practicalTest.controllers;

import com.example.practicalTest.models.Employee;
import com.example.practicalTest.security.services.EmployeeService;
import com.example.practicalTest.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    private static final String UPLOAD_FOLDER = "D:\\SpringBoot Projects\\pics\\profile-pictures\\";

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        if (userEmail.equals(employee.getEmail())){
            return employeeService.saveEmployee(employee,userDetails);
        }else{
            //exception to return error with response header 404
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: Email does not match authenticated user.");
        }

    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            Employee updatedEmployee = employee.get();
            updatedEmployee.setName(employeeDetails.getName());
            updatedEmployee.setBirthDate(employeeDetails.getBirthDate());
            employeeService.saveEmployee(updatedEmployee);
            return ResponseEntity.ok("Employee Updated Successfully") ;
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/remove/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
//        ResponseEntity.ok("File uploaded successfully");
        return ResponseEntity.ok("Employee Deleted Successfully");
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Optional<Employee> employeeOptional = employeeService.getEmployeeById(id);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();

            // Create upload folder if it doesn't exist
            Path folderPath = Paths.get("D:\\SpringBoot Projects\\pics\\profile-pictures\\");
            if (!Files.exists(folderPath)) {
                try {
                    Files.createDirectories(folderPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create upload folder");
                }
            }

            // Generate file path and name dynamically
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = employee.getId() + "_" + employee.getName() + fileExtension;
            String filePath = UPLOAD_FOLDER + fileName;

            // Save file to disk
            try {
                file.transferTo(new File(filePath));

                // Update employee's profile picture path
                employee.setProfilePicturePath(filePath);
                employeeService.saveEmployee(employee);

                return ResponseEntity.ok("File uploaded successfully");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
            }
        } else {
            return ResponseEntity.notFound().build();
}
}

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadProfilePicture(@PathVariable Long id) {
        Optional<Employee> employeeOptional = employeeService.getEmployeeById(id);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            String filePath = employee.getProfilePicturePath();
            try {
                Path path = Paths.get(filePath);
                byte[] data = Files.readAllBytes(path);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG); // Adjust the media type if needed
                headers.setContentDispositionFormData("attachment", path.getFileName().toString());

                return new ResponseEntity<>(data, headers, HttpStatus.OK);
//                return ResponseEntity.ok(data);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}






