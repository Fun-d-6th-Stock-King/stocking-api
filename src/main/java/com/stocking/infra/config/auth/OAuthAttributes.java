package com.stocking.infra.config.auth;

import com.stocking.modules.account.Account;
import com.stocking.modules.account.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private Long id;
    private Long uuid;
    private String email;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, Long id, Long uuid, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.id = id;
        this.uuid = uuid;
        this.email = email;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .id((Long) attributes.get("id"))
                .uuid((Long) attributes.get("uuid"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Account toEntity() {
        return Account.builder()
                .id(id)
                .uuid(uuid)
                .email(email)
                .role(Role.GUEST)
                .build();
    }

}
