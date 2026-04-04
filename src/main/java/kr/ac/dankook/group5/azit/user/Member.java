package kr.ac.dankook.group5.azit.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileUrl;

    @Column(precision = 3, scale = 1)
    private BigDecimal mannerTemp = new BigDecimal("36.5");

    private Boolean isSearching = true;

    // 사용자 권한 설정 (기본값: ROLE_USER)
    @Column(nullable = false)
    private String role = "ROLE_USER";

    // sns_links는 추후 별도 설정(JSON/Map) 예정입니다.
    @Column(columnDefinition = "JSON")
    private String snsLinks;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<MemberStack> memberStacks = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Career> careers = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Availability> availabilities = new java.util.ArrayList<>();
}
