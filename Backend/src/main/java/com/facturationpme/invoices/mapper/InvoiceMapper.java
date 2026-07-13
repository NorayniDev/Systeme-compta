package com.facturationpme.invoices.mapper;

import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceLine;
import com.facturationpme.invoices.dto.InvoiceLineResponse;
import com.facturationpme.invoices.dto.InvoiceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

  InvoiceResponse toResponse(Invoice invoice);

  InvoiceLineResponse toLineResponse(InvoiceLine line);
}
