package com.stocking.modules.account;

import com.stocking.modules.account.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "id")
    @ApiModelProperty(notes = "id")
    private Long id;

    @Column(nullable = false, name = "uuid")
    @ApiModelProperty(notes = "uuid")
    private Long uuid;

    @Column(nullable = false, name = "nickname")
    @ApiModelProperty(notes = "별칭")
    private String nickname;

    @Column(nullable = false, name = "email")
    @ApiModelProperty(notes = "이메일")
    private String email;


    @Column(nullable = false, name = "signup_date")
    @ApiModelProperty(notes = "가입 일자")
    private Date signupDate;

    @Column(name = "check_sns")
    @ApiModelProperty(notes = "소셜 연동")
    private boolean checkSns;

    @Column(name = "google_sns")
    @ApiModelProperty(notes = "google 연동")
    private boolean googleSns;

    @Column(name = "kakao_sns")
    @ApiModelProperty(notes = "kakao 연동")
    private boolean kakaoSns;

    @Column(name = "ios_sns")
    @ApiModelProperty(notes = "ios 연동")
    private boolean iosSns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Account(Long id, Long uuid, String nickname, String email, Date signupDate,
                   boolean checkSns, boolean googleSns, boolean kakaoSns, boolean iosSns, Role role) {
        this.id = id;
        this.uuid = uuid;
        this.nickname = nickname;
        this.email = email;
        this.signupDate = signupDate;
        this.checkSns = checkSns;
        this.googleSns = googleSns;
        this.kakaoSns = kakaoSns;
        this.iosSns = iosSns;
        this.role = role;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}