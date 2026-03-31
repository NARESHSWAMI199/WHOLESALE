package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Entity
@Table(name = "address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address implements Serializable {

    @Column(name = "altitude")
    Float altitude;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "slug")
    private String slug;
    @Column(name = "street")
    private String street;
    @Column(name = "zip_code")
    private String zipCode;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city", referencedColumnName = "id")
    private City city;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state", referencedColumnName = "id")
    private State state;
    @Column(name = "latitude")
    private Float latitude;
    /**
     * -------------> COMMON COLUMNS ---------------------
     */
    @Column(name = "created_at")
    private Long createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private Long updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;
/**-------------! COMMON COLUMNS ---------------------*/

}
