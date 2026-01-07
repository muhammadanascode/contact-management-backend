package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.ContactRepository;
import com.internship.contact_management_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public Contact createContact(Contact contact, String email) {

        // find user by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // attach contact to user
        contact.setUser(user);

        return contactRepository.save(contact);
    }


    public List<Contact> getContactsByEmail(String email){
        // find user by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // fetch contacts by user id
        return contactRepository.findByUserId(user.getId());
    }
}
