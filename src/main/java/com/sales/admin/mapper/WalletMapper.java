package com.sales.admin.mapper;


import com.sales.admin.dto.WalletDto;
import com.sales.entities.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletDto toDto(Wallet wallet);
}
