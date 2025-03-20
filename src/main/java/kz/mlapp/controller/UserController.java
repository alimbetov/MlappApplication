package kz.mlapp.controller;


import kz.mlapp.dto.AuthRequestDto;
import kz.mlapp.dto.AuthResponseDto;
import kz.mlapp.dto.UserDto;

import kz.mlapp.model.User;
import kz.mlapp.services.impls.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/xusers")
@CrossOrigin(origins = "*") // Разрешает фронтенду делать запросы
public class UserController {

    @Autowired
    private UserService userService;



    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUsers();
    }



    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
       return userService.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequestDto request) {
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email уже зарегистрирован");
        }
        String resp = "Регистрация успешна";
        try {
          userService.createUser(request);
        } catch (Exception e){
            resp = e.getMessage();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
