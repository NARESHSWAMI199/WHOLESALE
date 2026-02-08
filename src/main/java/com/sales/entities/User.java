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
public class User implements AuthUser,Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    int id;
    @Column(name = "slug")
    String slug;
    @Column(name = "otp")
    String otp;
    @Column(name = "avtar")
    String avatar;
    @Column(name = "username")
    String username;
    @JsonIgnore
    @Column(name = "password")
    String password;
    @Column(name = "email")
    String email;
    @Column(name = "contact",length = 12 , nullable = true)
    String contact;
    @Column(name = "user_type")
    String userType;
    @Column(name = "status")
    String status;
    @JsonIgnore
    @Column(name = "is_deleted")
    String isDeleted;
    @Column(name = "created_at")
    Long createdAt;
    @Column(name = "updated_at")
    Long updatedAt;
    @Column(name = "created_by")
    Integer createdBy;
    @Column(name = "updated_by")
    Integer updatedBy;
    @Column(name = "active_plan")
    Integer activePlan;

    @Column(name = "last_seen")
    Long lastSeen;

    @ManyToMany
    @JoinTable(
            name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups = new HashSet<>();


    @Transient
    private String avatarUrl;

    @Transient
    public boolean isOnline =false;

    @Transient
    public Integer chatNotification = 0;

    @Transient
    public boolean isBlocked= false;

    @Transient
    public String accepted;

    @Transient
    List<GrantedAuthority> authorities;

    public User (AuthUser loggedUser) {
        this.slug = UUID.randomUUID().toString();
        this.createdAt = getCurrentMillis();
        this.createdBy = loggedUser.getId();
        this.updatedAt = getCurrentMillis();
        this.updatedBy = loggedUser.getId();
        this.status = "A";
        this.isDeleted = "N";
    }


    public User () {
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
