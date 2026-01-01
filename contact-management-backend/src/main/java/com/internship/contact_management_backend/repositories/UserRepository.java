package com.internship.contact_management_backend.repositories;

import com.internship.contact_management_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
