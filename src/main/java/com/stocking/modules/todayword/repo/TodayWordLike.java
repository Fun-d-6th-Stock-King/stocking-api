package com.stocking.modules.todayword.repo;

import java.io.Serializable;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "today_word_like")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TodayWordLike implements Serializable {
    
    private static final long serialVersionUID = -5379921222437207905L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "today_word_id")
    @ApiModelProperty(notes = "단어 id", position = 2)
    private long todayWordId;

    @Column(name = "created_uid")
    private String createdUid;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    
}
