package com.stocking.modules.buyornot.repo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.stocking.infra.common.BaseEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluate")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class Evaluate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4152035635258004671L;

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

    @Column(name = "pros")
    @ApiModelProperty(notes = "장점", position = 4)
    private String pros;

    @Column(name = "cons")
    @ApiModelProperty(notes = "단점", position = 5)
    private String cons;

    @Column(name = "giphy_img_id")
    @ApiModelProperty(notes = "장점", position = 6)
    private String giphyImgId;

}
