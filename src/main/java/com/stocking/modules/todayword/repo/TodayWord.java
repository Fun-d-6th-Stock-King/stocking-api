package com.stocking.modules.todayword.repo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.stocking.infra.common.BaseEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "today_word")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
public class TodayWord extends BaseEntity {
    
    private static final long serialVersionUID = -123789277314541074L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "word_name")
    @ApiModelProperty(notes = "단어명", position = 2)
    private String wordName;

    @Column(name = "mean")
    @ApiModelProperty(notes = "단어의미", position = 3)
    private String mean;
    
}
