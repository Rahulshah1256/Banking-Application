package com.jantabank.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Forward-compatible summary tile for a product module (cards, loans, deposits).
 * {@code available=false} signals the module is not yet live so the frontend can
 * render a "coming soon" placeholder without special-casing per module.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSummaryDto {
    private String label;
    private boolean available;
    private int count;
    private double totalAmount;
}
