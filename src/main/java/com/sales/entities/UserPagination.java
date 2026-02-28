package com.sales.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Table(name = "user_paginations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPagination implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonIgnore
    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne
    @JoinColumn(name="pagination_id")
    private Pagination pagination;

    @Column(name="rows_number")
    private Integer rowsNumber;


}
