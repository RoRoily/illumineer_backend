import javax.persistence.*;

@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String domain;
    private String name;

    public Institution() {}

    public Institution(String domain, String name) {
        this.domain = domain;
        this.name = name;
    }

    // Getters and setters
}
