/*
 * GlobalLimit.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity representing global platform limits and settings.
 * Mapped to the "global_limits" database table.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "global_limits")
public class GlobalLimit {

    @Id
    private Integer id;

    /** Maximum hourly price allowed on the platform. */
    @Column(name = "hourly_price_limit")
    private BigDecimal hourlyPriceLimit;

    /** Global message text (can be saved as draft before publishing). */
    private String message;

    /** Whether the global message is visible to users. */
    @Column(name = "message_enabled", nullable = false)
    private Boolean messageEnabled = false;

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getHourlyPriceLimit() {
        return hourlyPriceLimit;
    }

    public void setHourlyPriceLimit(BigDecimal hourlyPriceLimit) {
        this.hourlyPriceLimit = hourlyPriceLimit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getMessageEnabled() {
        return messageEnabled;
    }

    public void setMessageEnabled(Boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }
}
