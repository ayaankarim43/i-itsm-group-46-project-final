package com.example.demo;

import jakarta.validation.constraints.*;

public record UpdateIncidentRequest(
        @NotBlank String description,
        @Min(1) @Max(5) int impact,
        @Min(1) @Max(5) int urgency) {}