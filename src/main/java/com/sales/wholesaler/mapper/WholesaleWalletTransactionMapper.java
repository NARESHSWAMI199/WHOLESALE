package com.sales.wholesaler.mapper;


import com.sales.entities.WalletTransaction;
import com.sales.wholesaler.dto.WholesaleWalletTransactionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleWalletTransactionMapper {
    WholesaleWalletTransactionDto toDto(WalletTransaction walletTransaction);
}
