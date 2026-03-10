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
    @NotNull
    private String street;
    @NotNull
    private String zipCode;
    @NotNull
    private Integer city;
    @NotNull
    private Integer state;
    private Float latitude;
    private Float altitude;

}
