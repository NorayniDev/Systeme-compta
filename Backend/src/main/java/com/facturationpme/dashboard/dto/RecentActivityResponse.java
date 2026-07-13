package com.facturationpme.dashboard.dto;

import java.time.Instant;

/** Alignee sur {@code IRecentActivity} ({@code type} : invoice|payment|client|quote|system). */
public record RecentActivityResponse(String id, String type, String message, Instant createdAt) {}
