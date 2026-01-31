package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.exception.ResourceNotFoundException;
import com.internship.contact_management_backend.repository.ContactRepository;
import com.internship.contact_management_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String BAD_CREDENTIALS = "Bad Credentials";

    public Contact createContact(Contact contact, String email) {

        // find user by email
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS));
        // attach contact to user
        contact.setUser(user);

        Contact saved = contactRepository.save(contact);
        log.info("Contact created id={} for user={}", saved.getId(), email);
        return saved ;
    }


    public List<Contact> getContactsByEmail(String email){
        // find user by email
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS));
        // fetch contacts by user id
        List<Contact> contacts = contactRepository.findByUserId(user.getId());
        log.info("Fetched {} contacts for user={}", contacts.size(), email);
        return  contacts;
    }

    public void deleteContact(Long contactId, String email) {
        // find user by email
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS));
        // find contact by id
        Contact contact = contactRepository.findContactById(contactId)
                                           .orElseThrow(() -> new ResourceNotFoundException("Contact not found : " + contactId));
        // check if the contact belongs to the user
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        // delete the contact
        contactRepository.delete(contact);
        log.info("Contact deleted id={} by user={}", contactId, email);
    }

    public Contact updateContact(Long contactId, Contact updatedContact, String email) {
        // find user by email
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS));

        // find contact by id
        Contact existingContact = contactRepository.findContactById(contactId)
                                                   .orElseThrow(() -> new ResourceNotFoundException("Contact not found : " + contactId));
        // check if the contact belongs to the user
        if (!existingContact.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        // update contact details
        existingContact.setFirstName(updatedContact.getFirstName());
        existingContact.setLastName(updatedContact.getLastName());
        existingContact.setEmail(updatedContact.getEmail());
        existingContact.setEmailLabel(updatedContact.getEmailLabel());
        existingContact.setPhoneNumber(updatedContact.getPhoneNumber());
        existingContact.setPhoneNumberLabel(updatedContact.getPhoneNumberLabel());

        Contact saved = contactRepository.save(existingContact);
        log.info("Contact updated id={} by user={}", saved.getId(), email);
        return saved;
    }

    // Search contacts by keyword in first name or last name
    public List<Contact> searchContacts(String keyword, String email) {

        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS));

        List<Contact> results =  contactRepository.searchContacts(user.getId(), keyword);
        log.info("Search returned {} results for user={}", results.size(), email);
        return results;
    }
}
