package com.stocking.infra.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUser {
    
    @ApiModelProperty(notes = "uid", position = 1)
    private String uid;

    @ApiModelProperty(notes = "표시 이름", position = 2)
    private String name;

    @ApiModelProperty(notes = "이메일", position = 3)
    private String email;
}
