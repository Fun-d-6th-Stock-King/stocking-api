package com.stocking.modules.stock;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@NoArgsConstructor
@Data
public class Stock implements Serializable {

    private static final long serialVersionUID = 6426956270036697407L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "company")
    @ApiModelProperty(notes = "회사명", position = 2)
    private String company;

    @Column(name = "code")
    @ApiModelProperty(notes = "종목코드", position = 3)
    private String code;

    @Column(name = "sector")
    @ApiModelProperty(notes = "업종", position = 4)
    private String sector;

    @Column(name = "product")
    @ApiModelProperty(notes = "주요제품", position = 5)
    private String product;

    @Column(name = "listing_date")
    @ApiModelProperty(notes = "상장일", position = 6)
    private String listingDate;

    @Column(name = "settle_month")
    @ApiModelProperty(notes = "결산월", position = 7)
    private String settleMonth;

    @Column(name = "representative")
    @ApiModelProperty(notes = "대표자명", position = 8)
    private String representative;

    @Column(name = "homepage")
    @ApiModelProperty(notes = "홈페이지", position = 9)
    private String homepage;

    @Column(name = "area")
    @ApiModelProperty(notes = "지역", position = 10)
    private String area;

    @Column(name = "market")
    @ApiModelProperty(notes = "시장구분", position = 11)
    private String market;
}
