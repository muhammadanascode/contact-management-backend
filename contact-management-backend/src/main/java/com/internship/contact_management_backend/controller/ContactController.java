package com.internship.contact_management_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/contacts")
public class ContactController {

    @GetMapping
    public String getContacts() {
        return "List of contacts";
    }
}
