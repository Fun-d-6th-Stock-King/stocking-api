package com.stocking.modules.firebase;

import java.util.List;

import org.springframework.stereotype.Service;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FireUserService {

    private final FireUserRepository fireUserRepository;
    
    /**
     * 저장 (uid 기준으로 조회해서 있으면 업데이트 없으면 저장)
     * @param fireUser
     * @return
     */
    public FireUser save(FireUserReq fireUserReq) {
        FireUser user = fireUserRepository
            .findByUid(fireUserReq.getUid())
            .orElse(
                FireUser.builder()
                    .uid(fireUserReq.getUid())
                    .displayName(fireUserReq.getDisplayName())
                    .photoURL(fireUserReq.getPhotoURL())
                    .email(fireUserReq.getEmail())
                    .phoneNumber(fireUserReq.getPhoneNumber())
                    .providerId(fireUserReq.getProviderId())
                    .build()
            );
        
        return fireUserRepository.save(user);
    }
    
    /**
     * 목록 조회
     * @return
     */
    public List<FireUser> getList() {
        return fireUserRepository.findAll();
    }
    
    /**
     * 사용자 조회
     * @param uid
     * @return
     * @throws NotFoundException 
     */
    public FireUserRes getFireUser(String uid) throws NotFoundException {
        FireUser fireUser =  fireUserRepository.findByUid(uid)
            .orElseThrow(() -> new NotFoundException("사용자 정보가 없습니다."));
        
        return FireUserRes.builder()
            .id(fireUser.getId())
            .uid(fireUser.getUid())
            .email(fireUser.getEmail())
            .displayName(fireUser.getDisplayName())
            .photoURL(fireUser.getPhotoURL())
            .phoneNumber(fireUser.getPhoneNumber())
            .providerId(fireUser.getProviderId())
            .build();
    }
    
}
