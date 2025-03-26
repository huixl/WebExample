package com.lixh.webexample.adapter.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {

    private List<Data> data;

    private String model;

    private String object;

    private Usage usage;

    @lombok.Data
    public static class Data {

        private int index;

        private List<Double> embedding;

        private String object;

    }

    @lombok.Data
    public static class Usage {

        private int prompt_tokens;

        private int total_tokens;

    }


}