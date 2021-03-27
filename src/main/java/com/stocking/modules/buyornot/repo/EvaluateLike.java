package com.stocking.modules.buyornot.repo;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluate_like")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EntityListeners(AuditingEntityListener.class)
public class EvaluateLike {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "evaluate_id")
    @ApiModelProperty(notes = "평가 ID", position = 2)
    private long evaluateId;

    @Column(name = "uid")
    @ApiModelProperty(notes = "계정 ID", position = 3)
    private String uid;
    
    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

}
