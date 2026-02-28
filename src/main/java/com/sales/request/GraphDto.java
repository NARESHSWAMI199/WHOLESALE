package com.sales.request;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GraphDto {
    Integer year;
    List<Integer> months;
}
