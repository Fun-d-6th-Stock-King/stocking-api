package com.stocking.modules.buythen.repo;

import java.io.Serializable;
import java.math.BigDecimal;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calc_hist")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CalcHist implements Serializable {

	private static final long serialVersionUID = -5602256297469967651L;

	@Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "code")
    private String code;

    @Column(name = "company")
    private String company;
    
    @Column(name = "invest_date")
    private LocalDateTime investDate;
    
    @Column(name = "invest_price")
    private BigDecimal investPrice;
    
    @Column(name = "yield_price")
    private BigDecimal yieldPrice;
    
    @Column(name = "yield_percent")
    private BigDecimal yieldPercent;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "created_uid")
    private String createdUid;
    
    @Column(name = "invest_date_name")
    private String investDateName;
    
    @Column(name = "sector")
    private String sector;
    
    @Column(name = "sector_kor")
    private String sectorKor;
    
}
