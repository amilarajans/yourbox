INSERT INTO ORI_AUTHORITY (NAME_) VALUES ('ROLE_ADMIN');
INSERT INTO ORI_AUTHORITY (NAME_) VALUES ('ROLE_USER');

INSERT INTO ORI_USER (LOGIN_, PASSWORD_HASH_, FIRST_NAME_, LAST_NAME_, EMAIL_, ACTIVATED_, LANG_KEY_, ACTIVATION_KEY_, RESET_KEY_, CREATED_BY_, CREATED_DATE_, RESET_DATE_, LAST_MODIFIED_BY_, LAST_MODIFIED_DATE_)
VALUES ('admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC', 'Administrator', 'Administrator',
                 'admin@localhost', TRUE, 'en', NULL, NULL, 'system', CURRENT_TIMESTAMP(), NULL, 'system', NULL);
