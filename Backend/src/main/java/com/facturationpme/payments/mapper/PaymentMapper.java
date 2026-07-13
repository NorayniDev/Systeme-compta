package com.facturationpme.payments.mapper;

import com.facturationpme.payments.domain.Payment;
import com.facturationpme.payments.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentResponse toResponse(Payment payment);
}
