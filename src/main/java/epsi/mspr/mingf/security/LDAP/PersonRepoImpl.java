package epsi.mspr.mingf.security.LDAP;

import epsi.mspr.mingf.repository.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Repository;

import javax.naming.directory.Attributes;

import java.util.List;

@Repository
public class PersonRepoImpl {

    @Autowired
    private LdapTemplate ldapTemplate;

    public PersonRepoImpl() {
    }

    private class PersonAttributesMapper implements AttributesMapper<Person> {
        public Person mapFromAttributes(Attributes attrs) throws NamingException, javax.naming.NamingException {
            Person person = new Person();
            person.setFullName((String)attrs.get("uid").get());
            person.setPassword((String)attrs.get("password").get());
            return person;
        }
    }

    public List<Person> getAllPersons() {
        return ldapTemplate.search(LdapQueryBuilder.query()
                .where("objectclass").is("person"), new PersonAttributesMapper());
    }
    public Person findPerson(String dn) {
        return ldapTemplate.lookup(dn, new PersonAttributesMapper());
    }
}
