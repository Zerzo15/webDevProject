ALTER TABLE users
    MODIFY COLUMN role
    ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL;

ALTER TABLE products
    DROP COLUMN image_url;

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    image_url VARCHAR(1000),
    original_filename VARCHAR(255),
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum VARCHAR(128),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(100),
    updated_at DATETIME(6),
    updated_by VARCHAR(100),
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),

    PRIMARY KEY (id),
    KEY idx_product_images_product_id (product_id),

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_product_images_size
        CHECK (size_bytes > 0)
);