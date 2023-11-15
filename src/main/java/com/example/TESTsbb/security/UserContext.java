package com.example.TESTsbb.security;

import com.example.TESTsbb.user.SiteUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserContext extends org.springframework.security.core.userdetails.User implements OAuth2User {

    private final Long id;
    private final String username;

    private Map<String, Object> attributes;
    private String userNameAttributeName;



    public UserContext(SiteUser siteUser, List<GrantedAuthority> authorities) {
        super(siteUser.getUsername(), siteUser.getPassword(), authorities);
        this.id = siteUser.getId();
        this.username = siteUser.getUsername();
    }

    public UserContext(SiteUser siteUser, List<GrantedAuthority> authorities, Map<String, Object> attributes, String userNameAttributeName) {
        this(siteUser, authorities);
        this.attributes = attributes;
        this.userNameAttributeName = userNameAttributeName;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return super.getAuthorities().stream().collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        return this.getAttribute(this.userNameAttributeName).toString();
    }
}
