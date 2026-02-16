package com.taxyaar.sign.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;


@Component
public class TaxConfig {

    @Value("${eri.user.id}")
    private String eriUserId;

    @Value("${eri.client.id}")
    private String eriClientId;

    @Value("${eri.client.secret}")
    private String eriClientSecret;

    @Value("${eri.service.name}")
    private String eriServiceName;

    @Value("${eri.plain.text}")
    private String eriPlainText;

    @Value("${eri.password.key}")
    private String eriPasswordKey;

    @Value("${eri.access.mode}")
    private String eriAccessMode;

    @Value("${eri.user.agent}")
    private String eriUserAgent;

    @Value("${eri.pfx.path}")
    private String eriPfxPath;

    @Value("${eri.pfx.type}")
    private String eriPfxType;

    @Value("${eri.pfx.alias}")
    private String eriPfxAlias;

    @Value("${eri.pfx.password}")
    private String eriPfxPassword;

    public String getEriUserId(){
        return eriUserId;
    }

    public String getEriClientId(){
        return eriClientId;
    }

    public String getEriClientSecret(){
        return eriClientSecret;
    }

    public String getEriServiceName(){
        return eriServiceName;
    }

    public String getEriPlainText(){
        return eriPlainText;
    }

    public String getEriPasswordKey(){
        return eriPasswordKey;
    }

    public String getEriAccessMode(){
        return eriAccessMode;
    }

    public String getEriUserAgent(){
        return eriUserAgent;
    }

    public String getEriPfxPath(){
        return eriPfxPath;
    }

    public String getEriPfxType(){
        return eriPfxType;
    }

    public String getEriPfxAlias(){
        return  eriPfxAlias;
    }

    public String getEriPfxPassword(){
        return  eriPfxPassword;
    }
}
