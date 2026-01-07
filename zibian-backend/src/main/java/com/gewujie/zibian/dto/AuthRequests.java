package com.gewujie.zibian.dto;

public class AuthRequests {
    public static class WeChatLoginRequest {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class AlipayLoginRequest {
        private String authCode;

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }
    }
}
