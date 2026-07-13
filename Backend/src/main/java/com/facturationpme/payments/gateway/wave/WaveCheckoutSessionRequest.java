package com.facturationpme.payments.gateway.wave;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Corps de {@code POST /v1/checkout/sessions} (api.wave.com) - voir docs.wave.com/business. */
record WaveCheckoutSessionRequest(
    String amount,
    String currency,
    @JsonProperty("success_url") String successUrl,
    @JsonProperty("error_url") String errorUrl) {}
