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
    @ApiModelProperty(notes = "id", position = 1)
    private Integer id;

    @Column(nullable = false, name = "user_id")
    @ApiModelProperty(notes = "아이디", position = 2)
    private String userId;

    @Column(nullable = false, name = "name")
    @ApiModelProperty(notes = "이름", position = 3)
    private String name;

    @Column(nullable = false, name = "passwd")
    @ApiModelProperty(notes = "비밀번호", position = 4)
    private String passwd;

    @Column(nullable = false, name = "email")
    @ApiModelProperty(notes = "이메일", position = 5)
    private String email;

    @Column(name = "like_stock_id")
    @ApiModelProperty(notes = "관심 종목 id", position = 6)
    private Integer likeStockId;

    @Column(name = "like_stock_name")
    @ApiModelProperty(notes = "관심 종목", position = 7)
    private String likeStockName;

    @Column(name = "code")
    @ApiModelProperty(notes = "종목 코드", position = 8)
    private String code;

    @Column(name = "checked_stock")
    @ApiModelProperty(notes = "평가 종목", position = 9)
    private String checkedStock;

    @Column(nullable = false, name = "signup_date")
    @ApiModelProperty(notes = "가입 일자", position = 10)
    private Date signupDate;

    @Builder
    public Account(Integer id, String userId, String name, String passwd, String email, Integer likeStockId, String likeStockName, String code, String checkedStock, Date signupDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.passwd = passwd;
        this.email = email;
        this.likeStockId = likeStockId;
        this.likeStockName = likeStockName;
        this.code = code;
        this.checkedStock = checkedStock;
        this.signupDate = signupDate;
    }
}