package com.facturationpme.payments.gateway.wave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Reponse de {@code POST /v1/checkout/sessions} - champs non utilises ici ignores. */
@JsonIgnoreProperties(ignoreUnknown = true)
record WaveCheckoutSessionResponse(
    String id, @JsonProperty("wave_launch_url") String waveLaunchUrl) {}
