package com.facturationpme.accounting.mapper;

import com.facturationpme.accounting.domain.JournalEntry;
import com.facturationpme.accounting.dto.JournalEntryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JournalEntryMapper {

  JournalEntryResponse toResponse(JournalEntry journalEntry);
}
