package com.stocking.modules.buyornot.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; 
    
    private long id;
    private String comment;         // 코멘트
    private String uid;             // 작성자 uid
    private LocalDateTime createdDate;       // 작성일시
    private String displayName; // 등록자 이름
    
    public String getCreatedDate() {
        
        return this.createdDate.format(DateTimeFormatter.ofPattern(FORMAT));
    }
}
