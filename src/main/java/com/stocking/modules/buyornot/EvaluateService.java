package com.stocking.modules.buyornot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stocking.modules.stock.Stock;
import com.stocking.modules.stock.StockRepository;

@Service
public class EvaluateService {
    
    @Autowired
    private EvaluateRepository evaluateRepository;
    
    @Autowired
    private EvaluateCommentRepository evaluateCommentRepository;
    
    @Autowired
    private EvaluateLikeRepository evaluateLikeRepository;
    
    @Autowired
    private StockRepository stockRepository;
    
    public Map<String, Object> saveLike(int evaluateId, int accountId) {
        EvaluateLike evaluateLike = new EvaluateLike();
        evaluateLike.setAccountId(accountId);
        evaluateLike.setEvaluateId(evaluateId);
        
        Map<String, Object> resultMap = new HashMap<>();
        
        evaluateLikeRepository.findByEvaluateIdAndAccountId(evaluateId, accountId)
            .ifPresentOrElse(
                vo -> {
                    resultMap.put("id", vo.getId());
                    resultMap.put("like", false);
                    evaluateLikeRepository.delete(vo);
                },
                () -> {
                    evaluateLikeRepository.save(evaluateLike);
                    resultMap.put("id", evaluateLike.getId());
                    resultMap.put("like", true);
                }
            );
        return resultMap;
    }
    
    /**
     * 평가 저장
     * @param evaluateReq
     * @param accountId
     * @return
     * @throws Exception
     */
    public Evaluate saveEvaluate(EvaluateReq evaluateReq, int accountId) throws Exception {
        Evaluate evaluate = new Evaluate();
        
        Stock stock = stockRepository.findByCode(evaluateReq.getCode())
                .orElseThrow(() -> new Exception("종목코드가 없습니다."));
        
        evaluate.setCompany(stock.getCompany());
        evaluate.setCode(stock.getCode());
        evaluate.setPros(evaluateReq.getPros());
        evaluate.setCons(evaluateReq.getCons());
        evaluate.setGiphyImgId(evaluateReq.getGiphyImgId());
        evaluate.setCreatedId(accountId);
        
        return evaluateRepository.save(evaluate);
    }
    
    /**
     * 코멘트 저장
     * @param evaluateId
     * @param comment
     * @param accountId
     * @return
     */
    public EvaluateComment saveComment(int evaluateId, String comment, int accountId) {
        
        EvaluateComment evaluateComment = new EvaluateComment();
        evaluateComment.setEvaluateId(evaluateId);
        evaluateComment.setComment(comment);
        evaluateComment.setCreatedId(accountId);
        evaluateCommentRepository.save(evaluateComment);
        
        return evaluateComment;
    }
}
