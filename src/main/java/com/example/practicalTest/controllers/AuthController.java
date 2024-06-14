package com.example.practicalTest.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.practicalTest.models.ERole;
import com.example.practicalTest.models.Employee;
import com.example.practicalTest.models.User;
import com.example.practicalTest.repository.EmployeeRepository;
import com.example.practicalTest.security.services.EmployeeService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practicalTest.models.Role;
import com.example.practicalTest.payload.request.LoginRequest;
import com.example.practicalTest.payload.request.SignupRequest;
import com.example.practicalTest.payload.response.JwtResponse;
import com.example.practicalTest.payload.response.MessageResponse;
import com.example.practicalTest.repository.RoleRepository;
import com.example.practicalTest.repository.UserRepository;
import com.example.practicalTest.security.jwt.JwtUtils;
import com.example.practicalTest.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  EmployeeService   employeeService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }


  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRoles();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "ROLE_ADMIN":
            try{
              Optional<Role> modRole = roleRepository.findByName(ERole.ROLE_ADMIN);
              if (modRole.isEmpty()){
                Role role1 = new Role();
                role1.setName(ERole.ROLE_ADMIN);
                roleRepository.save(role1);
                roles.add(role1);
              }else {
                roles.add(modRole.get());
              }

            }catch (Error e){
              System.out.println(e.getMessage());
            }
            break;
          case "ROLE_USER":
            System.out.println("user");
            try{
              Optional<Role> modRole = roleRepository.findByName(ERole.ROLE_USER);
              if (modRole.isEmpty()){
                Role role1 = new Role();
                role1.setName(ERole.ROLE_USER);
                roleRepository.save(role1);
                roles.add(role1);
              }else {
                roles.add(modRole.get());
              }

            }catch (Error e){
              System.out.println(e.getMessage());
            }
            break;


          default:
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
      });
    }
    user.setRoles(roles);
    User savedUser = userRepository.save(user);

    if (roles.stream().anyMatch(role -> role.getName().equals(ERole.ROLE_USER))) {
      Employee employee = new Employee();

      employee.setUser(savedUser); // Reference to the user
      employee.setRole(roles.stream().filter(role -> role.getName().equals(ERole.ROLE_USER)).findFirst().get());
      employeeService.saveEmployee(employee);
    }

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }


}
