package kz.mlapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private Set<String> roles;

    @JsonProperty("blocked")
    private boolean blocked;

    public void setRoles(Set<String> roleNames) {
        this.roles = roleNames;
    }

    public String getBlockState() {
        return blocked?"Blocked":"Open";
    }
}
