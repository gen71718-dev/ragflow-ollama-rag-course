package com.example.ragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String platform = "dify";
    private Dify dify = new Dify();
    private Ragflow ragflow = new Ragflow();

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Dify getDify() {
        return dify;
    }

    public void setDify(Dify dify) {
        this.dify = dify;
    }

    public Ragflow getRagflow() {
        return ragflow;
    }

    public void setRagflow(Ragflow ragflow) {
        this.ragflow = ragflow;
    }

    public static class Dify {
        private String url = "http://<SERVER_IP>/v1/chat-messages";
        private String apiKey = "<APP_API_KEY>";
        private String user = "business-demo";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }

    public static class Ragflow {
        private String url = "http://<SERVER_IP>/v1/chat/completions";
        private String apiKey = "<APP_API_KEY>";
        private String model = "<RF_MODEL>";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}