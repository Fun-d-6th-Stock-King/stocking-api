package com.stocking.modules.buythen.repo;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Table(name = "old_stock")
@Entity
@Data
@AllArgsConstructor
public class OldStock {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "company")
    @ApiModelProperty(notes = "회사명", example = "삼성전자")
    private String company;

    @Column(name = "code")
    @ApiModelProperty(notes = "종목코드")
    private String code;

    @Column(name = "one_year")
    @ApiModelProperty(notes = "1년 전 주가")
    private BigDecimal oneAgoStock;

    @Column(name = "five_year")
    @ApiModelProperty(notes = "5년 전 주가")
    private BigDecimal fiveAgoStock;

    @Column(name = "ten_year")
    @ApiModelProperty(notes = "10년 전 주가")
    private BigDecimal tenAgoStock;

    @Column(name = "cur_price")
    @ApiModelProperty(notes = "현재 주가")
    private BigDecimal currentStock;

    @Column(name = "stocks_id")
    @ApiModelProperty(notes = "stock ID")
    private Long stockId;
}
