package com.stocking.modules.buythen;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.common.PageInfo;
import com.stocking.infra.common.StockUtils;
import com.stocking.infra.common.StockUtils.RealTimeStock;
import com.stocking.infra.common.StockUtils.StockHist;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;
import com.stocking.modules.buythen.CalcHistRes.CalculationHist;
import com.stocking.modules.buythen.CalculatedRes.CalculatedValue;
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
    
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; 

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
     * kospi 상장기업 전체 조회
     * @return
     * @throws Exception 
     */
    @Cacheable(value = "stockList")
    public StockRes getStockList() throws Exception {
        
        List<StocksPrice> stockPriceList = stocksPriceRepository.findAllByIdNotIn(List.of(804L), Sort.by(Direction.DESC, "marketCap"))
                .orElseThrow(() -> new Exception("조회 실패"));
        
        List<Company> resultList = stockPriceList
            .stream().map(vo -> {
                List<InvestDate> vaildDateList = new ArrayList<>();
                if(vo.getPrice() != null) vaildDateList.add(InvestDate.DAY1);
                if(vo.getPriceW1() != null) vaildDateList.add(InvestDate.WEEK1);
                if(vo.getPriceM1() != null) vaildDateList.add(InvestDate.MONTH1);
                if(vo.getPriceM6() != null) vaildDateList.add(InvestDate.MONTH6);
                if(vo.getPriceY1() != null) vaildDateList.add(InvestDate.YEAR1);
                if(vo.getPriceY5() != null) vaildDateList.add(InvestDate.YEAR5);
                if(vo.getPriceY10() != null) vaildDateList.add(InvestDate.YEAR10);
                
                return Company.builder()
                    .company(vo.getCompany())
                    .code(vo.getCode())
                    .vaildDateList(vaildDateList)
                    .build();
            }).collect(Collectors.toList());
        
        return StockRes.builder()
            .companyList(resultList)
            .count(resultList.size())
            .build();
    }
    
    /**
     * 계산기
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public CalculatedRes getPastStock(BuyThenForm buyThenForm, FirebaseUser user) throws Exception {
        Stock stock = stockRepository.findByCode(buyThenForm.getCode())
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));
        
        StocksPrice stockPrice = stocksPriceRepository.findByStocksId(stock.getId())
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));
        
        String code = stock.getCode();
        InvestDate investDate = buyThenForm.getInvestDate();
        BigDecimal investPrice = buyThenForm.getInvestPrice();    // 투자금

        // 과거 주가
        BigDecimal oldStockPrice = Optional.ofNullable( switch (investDate) {
            case DAY1 -> stockPrice.getPrice();
            case WEEK1 -> stockPrice.getPriceW1();
            case MONTH1 -> stockPrice.getPriceM1();
            case MONTH6 -> stockPrice.getPriceM6();
            case YEAR1 -> stockPrice.getPriceY1();
            case YEAR5 -> stockPrice.getPriceY5();
            case YEAR10 -> stockPrice.getPriceY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        }).orElseThrow(() -> new Exception(stock.getCompany() + " 는(은) " + investDate.getName() + " 데이터가 없습니다."));
        
        // 종가일자
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

        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);
        
        BigDecimal currentPrice = realTimeStock.getCurrentPrice(); // 현재가 - 실시간정보 호출
        String lastTradeTime = realTimeStock.getLastTradeTime();
        
        BigDecimal holdingStock = investPrice.divide(oldStockPrice, MathContext.DECIMAL32);     // 내가 산 주식 개수 
        BigDecimal yieldPercent = currentPrice.subtract(oldStockPrice).divide(oldStockPrice, MathContext.DECIMAL32).multiply(new BigDecimal(100));  // (현재가-이전종가)/이전종가 * 100
        BigDecimal yieldPrice = investPrice.add(investPrice.multiply(yieldPercent).divide(new BigDecimal(100)));  // 수익금 = 투자금 + (투자금*수익률*100)

        BigDecimal salaryYear = null;      // 연봉
        BigDecimal salaryMonth = null;     // 월급
        
        switch (investDate) {
            case DAY1, WEEK1, MONTH1 :
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice;
                break;
            case MONTH6 :
                salaryYear = yieldPrice;
                salaryMonth = yieldPrice.divide(new BigDecimal(6), MathContext.DECIMAL32);
                break;
            case YEAR1 :
                salaryYear = yieldPrice;
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                break;
            case YEAR5 :
                salaryYear = yieldPrice.divide(new BigDecimal(5), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                break;
            case YEAR10 :
                salaryYear = yieldPrice.divide(new BigDecimal(10), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
                break;
            default : throw new IllegalArgumentException("Unexpected value: " + investDate);
        }
        
        // 계산이력 저장
        calcHistRepository.save(
    		CalcHist.builder()
    			.code(code)
    			.company(stockPrice.getCompany())
    			.createdUid(user.getUid())
    			.investDate(oldCloseDate)
    			.investDateName(investDate.getName())
    			.investPrice(investPrice)
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
                    .investPrice(investPrice)
                    .investDate(investDate.getName())
                    .oldPrice(oldStockPrice)
                    .yieldPrice(yieldPrice)
                    .yieldPercent(yieldPercent)
                    .oldCloseDate(oldCloseDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .holdingStock(holdingStock)
                    .salaryYear(salaryYear)
                    .salaryMonth(salaryMonth)
                    .build()
            ).build();
        
    }

    /**
     * kospi, 동종업종, 현재가 결과 조회
     * @return
     * @throws Exception
     */

    public CurrentKospiIndustryRes getCurrentKospiIndustry(BuyThenForm buyThenForm) throws Exception {
        CurrentKospiIndustryRes result;

        // 공통
        String code = buyThenForm.getCode(); // 검색한 종목 코드
        Stock stock = stockRepository.findByCode(code)
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));

        InvestDate investDate = buyThenForm.getInvestDate(); // 투자 날

        // 믿고싶지 않은 현재가
        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);  // 실시간 주식 정보
        BigDecimal pricePerStock = realTimeStock.getCurrentPrice();   // 실시간 주가
        BigDecimal stocksPerPrice = buyThenForm.getInvestPrice()      // 보유 주식 환산
                .divide(pricePerStock, MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_EVEN);

        // 코스피
        String kosCode = "KS11"; // 코스피 종목 코드
        Stock kosStock = stockRepository.findByCode(kosCode)
                .orElseThrow(() -> new Exception(
                        "코스피 종목코드(" + kosCode + ")가 올바르지 않습니다.")
                );
        StocksPrice kosStockPrice = stocksPriceRepository.findByStocksId(kosStock.getId())
                .orElseThrow(() -> new Exception(
                        "코스피 종목코드(" + kosCode + ")가 올바르지 않습니다.")
                );

        BigDecimal kosOldPrice; // 코스피 과거 지수
        String oldDate;         // 검색한 과거 날짜
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.");
        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        NumberPath<BigDecimal> datePriceField; // 동일 업종 날짜에 따른 주가 필드
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
        BigDecimal kosCurrentPrice = kosRealTimeStock.getCurrentPrice();    // 코스피 현재 지수
        BigDecimal kosYieldPercent = kosCurrentPrice.subtract(kosOldPrice). // 코스피 상승률
                divide(kosOldPrice, MathContext.DECIMAL32).
                multiply(new BigDecimal(100));

        // 동일 업종
        StocksPrice stocksPrice = stocksPriceRepository.findByStocksId(stock.getId())
                .orElseThrow(() -> new Exception("종목 코드가 올바르지 않습니다."));
        String sector = stocksPrice.getSectorYahoo();   // 영어 업종명

        List<Tuple> industryList = queryFactory
                .select(qStocksPrice.code,
                        qStocksPrice.company,
                        datePriceField)
                .from(qStocksPrice)
                .where(qStocksPrice.sectorYahoo.eq(sector))
                .orderBy(qStocksPrice.marketCap.desc())
                .fetch();

        List<String> industryCodes = new ArrayList<String>();   // 동일 업종 id 리스트
        List<Company> mainCompanies = new ArrayList<Company>();                            // 대표 종목 4가지
        int cnt = 0;
        BigDecimal oldSumPrice = new BigDecimal(0);         // 과거 동일업종 주가 합
        for (Tuple tuple : industryList) {
            cnt++;
            // 동일업종 종목코드 리스트
            industryCodes.add(tuple.get(qStocksPrice.code) + ".KS");

            // 대표 종목 4가지
            if (cnt <= 4) {
                Company company = Company.builder()
                        .code(tuple.get(qStocksPrice.code))
                        .company(tuple.get(qStocksPrice.company))
                        .build();
                mainCompanies.add(company);
            }

            // 과거 동일업종 주가 합
            BigDecimal oldPrice = tuple.get(datePriceField);
            if (oldPrice != null) {
            oldSumPrice = oldSumPrice.add(tuple.get(datePriceField));
            }
        }

        // 동일업종 상승률 계산
        int industryNum = industryList.size();
        String[] industryCodesArray = industryCodes.toArray(new String[industryNum]);
        BigDecimal newSumPrice = stockUtils.getCurrentSumPrice(industryCodesArray);
        BigDecimal industryYieldPercent = newSumPrice.subtract(oldSumPrice) // (현재종가합-이전종가합)/이전종가합 * 100
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
     * 모든 사용자의 계산이력 목록 조회
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
                    .createdDate(vo.getCreatedDate().format(DateTimeFormatter.ofPattern(FORMAT)))
                    .investDate(vo.getInvestDate().format(DateTimeFormatter.ofPattern(FORMAT)))
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
                        .pageNo(page.getNumber())
                        .pageSize(page.getSize())
                        .build()
                )
                .build();
    }
    
    /**
     * 기간별 수익률로 정렬시킨 목록 조회(투자기간, 정렬방향)
     * @param pageParam
     * @return
     */
    public YieldSortRes getYieldSortList(InvestDate investDate, BuySell buySell, int pageSize, int pageNo) {
        if(InvestDate.DAY1 == investDate) { 
            // 1일 전 수익률은 계속 바뀌는 현재가랑 재계산을 종목 갯수만큼 해야해서 보류...? 
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
        .where(oldDate.isNotNull())
        .orderBy(order)
        .offset((pageNo - 1) * pageSize)
        .limit(pageSize)
        .fetch();
        
        long count = queryFactory.selectFrom(qStocksPrice)
            .where(oldDate.isNotNull())
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
     * 코스피 데이터 출력 - historycal 데이터 캐싱처리 필요(하루주기) 
     * 현재가, 전일종가와 현재가를 비교한 수익률, historical price(10년치, 한달주기 데이터)
     * @throws IOException
     */
    public StockHist getKospiChart() throws IOException {
        return stockUtils.getStockHist("KS11");
    }
    
    /**
     * 검색어와 페이지번호 페이지 크기를 전달받아서 뉴스를 반환
     * max - pageSize 10 이면 pageNo 100까지 가능, pageSize 100 이면 pageNo 10까지 가능, 
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
                .queryParam("display", pageSize) // 10(기본값), 100(최대)
                .queryParam("start", ((pageNo - 1) * pageSize) + 1)    // offset 임 - 1(기본값), 1000(최대)
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
     * 삼성(005930),sk하이닉스(000660),카카오(035720),현대자동차(005380) 4가지 종목의 10년 내 최고가 일자, 가격, 현재가 
     * @throws IOException 
     */
    public List<HighPriceRes> getHighPrice() {
        
        List<StocksPrice> stockList = stocksPriceRepository
                .findByCodeInOrderByMarketCapDesc("005930", "000660", "035720", "005380");
        
        return stockList.stream()
            .map(vo -> {
                StockHist stockHist = stockUtils.getStockHist(vo.getCode());
                return HighPriceRes.builder()
                    .code(vo.getCode())
                    .company(vo.getCompany())
                    .sector(vo.getSectorYahoo())
                    .sectorKor(vo.getSectorKor())
                    .maxQuote(stockHist.getMaxQuote())
                    .build();
            }).collect(Collectors.toList());
    }
}