CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,

    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_contact_name_length
        CHECK (CHAR_LENGTH(name) >= 3),

    CONSTRAINT chk_contact_phone_format
        CHECK (phone_number REGEXP '^[0-9]{10,15}$'),

    CONSTRAINT fk_contacts_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);
