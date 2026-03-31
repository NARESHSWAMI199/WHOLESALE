package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "store_permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorePermissions implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;

    @Column(name = "permission")
    String permission;

    @Column(name = "permission_for")
    String permissionFor;

    @Column(name = "default_permission")
    String defaultPermission;

    @Column(name = "display_name")
    String displayName;

}
