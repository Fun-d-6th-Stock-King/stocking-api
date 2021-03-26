package com.stocking.modules.firebase;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "firebase")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FireUser implements Serializable {

    private static final long serialVersionUID = -2798011663551225225L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "id", position = 1)
    private long id;

    @Column(name = "uid")
    @ApiModelProperty(notes = "uid", position = 2)
    private String uid;

    @Column(name = "display_name")
    @ApiModelProperty(notes = "표시 이름", position = 3)
    private String displayName;
    
    @Column(name = "photo_url")
    @ApiModelProperty(notes = "사진 URL", position = 4)
    private String photoURL;

    @Column(name = "email")
    @ApiModelProperty(notes = "이메일", position = 5)
    private String email;
    
    @Column(name = "phone_number")
    @ApiModelProperty(notes = "전화번호", position = 6)
    private String phoneNumber;
    
    @Column(name = "provider_id")
    @ApiModelProperty(notes = "공급자 ID", position = 7)
    private String providerId;
}
