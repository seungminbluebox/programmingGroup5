package kr.ac.dankook.group5.azit.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tech_stacks")
@Getter
@Setter
@NoArgsConstructor
public class TechStack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String stackName;

    @Column(length = 20)
    private String category;

    @Column(length = 500)
    private String iconUrl;
}
