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
@Table(name = "stock_history")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StockHistory implements Serializable {

    private static final long serialVersionUID = 851644219211944624L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "adj_close")
    private BigDecimal adjClose;
    
    @Column(name = "close")
    private BigDecimal close;
    
    @Column(name = "date", unique = true)
    private LocalDateTime date;
    
    @Column(name = "high")
    private BigDecimal high;
    
    @Column(name = "low")
    private BigDecimal low;
    
    @Column(name = "open")
    private BigDecimal open;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "volume")
    private Long volume;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
