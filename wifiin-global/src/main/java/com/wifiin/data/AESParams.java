package com.wifiin.data;

public class AESParams {
    private String key;
    private String salt;
    private String iv;

    public AESParams(){}
    public AESParams(String key, String salt, String iv) {
        this.key = key;
        this.salt = salt;
        this.iv = iv;
    }

    public String getKey(){
        return key;
    }
    public void setKey(String key){
        this.key = key;
    }
    public String getSalt(){
        return salt;
    }
    public void setSalt(String salt){
        this.salt = salt;
    }
    public String getIv(){
        return iv;
    }
    public void setIv(String iv){
        this.iv = iv;
    }
}
