package com.internship.contact_management_backend.repository;

import com.internship.contact_management_backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Long> {
    // Find all contacts of a specific user
    List<Contact> findByUserId(Long userId);
}
