package com.sales.request;


import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Integer city;
    @NotNull
    private Integer state;
    private Float latitude;
    private Float altitude;

}
