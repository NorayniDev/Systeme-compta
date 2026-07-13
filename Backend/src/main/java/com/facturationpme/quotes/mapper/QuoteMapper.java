package com.facturationpme.quotes.mapper;

import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.domain.QuoteLine;
import com.facturationpme.quotes.dto.QuoteLineResponse;
import com.facturationpme.quotes.dto.QuoteResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuoteMapper {

  QuoteResponse toResponse(Quote quote);

  QuoteLineResponse toLineResponse(QuoteLine line);
}
