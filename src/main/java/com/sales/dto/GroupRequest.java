package com.sales.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {
    Integer groupId;
    String name;
    String slug;
    List<Integer> permissions;

}
