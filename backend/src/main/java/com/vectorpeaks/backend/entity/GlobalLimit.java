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

    /** Global message displayed to all users. */
    private String message;

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
}
