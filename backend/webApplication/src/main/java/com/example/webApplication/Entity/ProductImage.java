package com.example.webApplication.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "product_images",
    indexes = {
        @Index(name = "idx_product_images_product_id", columnList = "product_id")
    }
)
public class ProductImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
