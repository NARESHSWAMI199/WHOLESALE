package com.sales.wholesaler.mapper;


import com.sales.entities.Wallet;
import com.sales.wholesaler.dto.WholesaleWalletDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleWalletMapper {
    WholesaleWalletDto toDto(Wallet wallet);
}
