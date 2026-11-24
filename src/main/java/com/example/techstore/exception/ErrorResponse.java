package com.example.techstore.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        int status, String error, String message, String hint, List<String> details, Instant timestamp) {}
