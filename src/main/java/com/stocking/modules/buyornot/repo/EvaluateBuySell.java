package com.stocking.modules.buyornot.repo;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluate_buy_sell")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class EvaluateBuySell {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "account_id")
    @ApiModelProperty(notes = "계정 ID", position = 2)
    private long accountId;

    @Column(name = "code")
    @ApiModelProperty(notes = "종목코드", position = 3)
    private String code;

    @Column(name = "buy_sell")
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(notes = "살까 말까 평가", position = 4)
    private BuySell buySell;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public enum BuySell {
        BUY, SELL;
    }

}
