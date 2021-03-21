package com.stocking.modules.buyornot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.stocking.infra.common.BaseEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluate_comment")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EvaluateComment extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private int id;

    @Column(name = "evaluate_id")
    @ApiModelProperty(notes = "평가 ID", position = 2)
    private int evaluateId;

    @Column(name = "comment")
    @ApiModelProperty(notes = "코멘트", position = 4)
    private String comment;

}
