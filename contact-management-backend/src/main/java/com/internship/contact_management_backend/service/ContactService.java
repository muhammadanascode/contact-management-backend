package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.ContactRepository;
import com.internship.contact_management_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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

    public void deleteContact(Long contactId, String email) {
        // find user by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // find contact by id
        Contact contact = contactRepository.findContactById(contactId);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        // check if the contact belongs to the user
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException("Bad Credentials");
        }
        // delete the contact
        contactRepository.delete(contact);
    }

}
