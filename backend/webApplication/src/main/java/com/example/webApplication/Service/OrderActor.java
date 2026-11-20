package com.example.webApplication.Service;

/**
 * Who is asking for an order status change.
 * CUSTOMER, STAFF and ADMIN match the three roles of the system.
 * SYSTEM is the automatic actor used by scheduled jobs (for example completing a delivered order
 * after the customer did not confirm receipt in time).
 */
public enum OrderActor {
    CUSTOMER,
    STAFF,
    ADMIN,
    SYSTEM
}
