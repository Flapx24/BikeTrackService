package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Serializable> {

        boolean existsByEmail(String email);

        boolean existsByEmailIgnoreCase(String email);

        boolean existsByUsernameIgnoreCase(String username);

        User findByEmail(String email);

        List<User> findAll();

        List<User> findByEmailIgnoreCase(String email);

        List<User> findByUsernameIgnoreCase(String username);

        List<User> findByUsernameContainingIgnoreCase(String username);

        List<User> findAllByOrderByIdAsc();

        @Query("SELECT u FROM User u WHERE u.role != :adminRole")
        List<User> findAllNonAdminUsers(@Param("adminRole") Role adminRole);

        @Query("SELECT u FROM User u WHERE " +
                        "u.role != :adminRole AND " +
                        "(:username IS NULL OR :username = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND "
                        +
                        "(:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))")
        Page<User> findByUsernameContainingAndEmailContainingAndRoleNotPaginated(
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("adminRole") Role adminRole,
                        Pageable pageable);

}
