package at.htlleonding.library.persistence;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Author {
    public static Author JohnDoe = new Author("John", "Doe", LocalDate.of(1970,1,1));
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateBirth;

    @Column()
    private LocalDate dateDeath;


    public String getFirstName() {
        return firstName;
    }
    public Integer getId() {
        return id;
    }

    public Author() {
    }

    public Author(String firstName, String lastName, LocalDate dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateBirth = dob;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(LocalDate dateBirth) {
        this.dateBirth = dateBirth;
    }

    public LocalDate getDateDeath() {
        return dateDeath;
    }

    public void setDateDeath(LocalDate dateDeath) {
        this.dateDeath = dateDeath;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() { return id + " " + getFullName(); }
}
