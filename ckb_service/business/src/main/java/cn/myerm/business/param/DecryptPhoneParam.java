package cn.myerm.business.param;

import lombok.Data;

@Data
public class DecryptPhoneParam {
    private String code;
    private String encryptedData;
    private String iv;
}
