package com.ccruz.personal_finance.transfer.web;

import com.ccruz.personal_finance.transfer.persistence.Transfer;
import com.ccruz.personal_finance.transfer.web.dto.TransferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "sourceAccountId", source = "sourceAccount.id")
    @Mapping(target = "sourceAccountCode", source = "sourceAccount.code")
    @Mapping(target = "targetAccountId", source = "targetAccount.id")
    @Mapping(target = "targetAccountCode", source = "targetAccount.code")
    TransferResponse toResponse(Transfer transfer);

    List<TransferResponse> toResponse(List<Transfer> transfers);
}
