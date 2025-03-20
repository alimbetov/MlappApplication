package kz.mlapp.services.impls;

import jakarta.annotation.PostConstruct;
import kz.mlapp.dto.AuthRequestDto;
import kz.mlapp.dto.AuthResponseDto;
import kz.mlapp.dto.UserDto;
import kz.mlapp.enums.RoleName;
import kz.mlapp.model.Role;
import kz.mlapp.model.User;
import kz.mlapp.repos.RoleRepository;
import kz.mlapp.repos.UserRepository;
import kz.mlapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Добавили для хеширования пароля

    @Autowired
    private  JwtUtil jwtUtil;

    @PostConstruct
    public void initRoles() {
        List<RoleName> predefinedRoles = Arrays.asList(RoleName.values());

        for (RoleName roleName : predefinedRoles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Создана роль: " + roleName);
            }
        }
    }



    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public UserDto createUser(AuthRequestDto authRequestDto) {
        User user = new User();
        Set<Role> setRoles = new HashSet<>();
        var userRole = roleRepository.findByName(RoleName.ROLE_USER);
        userRole.ifPresent(setRoles::add);
        user.setUsername(authRequestDto.getEmail());
        user.setRoles(setRoles);
        user.setBlocked(false);
        user.setPassword(passwordEncoder.encode(authRequestDto.getPassword())); // ❗ Устанавливаем пароль
        return convertToDto(userRepository.save(user));
    }

    public List<String> getUSerRoleListByTokken(String tokken){
      return jwtUtil.getRolesFromToken(tokken).stream().toList();
    }

    public ResponseEntity<AuthResponseDto> login ( AuthRequestDto request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getEmail());
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = userOptional.get();
        String token = jwtUtil.createToken(user.getUsername(), user.getRoles());
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        var resp = AuthResponseDto.builder()
                .token(token)
                .roles(roleNames)
                .build();
        return ResponseEntity.ok(resp);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setRoles(userDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(RoleName.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleName)))
                .collect(Collectors.toSet()));
        user.setBlocked(userDto.isBlocked());
        return convertToDto(userRepository.save(user));
    }
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return convertToDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        Set<String> roleNames = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(roleNames);
        dto.setBlocked(user.isBlocked());
        return dto;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByUsername(email);
    }

    public UserDto findUserDtoByEmail(String email) {
        var entity = findByEmail(email).orElse(null);
        if (entity==null) return null;
        return convertToDto(entity);
    }

}
