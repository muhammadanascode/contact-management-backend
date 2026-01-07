package com.internship.contact_management_backend.controller;

import com.internship.contact_management_backend.dto.ContactDto;
import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/create")
    public ResponseEntity<ContactDto> createContact(@Valid @RequestBody ContactDto contact){

        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //create contact
       Contact savedContact = contactService.createContact(contact.toEntity(),userEmail);
       return ResponseEntity.ok(savedContact.toDto());

    }
}
