package com.facturationpme.payments.gateway;

import java.math.BigDecimal;

public record CheckoutRequest(
    BigDecimal amount, String currency, String reference, String successUrl, String errorUrl) {}
