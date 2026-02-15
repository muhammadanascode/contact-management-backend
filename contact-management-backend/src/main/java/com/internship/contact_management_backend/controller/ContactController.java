package com.internship.contact_management_backend.controller;

import com.internship.contact_management_backend.dto.ContactDto;
import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/create")
    public ResponseEntity<ContactDto> createContact(@Valid @RequestBody ContactDto contact){

        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //create contact
       Contact savedContact = contactService.createContact(contact.toEntity(),userEmail);
       return ResponseEntity
               .status(HttpStatus.CREATED)
               .body(savedContact.toDto());

    }

    @GetMapping(value = "/getAll", params = "!name")
    public ResponseEntity<List<ContactDto>> getAllContactsForUser(){

        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //fetch contacts by user id
        return ResponseEntity.ok(contactService.getContactsByEmail(userEmail)
                .stream()
                .map(Contact::toDto)
                .toList());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id){
        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //delete the contact
        contactService.deleteContact(id,userEmail);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ContactDto> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDto contactDto){
        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //update the contact
        Contact updatedContact = contactService.updateContact(id, contactDto.toEntity(), userEmail);
        return ResponseEntity.ok(updatedContact.toDto());

    }

    @GetMapping(value = "/getAll", params = "name")
    public ResponseEntity<List<ContactDto>> searchContacts(
            @RequestParam("name") String keyword) {
        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //search contacts
         return ResponseEntity.ok(contactService.searchContacts(keyword, userEmail)
                                                .stream()
                                                .map(ContactDto::from)
                                                .toList()
         );
    }
}
