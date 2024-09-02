package com.yami.shop.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketIdentifyResult {

    private List<wordsResult> words_result;
    @Data
    public static class wordsResult {
        private String words;
    }
}
