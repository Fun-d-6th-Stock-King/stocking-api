package com.stocking.modules.firebase;

import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FireUserService {

    private final FireUserRepository fireUserRepository;
    
    /**
     * 저장 (uid 기준으로 조회해서 있으면 업데이트 없으면 저장)
     * @param fireUser
     * @return
     */
    public void save(FirebaseToken decodedToken) {
        
        fireUserRepository
            .findByUid(decodedToken.getUid())
            .ifPresentOrElse(vo -> {}, () -> {
                try {
                    UserRecord userRecord = FirebaseAuth.getInstance().getUser(decodedToken.getUid());
                    
                    fireUserRepository.save(
                        FireUser.builder()
                            .uid(userRecord.getUid())
                            .displayName(userRecord.getDisplayName())
                            .photoURL(userRecord.getPhotoUrl())
                            .email(userRecord.getEmail())
                            .phoneNumber(userRecord.getPhoneNumber())
                            .providerId(userRecord.getProviderId())
                            .build()
                    );
                } catch (FirebaseAuthException e) {
                    log.error("UserRecord 가져올때 에러발생 [" + decodedToken.getUid() + "]", e);
                }
                
            });
    }
    
}