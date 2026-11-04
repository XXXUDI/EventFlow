package com.example.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    private Integer id;

    @Column("user_id")
    private Integer userId;

    @Column("token_hash")
    private String tokenHash;

    private String device;

    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("expires_at")
    private LocalDateTime expiresAt;

}
