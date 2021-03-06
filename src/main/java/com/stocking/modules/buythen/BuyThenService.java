package com.stocking.modules.buythen;

import static com.stocking.modules.buythen.InvestDate.DAY1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.common.PageInfo;
import com.stocking.infra.common.StockUtils;
import com.stocking.infra.common.StockUtils.RealTimeStock;
import com.stocking.infra.common.StockUtils.StockHighLow;
import com.stocking.infra.common.StockUtils.StockHighest;
import com.stocking.infra.common.StockUtils.StockHist;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;
import com.stocking.modules.buythen.CalcAllRes.CalculatedResult;
import com.stocking.modules.buythen.CalcHistRes.CalculationHist;
import com.stocking.modules.buythen.CalculatedRes.CalculatedValue;
import com.stocking.modules.buythen.CalculatedRes.ExceptCase;
import com.stocking.modules.buythen.CurrentKospiIndustryRes.CurrentValue;
import com.stocking.modules.buythen.CurrentKospiIndustryRes.IndustryValue;
import com.stocking.modules.buythen.CurrentKospiIndustryRes.KospiValue;
import com.stocking.modules.buythen.StockRes.Company;
import com.stocking.modules.buythen.YieldSortRes.YieldSort;
import com.stocking.modules.buythen.repo.CalcHist;
import com.stocking.modules.buythen.repo.CalcHistRepository;
import com.stocking.modules.buythen.repo.QStocksPrice;
import com.stocking.modules.buythen.repo.StocksPrice;
import com.stocking.modules.buythen.repo.StocksPriceRepository;
import com.stocking.modules.stock.Stock;
import com.stocking.modules.stock.StockRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuyThenService {
    
//	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String KOSPI = "KS11";     // ????????? ?????? ??????
    private static final String SAMSUNG = "005930"; // ???????????? ?????? ??????
    private static final String SK = "000660";      // SK???????????? ?????? ??????
    private static final String KAKAO = "035720";   // ????????? ?????? ??????

    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private StocksPriceRepository stocksPriceRepository;
    
    @Autowired
    private StockUtils stockUtils;
    
    @Autowired
    private CalcHistRepository calcHistRepository;
    
    @Autowired
    private JPAQueryFactory queryFactory;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * kospi ???????????? ?????? ??????
     * @return
     * @throws Exception 
     */
    @Cacheable(value = "stockList")
    public StockRes getStockList() throws Exception {
        
        List<StocksPrice> stockPriceList = stocksPriceRepository.findAllByIdNotIn(List.of(804L), Sort.by(Direction.DESC, "marketCap"))
                .orElseThrow(() -> new Exception("?????? ??????"));
        
        List<Company> resultList = stockPriceList
            .stream().map(vo -> {
                
                return Company.builder()
                    .company(vo.getCompany())
                    .code(vo.getCode())
                    .build();
            }).collect(Collectors.toList());
        
        return StockRes.builder()
            .companyList(resultList)
            .count(resultList.size())
            .build();
    }
    
    /**
     * ?????????
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public CalculatedRes getPastStock(BuyThenForm buyThenForm, FirebaseUser user) throws Exception {
        Stock stock = stockRepository.findByCode(buyThenForm.getCode())
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        
        StocksPrice stockPrice = stocksPriceRepository.findByStocksId(stock.getId())
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        
        String code = stock.getCode();                          // ?????? ??????
        InvestDate investDate = buyThenForm.getInvestDate();    // ?????? ??????
        BigDecimal investPrice = buyThenForm.getInvestPrice();  // ?????????

        Boolean isExceptCase;                                   // ?????? ????????? ??????

        // ?????? ?????? ??????
        Boolean isTradingHalt = stockPrice.getTradingHalt();
        Boolean isInvestmentAlert = stockPrice.getInvestmentAlert();
        Boolean isManagement = stockPrice.getManagement();
        Boolean isStockExcept = isTradingHalt || isInvestmentAlert || isManagement;
        isExceptCase = isStockExcept;

        // ?????? ??????
        List<InvestDate> investDates = new ArrayList<InvestDate>(EnumSet.allOf(InvestDate.class));
        int investDateIndex = investDates.indexOf(investDate);
        InvestDate newInvestDate = investDate;
        Boolean isDateExceptCase = Boolean.FALSE;

        Optional<BigDecimal> oldStockPrice = Optional.empty();
        for (int i=investDateIndex;i>=0;i--) {
            oldStockPrice = Optional.ofNullable( switch (newInvestDate) {
                case DAY1 -> stockPrice.getPrice();
                case WEEK1 -> stockPrice.getPriceW1();
                case MONTH1 -> stockPrice.getPriceM1();
                case MONTH6 -> stockPrice.getPriceM6();
                case YEAR1 -> stockPrice.getPriceY1();
                case YEAR5 -> stockPrice.getPriceY5();
                case YEAR10 -> stockPrice.getPriceY10();
                default -> throw new IllegalArgumentException("Unexpected value: " + newInvestDate);
            });
            if (oldStockPrice.isPresent()) {
                break;
            }
            newInvestDate = investDates.get(i);
            isDateExceptCase = Boolean.TRUE;
            isExceptCase = Boolean.TRUE;
        }
        if (!oldStockPrice.isPresent()) {
            throw new Exception(
                    stock.getCompany() + " ??? ?????? ???????????? ???????????? ????????????."
            );
        }

        // ????????? ?????? ??????
        BigDecimal newInvestPrice = investPrice;
        Boolean isPriceExcept = Boolean.FALSE;
        if (oldStockPrice.get().compareTo(investPrice) > 0) {
            newInvestPrice = oldStockPrice.get();
            isExceptCase = Boolean.TRUE;
            isPriceExcept = Boolean.TRUE;
        }

        // ????????? ??????
        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);

        BigDecimal currentPrice = realTimeStock.getCurrentPrice(); // ????????? - ??????????????? ??????
        String lastTradeTime = realTimeStock.getLastTradeTime();

        BigDecimal holdingStock = newInvestPrice  // ?????? ??? ?????? ??????
                .divide(oldStockPrice.get(), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal yieldPercent = currentPrice.subtract(oldStockPrice.get()) // (?????????-????????????)/???????????? * 100
                .divide(oldStockPrice.get(), MathContext.DECIMAL32)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal yieldPrice = newInvestPrice.add(
                newInvestPrice
                .multiply(yieldPercent)
                .divide(new BigDecimal(100)));  // ????????? = ????????? + (?????????*?????????*100)


        // ??????/??????, ??????/sk/kakao ?????? ??????, ????????????
        BigDecimal salaryYear = null;   // ??????
        BigDecimal salaryMonth = null;  // ??????

        BigDecimal samsungStock = null; // ?????? ?????? ???
        BigDecimal skStock = null;      // sk ?????? ???
        BigDecimal kakaoStock = null;   // kakao ?????? ???
        StocksPrice samsungStockPrice = stocksPriceRepository.findByCode(SAMSUNG)
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        StocksPrice skStockPrice = stocksPriceRepository.findByCode(SK)
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        StocksPrice kakaoStockPrice = stocksPriceRepository.findByCode(KAKAO)
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));

        LocalDateTime oldCloseDate;     // ????????????

        switch (newInvestDate) {
            case DAY1 :
                // ??????
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice;
                // ??????, sk, ?????????
                samsungStock = samsungStockPrice.getPrice();
                skStock = skStockPrice.getPrice();
                kakaoStock = kakaoStockPrice.getPrice();

                // ????????????
                oldCloseDate = stockPrice.getLastTradeDate();
                break;
            case WEEK1 :
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice;
                samsungStock = samsungStockPrice.getPriceW1();
                skStock = skStockPrice.getPriceW1();
                kakaoStock = kakaoStockPrice.getPriceW1();
                oldCloseDate = stockPrice.getDateW1();
                break;
            case MONTH1 :
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice;
                samsungStock = samsungStockPrice.getPriceM1();
                skStock = skStockPrice.getPriceM1();
                kakaoStock = kakaoStockPrice.getPriceM1();
                oldCloseDate = stockPrice.getDateM1();
                break;
            case MONTH6 :
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice.divide(new BigDecimal(6), MathContext.DECIMAL32);
                samsungStock = samsungStockPrice.getPriceM6();
                skStock = skStockPrice.getPriceM6();
                kakaoStock = kakaoStockPrice.getPriceM6();
                oldCloseDate = stockPrice.getDateM6();
                break;
            case YEAR1 :
                salaryYear = yieldPrice;
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                samsungStock = samsungStockPrice.getPriceY1();
                skStock = skStockPrice.getPriceY1();
                kakaoStock = kakaoStockPrice.getPriceY1();
                oldCloseDate = stockPrice.getDateY1();
                break;
            case YEAR5 :
                salaryYear = yieldPrice.divide(new BigDecimal(5), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                samsungStock = samsungStockPrice.getPriceY5();
                skStock = skStockPrice.getPriceY5();
                kakaoStock = kakaoStockPrice.getPriceY5();
                oldCloseDate = stockPrice.getDateY5();
                break;
            case YEAR10 :
                salaryYear = yieldPrice.divide(new BigDecimal(10), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                samsungStock = samsungStockPrice.getPriceY10();
                skStock = skStockPrice.getPriceY10();
                kakaoStock = kakaoStockPrice.getPriceY10();
                oldCloseDate = stockPrice.getDateY10();
                break;
            default : throw new IllegalArgumentException("Unexpected value: " + newInvestDate);
        }
        samsungStock = newInvestPrice
                .divide(samsungStock, MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
        skStock = newInvestPrice
                .divide(skStock, MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
        kakaoStock = newInvestPrice
                .divide(kakaoStock, MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);

        // ???????????? ??????
        calcHistRepository.save(
    		CalcHist.builder()
    			.code(code)
    			.company(stockPrice.getCompany())
    			.createdUid(user.getUid())
    			.investDate(oldCloseDate)
    			.investDateName(newInvestDate.getName())
    			.investPrice(newInvestPrice)
    			.yieldPrice(yieldPrice)
    			.yieldPercent(yieldPercent)
    			.price(currentPrice)
    			.sector(stockPrice.getSectorYahoo())
    			.sectorKor(stockPrice.getSectorKor())
    			.build()
		);
        
        return CalculatedRes.builder()
            .code(code)
            .company(stock.getCompany())
            .currentPrice(currentPrice)
            .lastTradingDateTime(lastTradeTime)
            .calculatedValue(
                CalculatedValue.builder()
                    .investPrice(newInvestPrice)
                    .investDate(newInvestDate.getName())
                    .oldPrice(oldStockPrice.get())
                    .yieldPrice(yieldPrice)
                    .yieldPercent(yieldPercent)
                    .oldCloseDate(oldCloseDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .holdingStock(holdingStock)
                    .salaryYear(salaryYear)
                    .salaryMonth(salaryMonth)
                    .samsungStock(samsungStock)
                    .skStock(skStock)
                    .kakaoStock(kakaoStock)
                    .build())
            .exceptCase(
                ExceptCase.builder()
                    .isExceptCase(isExceptCase)
                    .isDateExcept(isDateExceptCase)
                    .oldInvestDate(investDate)
                    .newInvestDate(newInvestDate)
                    .isPriceExcept(isPriceExcept)
                    .oldInvestPrice(investPrice)
                    .newInvestPrice(newInvestPrice)
                    .isStockExcept(isStockExcept)
                    .isTradingHalt(isTradingHalt)
                    .isInvestmentAlert(isInvestmentAlert)
                    .isManagement(isManagement)
                    .build())
            .build();
        
    }

    /**
     * kospi, ????????????, ????????? ?????? ??????
     * @return
     * @throws Exception
     */

    public CurrentKospiIndustryRes getCurrentKospiIndustry(BuyThenForm buyThenForm) throws Exception {
        CurrentKospiIndustryRes result;

        // ??????
        String code = buyThenForm.getCode(); // ????????? ?????? ??????
        Stock stock = stockRepository.findByCode(code)
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));

        InvestDate investDate = buyThenForm.getInvestDate(); // ?????? ???

        // ???????????? ?????? ?????????
        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);  // ????????? ?????? ??????
        BigDecimal pricePerStock = realTimeStock.getCurrentPrice();   // ????????? ??????
        BigDecimal stocksPerPrice = buyThenForm.getInvestPrice()      // ?????? ?????? ??????
                .divide(pricePerStock, MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_EVEN);

        // ?????????
        String kosCode = KOSPI; // ????????? ?????? ??????
        Stock kosStock = stockRepository.findByCode(kosCode)
                .orElseThrow(() -> new Exception(
                        "????????? ????????????(" + kosCode + ")??? ???????????? ????????????.")
                );
        StocksPrice kosStockPrice = stocksPriceRepository.findByStocksId(kosStock.getId())
                .orElseThrow(() -> new Exception(
                        "????????? ????????????(" + kosCode + ")??? ???????????? ????????????.")
                );

        BigDecimal kosOldPrice; // ????????? ?????? ??????
        String oldDate;         // ????????? ?????? ??????
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.");
        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        NumberPath<BigDecimal> datePriceField; // ?????? ?????? ????????? ?????? ?????? ??????
        switch(investDate) {
            case DAY1 -> {
                kosOldPrice = kosStockPrice.getPrice();
                oldDate = kosStockPrice.getLastTradeDate().format(dateFormatter);
                datePriceField = qStocksPrice.price;
            }
            case WEEK1 -> {
                kosOldPrice = kosStockPrice.getPriceW1();
                oldDate = kosStockPrice.getDateW1().format(dateFormatter);
                datePriceField = qStocksPrice.priceW1;
            }
            case MONTH1 -> {
                kosOldPrice = kosStockPrice.getPriceM1();
                oldDate = kosStockPrice.getDateM1().format(dateFormatter);
                datePriceField = qStocksPrice.priceM1;
            }
            case MONTH6 -> {
                kosOldPrice = kosStockPrice.getPriceM6();
                oldDate = kosStockPrice.getDateM6().format(dateFormatter);
                datePriceField = qStocksPrice.priceM6;
            }
            case YEAR1 -> {
                kosOldPrice = kosStockPrice.getPriceY1();
                oldDate = kosStockPrice.getDateY1().format(dateFormatter);
                datePriceField = qStocksPrice.priceY1;
            }
            case YEAR5 -> {
                kosOldPrice = kosStockPrice.getPriceY5();
                oldDate = kosStockPrice.getDateY5().format(dateFormatter);
                datePriceField = qStocksPrice.priceY5;
            }
            case YEAR10 -> {
                kosOldPrice = kosStockPrice.getPriceY10();
                oldDate = kosStockPrice.getDateY10().format(dateFormatter);
                datePriceField = qStocksPrice.priceY10;
            }
            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + investDate
            );
        }

        RealTimeStock kosRealTimeStock = stockUtils.getStockInfo(kosCode);
        BigDecimal kosCurrentPrice = kosRealTimeStock.getCurrentPrice();    // ????????? ?????? ??????
        BigDecimal kosYieldPercent = kosCurrentPrice.subtract(kosOldPrice). // ????????? ?????????
                divide(kosOldPrice, MathContext.DECIMAL32).
                multiply(new BigDecimal(100));

        // ?????? ??????
        StocksPrice stocksPrice = stocksPriceRepository.findByStocksId(stock.getId())
                .orElseThrow(() -> new Exception("?????? ????????? ???????????? ????????????."));
        String sector = stocksPrice.getSectorYahoo();   // ?????? ?????????

        List<Tuple> industryList = queryFactory
                .select(qStocksPrice.code,
                        qStocksPrice.company,
                        datePriceField)
                .from(qStocksPrice)
                .where(qStocksPrice.sectorYahoo.eq(sector))
                .orderBy(qStocksPrice.marketCap.desc())
                .fetch();

        List<String> industryCodes = new ArrayList<String>();   // ?????? ?????? id ?????????
        List<Company> mainCompanies = new ArrayList<Company>();                            // ?????? ?????? 4??????
        int cnt = 0;
        BigDecimal oldSumPrice = new BigDecimal(0);         // ?????? ???????????? ?????? ???
        for (Tuple tuple : industryList) {
            cnt++;
            // ???????????? ???????????? ?????????
            industryCodes.add(tuple.get(qStocksPrice.code) + ".KS");

            // ?????? ?????? 4??????
            if (cnt <= 4) {
                Company company = Company.builder()
                        .code(tuple.get(qStocksPrice.code))
                        .company(tuple.get(qStocksPrice.company))
                        .build();
                mainCompanies.add(company);
            }

            // ?????? ???????????? ?????? ???
            BigDecimal oldPrice = tuple.get(datePriceField);
            if (oldPrice != null) {
            oldSumPrice = oldSumPrice.add(tuple.get(datePriceField));
            }
        }

        // ???????????? ????????? ??????
        int industryNum = industryList.size();
        String[] industryCodesArray = industryCodes.toArray(new String[industryNum]);
        BigDecimal newSumPrice = stockUtils.getCurrentSumPrice(industryCodesArray);
        BigDecimal industryYieldPercent = newSumPrice.subtract(oldSumPrice) // (???????????????-???????????????)/??????????????? * 100
                .divide(oldSumPrice, MathContext.DECIMAL32)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_EVEN);
        industryNum = industryNum >= 4 ? industryNum - 4 : 0;

        // Build
        result = CurrentKospiIndustryRes.builder()
                .code(code)
                .company(stock.getCompany())
                .currentValue(
                        CurrentValue.builder()
                                .pricePerStock(pricePerStock)
                                .stockPerPrice(stocksPerPrice)
                                .currentTime(realTimeStock.getCurrentTime())
                                .build())
                .kospiValue(
                        KospiValue.builder()
                        .yieldPercent(kosYieldPercent)
                        .oldDate(oldDate)
                        .oldStock(kosOldPrice)
                        .currentStock(kosCurrentPrice)
                        .currentTime(kosRealTimeStock.getCurrentTime())
                        .build())
                .industryValue(
                        IndustryValue.builder()
                        .sector(sector)
                        .sectorKor(stocksPrice.getSectorKor())
                        .yieldPercent(industryYieldPercent)
                        .companies(mainCompanies)
                        .companyCnt(industryNum)
                        .build()
                )
                .build();

        return result;
    }
    
    /**
     * ?????? ???????????? ???????????? ?????? ??????
     * @param PageParam
     * @return
     * @throws Exception
     */
    public CalcHistRes getCalculationHistory(int pageSize, int pageNo) {
        Page<CalcHist> page = calcHistRepository.findAll(PageRequest.of(pageNo - 1,
                pageSize, Sort.by(Direction.DESC, "createdDate")));

        List<CalculationHist> calculationHistList = page.getContent()
            .stream()
            .map(vo -> {
                return CalculationHist.builder()
                    .id(vo.getId())
                    .code(vo.getCode())
                    .company(vo.getCompany())
                    .createdUid(vo.getCreatedUid())
                    .createdDate(vo.getCreatedDate())
                    .investDate(vo.getInvestDate())
                    .investDateName(vo.getInvestDateName())
                    .investPrice(vo.getInvestPrice())
                    .price(vo.getPrice())
                    .sector(vo.getSector())
                    .sectorKor(vo.getSectorKor())
                    .yieldPrice(vo.getYieldPrice())
                    .yieldPercent(vo.getYieldPercent())
                    .build();
            }).collect(Collectors.toList());

        return CalcHistRes
                .builder()
                .calculationHistList(calculationHistList)
                .pageInfo(
                    PageInfo.builder()
                        .count(page.getTotalElements())
                        .pageNo(pageNo)
                        .pageSize(page.getSize())
                        .build()
                )
                .build();
    }
    
    /**
     * ????????? ???????????? ???????????? ?????? ??????(????????????, ????????????)
     * @param pageParam
     * @return
     */
    public YieldSortRes getYieldSortList(InvestDate investDate, BuySell buySell, int pageSize, int pageNo) {
        if(DAY1 == investDate) {
            // 1??? ??? ???????????? ?????? ????????? ???????????? ???????????? ?????? ???????????? ???????????? ??????...? 
            return YieldSortRes.builder().build();
        }
        
        // q class
        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        
        NumberPath<BigDecimal> oldPrice = qStocksPrice.priceW1;
        DateTimePath<LocalDateTime> oldDate = qStocksPrice.dateW1;
        NumberPath<BigDecimal> yieldPercent = qStocksPrice.yieldW1;
        
        switch (investDate) {
            case WEEK1 -> {}
            case MONTH1 -> {
                oldPrice = qStocksPrice.priceM1;
                oldDate = qStocksPrice.dateM1;
                yieldPercent = qStocksPrice.yieldM1;
            }
            case MONTH6 -> {
                oldPrice = qStocksPrice.priceM6;
                oldDate = qStocksPrice.dateM6;
                yieldPercent = qStocksPrice.yieldM6;
            }
            case YEAR1 -> {
                oldPrice = qStocksPrice.priceY1;
                oldDate = qStocksPrice.dateY1;
                yieldPercent = qStocksPrice.yieldY1;
            }
            case YEAR5 -> {
                oldPrice = qStocksPrice.priceY5;
                oldDate = qStocksPrice.dateY5;
                yieldPercent = qStocksPrice.yieldY5;
            }
            case YEAR10 -> {
                oldPrice = qStocksPrice.priceY10;
                oldDate = qStocksPrice.dateY10;
                yieldPercent = qStocksPrice.yieldY10;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        OrderSpecifier<?> order = switch (buySell) {
            case BUY -> yieldPercent.desc();
            case SELL -> yieldPercent.asc();
            default -> throw new IllegalArgumentException("Unexpected value: " + buySell);
        };
        
        List<YieldSort> yieldSortList = queryFactory.select(
            Projections.fields(YieldSort.class,
                qStocksPrice.id,
                qStocksPrice.code,
                qStocksPrice.company,
                qStocksPrice.sectorYahoo.as("sector"),
                qStocksPrice.sectorYahoo.as("sectorKor"),
                oldPrice.as("oldPrice"),
                oldDate.as("oldDate"),
                yieldPercent.as("yieldPercent"),
                qStocksPrice.updatedDate.as("updatedDate"),
                qStocksPrice.price.as("price")
            )
        ).from(qStocksPrice)
        .where(oldDate.isNotNull().and(qStocksPrice.tradingHalt.eq(false)))
        .orderBy(order)
        .offset((pageNo - 1) * pageSize)
        .limit(pageSize)
        .fetch();
        
        long count = queryFactory.selectFrom(qStocksPrice)
            .where(oldDate.isNotNull().and(qStocksPrice.tradingHalt.eq(false)))
            .fetchCount();
        
        return YieldSortRes.builder()
                .updatedDate(yieldSortList.get(0).getUpdatedDate())
                .yieldSortList(yieldSortList)
                .pageInfo(
                    PageInfo.builder()
                        .count(count)
                        .pageNo(pageNo)
                        .pageSize(pageSize)
                        .build()
                ).build();
    }
    
    /**
     * ????????? ????????? ?????? - historycal ????????? ???????????? ??????(????????????) 
     * ?????????, ??????????????? ???????????? ????????? ?????????, historical price(10??????, ???????????? ?????????)
     * @throws IOException
     */
    public StockHist getKospiChart() throws IOException {
        return stockUtils.getStockHist("KS11");
    }
    
    /**
     * ???????????? ??????????????? ????????? ????????? ??????????????? ????????? ??????
     * max - pageSize 10 ?????? pageNo 100?????? ??????, pageSize 100 ?????? pageNo 10?????? ??????, 
     * @param query
     * @param pageNo
     * @param pageSize
     * @return
     * @throws UnsupportedEncodingException 
     */
    public NewsRes getNaverNews(String query, int pageNo, int pageSize) throws UnsupportedEncodingException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-Naver-Client-Id", "bQfZm07mCJv9mn22P4hG");
        headers.set("X-Naver-Client-Secret", "zcDnZCYohs");

        
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://openapi.naver.com/v1/search/news.json")
                .queryParam("query", query)
                .queryParam("display", pageSize) // 10(?????????), 100(??????)
                .queryParam("start", ((pageNo - 1) * pageSize) + 1)    // offset ??? - 1(?????????), 1000(??????)
                .queryParam("sort", "date").encode(StandardCharsets.UTF_8);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<NewsRes> response = restTemplate.exchange(
                builder.toUriString(), 
                HttpMethod.GET, 
                entity, 
                NewsRes.class);
        
        log.info(builder.toUriString());
        
        return response.getBody();
    }
    
    /**
     * ??????(005930),sk????????????(000660),?????????(035720),???????????????(005380) 4?????? ????????? 10??? ??? ????????? ??????, ??????, ????????? 
     * @throws IOException 
     */
    public List<HighPriceRes> getHighPrice() {
        
        List<StocksPrice> stockList = stocksPriceRepository
                .findByCodeInOrderByMarketCapDesc("005930", "000660", "035720", "005380");
        
        return stockList.stream()
            .map(vo -> {
                StockHist stockHist = stockUtils.getStockHist(vo.getCode());
                try {
                    return HighPriceRes.builder()
                        .code(vo.getCode())
                        .company(vo.getCompany())
                        .sector(vo.getSectorYahoo())
                        .sectorKor(vo.getSectorKor())
                        .currentPrice(stockUtils.getStockInfo(vo.getCode()).getCurrentPrice())
                        .maxQuote(stockHist.getMaxQuote())
                        .build();
                } catch (IOException exception) {
                    throw new IllegalArgumentException("Unexpected : stockUtils.getStockInfo failed");
                }
            }).collect(Collectors.toList());
    }
    
    /**
     * ????????? ??????, ??????, ?????? ??????, ????????? ????????????.
     * @return
     */
    public List<StockHighLow> getHighLow() {
        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        
        List<StockHighLow> result = new ArrayList<>();
        
        List<String> codeList = queryFactory.select(qStocksPrice.code).from(qStocksPrice)
                .orderBy(NumberExpression.random().asc())
                .limit(4)
                .fetch();
        
        codeList.forEach(code -> result.add(stockUtils.getStockHighLow(code)));
        
        return result ;
    }
    
    /**
     * ??????, ?????????,  
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public CalcAllRes getAllDateResult(String code, BigDecimal investPrice) throws Exception {
        StocksPrice stockPrice = stocksPriceRepository.findByCode(code)
                .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        
        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);

        BigDecimal currentPrice = realTimeStock.getCurrentPrice(); // ????????? - ??????????????? ??????
        String lastTradeTime = realTimeStock.getLastTradeTime();
        
        return CalcAllRes.builder()
            .code(code)
            .company(stockPrice.getCompany())
            .currentPrice(currentPrice)
            .lastTradingDateTime(lastTradeTime)
            .day1(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.DAY1))
            .week1(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.WEEK1))
            .month1(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.MONTH1))
            .month6(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.MONTH6))
            .year1(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.YEAR1))
            .year10(getCalcResult(stockPrice, realTimeStock, investPrice, InvestDate.YEAR10))
            .build();
    }
    
    /**
     * ???????????? ????????????
     * @param stockPrice - ?????? ??????
     * @param realTimeStock - ????????????
     * @param investPrice - ?????????
     * @param investDate - ????????????
     * @return
     */
    private CalculatedResult getCalcResult(StocksPrice stockPrice, RealTimeStock realTimeStock, BigDecimal investPrice, InvestDate investDate) {
        BigDecimal currentPrice = realTimeStock.getCurrentPrice(); // ????????? - ??????????????? ??????
        
        BigDecimal oldStockPrice = switch (investDate) {
            case DAY1 -> stockPrice.getPrice();
            case WEEK1 -> stockPrice.getPriceW1();
            case MONTH1 -> stockPrice.getPriceM1();
            case MONTH6 -> stockPrice.getPriceM6();
            case YEAR1 -> stockPrice.getPriceY1();
            case YEAR5 -> stockPrice.getPriceY5();
            case YEAR10 -> stockPrice.getPriceY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        if(oldStockPrice == null) return null;  // 
        
        // ????????????
        LocalDateTime oldCloseDate = switch (investDate) {
            case DAY1 -> stockPrice.getLastTradeDate();
            case WEEK1 -> stockPrice.getDateW1();
            case MONTH1 -> stockPrice.getDateM1();
            case MONTH6 -> stockPrice.getDateM6();
            case YEAR1 -> stockPrice.getDateY1();
            case YEAR5 -> stockPrice.getDateY5();
            case YEAR10 -> stockPrice.getDateY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };

        // ????????? ??????
        BigDecimal holdingStock = investPrice.divide(oldStockPrice, MathContext.DECIMAL32);     // ?????? ??? ?????? ??????
        BigDecimal yieldPercent = currentPrice.subtract(oldStockPrice).divide(oldStockPrice, MathContext.DECIMAL32).multiply(new BigDecimal(100));  // (?????????-????????????)/???????????? * 100
        BigDecimal yieldPrice = investPrice.add(investPrice.multiply(yieldPercent).divide(new BigDecimal(100)));  // ????????? = ????????? + (?????????*?????????*100)

        return CalculatedResult.builder()
                .investPrice(investPrice)
                .investDate(investDate.getName())
                .oldPrice(oldStockPrice)
                .yieldPrice(yieldPrice)
                .yieldPercent(yieldPercent)
                .oldCloseDate(oldCloseDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .holdingStock(holdingStock)
                .build();
    }
    
    /**
     * ????????? ???????????? ????????? ????????? ??????
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public CalcHighestRes getHighestPrice(BuyThenForm buyThenForm) throws Exception {
        InvestDate investDate = buyThenForm.getInvestDate();
        BigDecimal investPrice = buyThenForm.getInvestPrice();
        
        StocksPrice stockPrice = stocksPriceRepository.findByCode(buyThenForm.getCode())
            .orElseThrow(() -> new Exception("??????????????? ???????????? ????????????."));
        
        BigDecimal oldStockPrice = switch (investDate) {
            case DAY1 -> stockPrice.getPrice();
            case WEEK1 -> stockPrice.getPriceW1();
            case MONTH1 -> stockPrice.getPriceM1();
            case MONTH6 -> stockPrice.getPriceM6();
            case YEAR1 -> stockPrice.getPriceY1();
            case YEAR5 -> stockPrice.getPriceY5();
            case YEAR10 -> stockPrice.getPriceY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        if(oldStockPrice == null) {
            investDate = getExistMaxDate(stockPrice);
            
            oldStockPrice = switch (investDate) {
                case DAY1 -> stockPrice.getPrice();
                case WEEK1 -> stockPrice.getPriceW1();
                case MONTH1 -> stockPrice.getPriceM1();
                case MONTH6 -> stockPrice.getPriceM6();
                case YEAR1 -> stockPrice.getPriceY1();
                case YEAR5 -> stockPrice.getPriceY5();
                case YEAR10 -> stockPrice.getPriceY10();
                default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
            };
        }
        
        LocalDateTime oldCloseDate = switch (investDate) {
            case DAY1 -> stockPrice.getLastTradeDate();
            case WEEK1 -> stockPrice.getDateW1();
            case MONTH1 -> stockPrice.getDateM1();
            case MONTH6 -> stockPrice.getDateM6();
            case YEAR1 -> stockPrice.getDateY1();
            case YEAR5 -> stockPrice.getDateY5();
            case YEAR10 -> stockPrice.getDateY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };

        // ????????? ????????? ??????
        StockHighest stockHighest = stockUtils.getStockHighest(buyThenForm.getCode(), investDate);
        
        // ????????? ??????
        BigDecimal yieldPercent = stockHighest.getMaxQuote().getHigh().subtract(oldStockPrice).divide(oldStockPrice, MathContext.DECIMAL32).multiply(new BigDecimal(100));  // (?????????-????????????)/???????????? * 100
        BigDecimal yieldPrice = investPrice.add(investPrice.multiply(yieldPercent).divide(new BigDecimal(100)));  // ????????? = ????????? + (?????????*?????????*100)
        
        // ?????? ??????
        LocalDateTime endDateTime = stockHighest.getMaxQuote().getDate();
        long diff = ChronoUnit.DAYS.between(oldCloseDate, endDateTime);
        long week = diff / 7L; 
        String period = String.format("%d???(%d???)", week, diff);
        
        return CalcHighestRes.builder()
            .code(buyThenForm.getCode())
            .investPrice(buyThenForm.getInvestPrice())
            .investDate(investDate.getName())
            .investStartDate(oldCloseDate)
            .investEndDate(endDateTime)
            .yieldPercent(yieldPercent)
            .yieldPrice(yieldPrice)
            .investPeriod(period)
            .build();
    }
    
    /**
     * ????????? ???????????? ?????? ??????
     * @param stockPrice
     * @return
     */
    private InvestDate getExistMaxDate(StocksPrice stockPrice) {
        if(stockPrice.getPriceY10() != null) return InvestDate.YEAR10;
        if(stockPrice.getPriceY5() != null) return InvestDate.YEAR5;
        if(stockPrice.getPriceY1() != null) return InvestDate.YEAR1;
        if(stockPrice.getPriceM6() != null) return InvestDate.MONTH1;
        if(stockPrice.getPriceM1() != null) return InvestDate.MONTH6;
        if(stockPrice.getPriceW1() != null) return InvestDate.WEEK1;
        return InvestDate.DAY1;
    }
}