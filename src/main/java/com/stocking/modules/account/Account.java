package com.stocking.modules.account;

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
    private Integer id;

    @Column(nullable = false, name = "uuid")
    @ApiModelProperty(notes = "uuid")
    private String uuid;

    @Column(nullable = false, name = "user_id")
    @ApiModelProperty(notes = "아이디")
    private String userId;

    @Column(nullable = false, name = "nickname")
    @ApiModelProperty(notes = "별칭")
    private String nickname;

    @Column(nullable = false, name = "name")
    @ApiModelProperty(notes = "이름")
    private String name;

    @Column(nullable = false, name = "passwd")
    @ApiModelProperty(notes = "비밀번호")
    private String passwd;

    @Column(nullable = false, name = "email")
    @ApiModelProperty(notes = "이메일")
    private String email;

    @Column(name = "like_stock_id")
    @ApiModelProperty(notes = "관심 종목 id")
    private Integer likeStockId;

    @Column(name = "like_stock_name")
    @ApiModelProperty(notes = "관심 종목")
    private String likeStockName;

    @Column(name = "code")
    @ApiModelProperty(notes = "종목 코드")
    private String code;

    @Column(name = "checked_stock")
    @ApiModelProperty(notes = "평가 종목")
    private String checkedStock;

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

    @Builder
    public Account(Integer id, String uuid, String userId, String nickname, String name,
                   String passwd, String email, Integer likeStockId, String likeStockName,
                   String code, String checkedStock, Date signupDate, boolean checkSns,
                   boolean googleSns, boolean kakaoSns, boolean iosSns) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.nickname = nickname;
        this.name = name;
        this.passwd = passwd;
        this.email = email;
        this.likeStockId = likeStockId;
        this.likeStockName = likeStockName;
        this.code = code;
        this.checkedStock = checkedStock;
        this.signupDate = signupDate;
        this.checkSns = checkSns;
        this.googleSns = googleSns;
        this.kakaoSns = kakaoSns;
        this.iosSns = iosSns;
    }
}