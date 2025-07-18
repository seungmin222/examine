package com.example.examine.entity.User;

import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User extends EntityTime implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 4, max = 12, message = "아이디는 4자 이상 12자 이하로 입력하세요.")
    @Column(nullable = false, unique = true)
    private String username;

    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력하세요.")
    @Column(nullable = false)
    private String password;

    private String role = "USER"; // 기본 역할

    private Integer level = 1;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPage> userPages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProduct> userProducts = new ArrayList<>();


    // 권한을 단순 문자열로 예시
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // 아래는 대부분 true로 반환 (계정 만료, 잠김 등)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setRole(String role) {
        this.role = role;
        int level = switch (role) {
            case "ADMIN" -> 10;
            case "USER" -> 1;
            default -> 0;
        };
        this.level = level;
    }
}
