package com.classroom.googel.enums;

import java.util.HashMap;
import java.util.Map;

public enum OAuthProvider {

    GOOGLE("GOOGLE"),
    CANVAS("CANVAS"),
    SCHOOLOGY("SCHOOLOGY"),
    SYSTEM("SYSTEM");

    private static final Map<String, OAuthProvider> BY_LABEL = new HashMap<>();

    static {
        for(OAuthProvider oAuthProvider: values()){
            BY_LABEL.put(oAuthProvider.label, oAuthProvider);
        }
    }

    private final String label;

    OAuthProvider(String label){
        this.label = label;
    }

    public static OAuthProvider valueOfLabel(String label){
        return BY_LABEL.get(label);
    }

    public String getLabel(){
        return label;
    }
}
