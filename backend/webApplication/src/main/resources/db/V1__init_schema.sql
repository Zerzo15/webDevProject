-- web_db.tags definition

CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt48xdq560gs3gap9g7jg36kgc` (`name`),
  UNIQUE KEY `UKsn0d91hxu700qcw0n4pebp5vc` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.users definition

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_status` enum('ACTIVE','LOCKED') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','CUSTOMER') NOT NULL,
  `phone` varchar(20) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `email_verified` bit(1) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKdu5v5sr43g5bfnji4vb8hg5s3` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.vouchers definition

CREATE TABLE `vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `discount_value` decimal(12,2) NOT NULL,
  `end_date` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `max_discount_amount` decimal(12,2) DEFAULT NULL,
  `min_order_amount` decimal(12,2) NOT NULL,
  `start_date` datetime(6) NOT NULL,
  `type` enum('FIXED_AMOUNT','FREESHIP','PERCENT') NOT NULL,
  `usage_limit` int NOT NULL,
  `used_count` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK30ftp2biebbvpik8e49wlmady` (`code`),
  KEY `idx_vouchers_code` (`code`),
  KEY `idx_vouchers_active_dates` (`is_active`,`start_date`,`end_date`),
  CONSTRAINT `vouchers_chk_1` CHECK (((`discount_value` >= 0) and (`min_order_amount` >= 0) and (`end_date` > `start_date`) and ((`type` <> _utf8mb4'PERCENT') or (`discount_value` <= 100))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.body_profile definition

CREATE TABLE `body_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bust_cm` int DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `height_cm` int DEFAULT NULL,
  `hips_cm` int DEFAULT NULL,
  `waist_cm` int DEFAULT NULL,
  `weight_kg` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKei0pn591mfuui7ojcur59jni` (`user_id`),
  CONSTRAINT `FKpijimv2ggrcaqhm1okrtpwma2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.carts definition

CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.categories definition

CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `description` text,
  `display_order` int NOT NULL,
  `is_active` bit(1) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoul14ho7bctbefv8jywp5v3i2` (`slug`),
  KEY `idx_categories_slug` (`slug`),
  KEY `FKsaok720gsu4u2wrgbk10b5n8d` (`parent_id`),
  CONSTRAINT `FKsaok720gsu4u2wrgbk10b5n8d` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.orders definition

CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shipping_address_text` varchar(255) DEFAULT NULL,
  `status` enum('PENDING_CONFIRMATION','AWAITING_PICKUP','SHIPPING','DELIVERED','COMPLETED','CANCELLED') NOT NULL,
  `total_amount` decimal(12,2) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `discount_amount` decimal(12,2) NOT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  `payment_method` varchar(50) NOT NULL,
  `shipping_discount` decimal(12,2) NOT NULL,
  `shipping_fee` decimal(12,2) NOT NULL,
  `tracking_code` varchar(100) DEFAULT NULL,
  `voucher_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKd1kkvl4hi9hp3peub1umk2xeo` (`idempotency_key`),
  KEY `idx_orders_user_id` (`user_id`),
  KEY `idx_orders_status` (`status`),
  KEY `FKdimvsocblb17f45ikjr6xn1wj` (`voucher_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKdimvsocblb17f45ikjr6xn1wj` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.otp_codes definition

CREATE TABLE `otp_codes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used` bit(1) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_otp_codes_email` (`email`),
  KEY `idx_otp_codes_user_id` (`user_id`),
  CONSTRAINT `FKt9j4to767daef0ws3fiixis0y` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.outfit_recommendation definition

CREATE TABLE `outfit_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_response_text` varchar(255) DEFAULT NULL,
  `context_prompt` varchar(255) DEFAULT NULL,
  `recommended_variant_ids` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK11uy4jgemsp7t7s61nmndnyna` (`user_id`),
  CONSTRAINT `FK11uy4jgemsp7t7s61nmndnyna` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.products definition

CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `base_price` decimal(12,2) NOT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `description` text,
  `image_url` varchar(500) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `material` varchar(100) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `slug` varchar(180) NOT NULL,
  `weight_grams` int NOT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKostq1ec3toafnjok09y9l7dox` (`slug`),
  KEY `idx_products_category_id` (`category_id`),
  KEY `idx_products_slug` (`slug`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `products_chk_1` CHECK (((`base_price` >= 0) and (`weight_grams` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.refresh_tokens definition

CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo2mlirhldriil2y7krapq4frt` (`token_hash`),
  KEY `idx_refresh_tokens_user_id` (`user_id`),
  KEY `idx_refresh_tokens_token_hash` (`token_hash`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.voucher_usages definition

CREATE TABLE `voucher_usages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `used_at` datetime(6) NOT NULL,
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `voucher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_user_order` (`voucher_id`,`user_id`,`order_id`),
  UNIQUE KEY `UKp6oa0unmdk69fka8d97jsvwqm` (`order_id`),
  KEY `FKayocv8v53p94c3jr2ookjd4jq` (`user_id`),
  CONSTRAINT `FKayocv8v53p94c3jr2ookjd4jq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKc6ctsyf63q19vkjb6k3lrecev` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKguvf95urdn2namu0hgiasgttx` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.order_status_history definition

CREATE TABLE `order_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `changed_at` datetime(6) NOT NULL,
  `changed_by` varchar(100) NOT NULL,
  `from_status` enum('PENDING_CONFIRMATION','AWAITING_PICKUP','SHIPPING','DELIVERED','COMPLETED','CANCELLED') DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `to_status` enum('PENDING_CONFIRMATION','AWAITING_PICKUP','SHIPPING','DELIVERED','COMPLETED','CANCELLED') NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_history_order_id` (`order_id`),
  CONSTRAINT `FKnmcbg3mmbt8wfva97ra40nmp3` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.product_variants definition

CREATE TABLE `product_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `stock_quantity` int NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.cart_items definition

CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `quantity` int NOT NULL,
  `cart_id` bigint NOT NULL,
  `product_variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_variant` (`cart_id`,`product_variant_id`),
  KEY `FKn1s4l7h0vm4o259wpu7ft0y2y` (`product_variant_id`),
  CONSTRAINT `FKn1s4l7h0vm4o259wpu7ft0y2y` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.order_items definition

CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `unit_price` decimal(12,2) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `product_variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt4dc2r9nbvbujrljv3e23iibt` (`order_id`),
  KEY `FK1n92a16igj05dvyfl9c107xoc` (`product_variant_id`),
  CONSTRAINT `FK1n92a16igj05dvyfl9c107xoc` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKt4dc2r9nbvbujrljv3e23iibt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- web_db.return_requests definition

CREATE TABLE `return_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reason` varchar(255) DEFAULT NULL,
  `status` enum('PENDING','REFUNDED','REJECTED') DEFAULT NULL,
  `order_item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKku9t1gdmxev2m45dy78wrtc9a` (`order_item_id`),
  CONSTRAINT `FKku9t1gdmxev2m45dy78wrtc9a` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;