package com.stocking.modules.firebase;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FireUserRes {
    
    @ApiModelProperty(notes = "id", position = 2)
    private long id;

    @ApiModelProperty(notes = "uid", position = 2)
    private String uid;

    @ApiModelProperty(notes = "표시 이름", position = 3)
    private String displayName;
    
    @ApiModelProperty(notes = "사진 URL", position = 4)
    private String photoURL;

    @ApiModelProperty(notes = "이메일", position = 5)
    private String email;
    
    @ApiModelProperty(notes = "전화번호", position = 6)
    private String phoneNumber;
    
    @ApiModelProperty(notes = "공급자 ID", position = 7)
    private String providerId;
}
