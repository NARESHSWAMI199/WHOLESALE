package com.sales.entities;


import com.sales.claims.AuthUser;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.sales.utils.Utils.getCurrentMillis;

@Entity
@Table(name = "`GROUPS`")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Group implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;

    @Column(name = "name")
    String name;

    @Column(name = "slug")
    String slug;

    @Column(name = "created_at")
    Long createdAt;

    @Column(name = "created_by")
    Integer createdBy;

    @Column(name = "updated_at")
    Long updatedAt;

    @Column(name = "updated_by")
    Integer updatedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_permissions",
            joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")
    )
    private Set<Permission> permissions = new HashSet<>();


    public Group(AuthUser loggedUser) {
        this.slug = UUID.randomUUID().toString();
        this.createdAt = getCurrentMillis();
        this.createdBy = loggedUser.getId();
        this.updatedAt = getCurrentMillis();
        this.updatedBy = loggedUser.getId();
    }


}
