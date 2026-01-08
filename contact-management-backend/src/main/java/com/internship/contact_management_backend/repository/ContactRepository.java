package com.internship.contact_management_backend.repository;

import com.internship.contact_management_backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact,Long> {
    // Find all contacts of a specific user
    List<Contact> findByUserId(Long userId);

    // Find contact by id
    Optional<Contact> findContactById(Long id);

    @Query("""
SELECT c FROM Contact c
WHERE c.user.id = :userId
AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
     OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    List<Contact> searchContacts(Long userId, String keyword);

}
