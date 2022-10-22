package ru.kata.spring.boot_security.demo.dao;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    public UserDAOImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT user FROM User user", User.class).getResultList();
    }

    public User getUser(int id) {
        return (User) entityManager.createQuery("FROM User WHERE id = :id").setParameter("id", id).getSingleResult();
    }

    public User getUserByEmail(String email) {
        return (User) entityManager.createQuery("FROM User user JOIN FETCH user.roles WHERE email = :email").setParameter("email", email).getSingleResult();
    }

    public void save(User user) {
        entityManager.persist(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() != null) {
            user.setRoles(listRoles(user));
        }
    }

    public void update(int id, User user) {
        String oldPassword = getUser(id).getPassword();
        entityManager.merge(user);
        String newPassword = user.getPassword();
        if(!Objects.equals(oldPassword, newPassword)) {
            getUser(id).setPassword(passwordEncoder.encode(newPassword));
        }
        if (user.getRole() != null) {
            getUser(id).setRoles(listRoles(user));
        }
    }

    public void delete(int id) {
        entityManager.remove(getUser(id));
    }

    private List<Role> listRoles(User user) {
        List<Role> roles = new ArrayList<>();
        if (user.getRole().toLowerCase().contains("admin")) {
            roles.add(new Role(1, "ROLE_ADMIN"));
        }
        if (user.getRole().toLowerCase().contains("user")) {
            roles.add(new Role(2, "ROLE_USER"));
        }
        return roles;
    }
}
