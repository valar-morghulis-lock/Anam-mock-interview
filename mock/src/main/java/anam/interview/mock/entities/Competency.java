package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "competency")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Competency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}