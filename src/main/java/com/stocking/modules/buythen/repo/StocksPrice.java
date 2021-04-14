package com.stocking.modules.buythen.repo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.stocking.modules.stock.Stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks_price")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StocksPrice implements Serializable {

    private static final long serialVersionUID = -1293146290479381252L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "stocks_id")
    private long stocksId;

    @Column(name = "sector_yahoo")
    private String sectorYahoo;

    @Column(name = "industry_yahoo")
    private String industryYahoo;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "last_trade_date")
    private LocalDateTime lastTradeDate;

    @Column(name = "price_y1")
    private BigDecimal priceY1;

    @Column(name = "date_y1")
    private LocalDateTime dateY1;

    @Column(name = "yield_y1")
    private BigDecimal yieldY1;

    @Column(name = "price_y3")
    private BigDecimal priceY3;

    @Column(name = "date_y3")
    private LocalDateTime dateY3;

    @Column(name = "yield_y3")
    private BigDecimal yieldY3;

    @Column(name = "price_y5")
    private BigDecimal priceY5;

    @Column(name = "date_y5")
    private LocalDateTime dateY5;

    @Column(name = "yield_y5")
    private BigDecimal yieldY5;

    @Column(name = "price_y10")
    private BigDecimal priceY10;

    @Column(name = "date_y10")
    private LocalDateTime dateY10;

    @Column(name = "yield_y10")
    private BigDecimal yieldY10;

    @Column(name = "price_w1")
    private BigDecimal priceW1;

    @Column(name = "date_w1")
    private LocalDateTime dateW1;

    @Column(name = "yield_w1")
    private BigDecimal yieldW1;
    
    @Column(name = "price_m1")
    private BigDecimal priceM1;

    @Column(name = "date_m1")
    private LocalDateTime dateM1;

    @Column(name = "yield_m1")
    private BigDecimal yieldM1;

    @Column(name = "price_m6")
    private BigDecimal priceM6;

    @Column(name = "date_m6")
    private LocalDateTime dateM6;

    @Column(name = "yield_m6")
    private BigDecimal yieldM6;
    
    @Column(name = "market_cap")
    private BigDecimal marketCap;
    
    @Column(name = "stop_trading")
    private Boolean stopTrading;
    
    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stocks_id", insertable = false, updatable = false)
    private Stock stocks;
}
