ALTER TABLE stores
    ADD CONSTRAINT fk_address_id FOREIGN KEY (address_id) REFERENCES address (id);