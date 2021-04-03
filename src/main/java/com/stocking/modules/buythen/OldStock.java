package com.stocking.modules.buythen;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Table(name = "old_stock")
@Entity
public class OldStock {

    @Id
    @ApiModelProperty(notes = "ID")
    private Long id;

    @Column
    @ApiModelProperty(notes = "회사명", example = "삼성전자")
    private String company;

    @Column
    @ApiModelProperty(notes = "종목코드")
    private String code;

    @Column
    @ApiModelProperty(notes = "1년 전 주가")
    private Long oneAgoStock;

    @Column
    @ApiModelProperty(notes = "5년 전 주가")
    private Long fiveAgoStock;

    @Column
    @ApiModelProperty(notes = "10년 전 주가")
    private Long tenAgoStock;

    @Column
    @ApiModelProperty(notes = "현재 주가")
    private Long currentStock;

    @Column
    @ApiModelProperty(notes = "stock ID")
    private Long stockId;
}
