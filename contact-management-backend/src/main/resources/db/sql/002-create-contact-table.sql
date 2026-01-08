CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,

    email VARCHAR(100) NOT NULL,
    email_label VARCHAR(20) NOT NULL,   -- WORK, PERSONAL, etc.

    phone_number VARCHAR(20) NOT NULL,
    phone_number_label VARCHAR(20) NOT NULL, -- WORK, PERSONAL, etc.

    user_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Name validation
    CONSTRAINT chk_first_name_length
        CHECK (CHAR_LENGTH(first_name) >= 2),

    CONSTRAINT chk_last_name_length
        CHECK (CHAR_LENGTH(last_name) >= 2),

    -- Email validation (your regex)
    CONSTRAINT chk_email_format
        CHECK (email REGEXP '^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$'),

    -- Phone validation (E.164)
    CONSTRAINT chk_phone_format
        CHECK (phone_number REGEXP '^\\+[1-9][0-9]{1,14}$'),

    -- Foreign key
    CONSTRAINT fk_contacts_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);
