package at.htlleonding.library.rest;

public class AuthorDto {
    public AuthorDto() {
    }

    public int Id;
    public String FirstName;
    public String LastName;

    public AuthorDto(String firstName, String lastName) {
        FirstName = firstName;
        LastName = lastName;
    }

    public AuthorDto(int id, String firstName, String lastName) {
        Id = id;
        FirstName = firstName;
        LastName = lastName;
    }

    @Override
    public String toString() { return FirstName + " " + LastName;}
}
