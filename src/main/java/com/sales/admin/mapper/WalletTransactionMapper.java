package com.sales.admin.mapper;


import com.sales.admin.dto.WalletTransactionDto;
import com.sales.entities.WalletTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {
    WalletTransactionDto toDto(WalletTransaction walletTransaction);
}
