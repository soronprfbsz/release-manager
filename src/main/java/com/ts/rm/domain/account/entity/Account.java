package com.ts.rm.domain.account.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import com.ts.rm.domain.department.entity.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "avatar_style", length = 50)
    private String avatarStyle;

    @Column(name = "avatar_seed", length = 100)
    private String avatarSeed;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "position", length = 100)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_attempt_count", nullable = false)
    @Builder.Default
    private Integer loginAttemptCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 부서 변경
     */
    public void changeDepartment(Department department) {
        this.department = department;
    }

    /**
     * 직급 변경
     */
    public void changePosition(String position) {
        this.position = position;
    }
}
