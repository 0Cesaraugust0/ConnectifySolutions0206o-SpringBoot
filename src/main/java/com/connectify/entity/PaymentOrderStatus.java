package com.connectify.entity;

/**
 * Estado interno de una orden enviada al gateway. No equivale al estado de
 * Purchase: la compra y los tickets se crean únicamente después de una
 * confirmación firmada del proveedor de pagos.
 */
public enum PaymentOrderStatus {
    CREATED,
    PENDING_GATEWAY,
    PAID,
    FAILED,
    CANCELLED
}
