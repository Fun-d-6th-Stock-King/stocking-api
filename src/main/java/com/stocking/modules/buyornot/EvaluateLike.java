package com.stocking.modules.buyornot;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
@Table(name = "evaluate_like")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class EvaluateLike {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private int id;

    @Column(name = "evaluate_id")
    @ApiModelProperty(notes = "평가 ID", position = 2)
    private int evaluateId;

    @Column(name = "account_id")
    @ApiModelProperty(notes = "계정 ID", position = 3)
    private int accountId;
    
    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

}
