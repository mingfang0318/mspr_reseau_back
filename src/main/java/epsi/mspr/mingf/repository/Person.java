package epsi.mspr.mingf.repository;

import org.springframework.stereotype.Repository;

@Repository
public class Person {
    private String fullName;//prenom.nom en miniscule
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullname) {
        this.fullName = fullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
