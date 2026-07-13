package com.facturationpme.payments.gateway;

public record WebhookEvent(String sessionId, WebhookOutcome outcome) {}
