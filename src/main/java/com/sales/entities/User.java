package com.sales.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sales.claims.AuthUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.sales.utils.Utils.getCurrentMillis;


@Getter
@Setter
@AllArgsConstructor

@Entity
@Table(name = "`users`")
@SQLRestriction("is_deleted != 'Y' ")
@Builder
public class User implements AuthUser, Serializable {
    @Transient
    private boolean isOnline = false;
    @Transient
    private Integer chatNotification = 0;
    @Transient
    private boolean isBlocked = false;
    @Transient
    private String accepted;
    @Transient
    private List<GrantedAuthority> authorities;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;
    @Column(name = "slug")
    private String slug;
    @Column(name = "otp")
    private String otp;
    @Column(name = "avtar")
    private String avatar;
    @Column(name = "username")
    private String username;
    @JsonIgnore
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "contact", length = 12, nullable = true)
    private String contact;
    @Column(name = "user_type")
    private String userType;
    @Column(name = "status")
    private String status;
    @JsonIgnore
    @Column(name = "is_deleted")
    private String isDeleted;
    @Column(name = "created_at")
    private Long createdAt;
    @Column(name = "updated_at")
    private Long updatedAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_by")
    private Integer updatedBy;
    @Column(name = "active_plan")
    private Integer activePlan;
    @Column(name = "last_seen")
    private Long lastSeen;
    @ManyToMany
    @JoinTable(
            name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups = new HashSet<>();
    @Transient
    private String avatarUrl;

    public User(AuthUser loggedUser) {
        this.slug = UUID.randomUUID().toString();
        this.createdAt = getCurrentMillis();
        this.createdBy = loggedUser.getId();
        this.updatedAt = getCurrentMillis();
        this.updatedBy = loggedUser.getId();
        this.status = "A";
        this.isDeleted = "N";
    }


    public User() {
        this.slug = UUID.randomUUID().toString();
        this.createdAt = getCurrentMillis();
        this.updatedAt = getCurrentMillis();
        this.status = "A";
        this.isDeleted = "N";
    }

    @Override
    public boolean isEnabled() {
        return "A".equals(this.status);
    }
}
