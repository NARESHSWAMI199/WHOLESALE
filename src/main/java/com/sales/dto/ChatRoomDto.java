package com.sales.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
public class ChatRoomDto {
    Long id;
    @NotNull
    String name;
    String description;
    String slug;
    @NotNull
    List<String> users;
}
