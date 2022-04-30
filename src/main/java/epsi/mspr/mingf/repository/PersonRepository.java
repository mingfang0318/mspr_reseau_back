package epsi.mspr.mingf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import epsi.mspr.mingf.models.Person;


@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
	Optional<Person> findByUsername(String username);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
}