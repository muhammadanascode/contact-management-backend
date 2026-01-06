package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(Contact contact){
        return contactRepository.save(contact);
    }

    public List<Contact> getContactsByUserId(Long userId){
        return contactRepository.findByUserId(userId);
    }
}
