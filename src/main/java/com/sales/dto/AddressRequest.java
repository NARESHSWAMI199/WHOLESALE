package com.sales.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private Integer addressId;
    private String street;
    private String zipCode;
    private Integer city;
    private Integer state;
    private Float latitude;
    private Float altitude;

}
