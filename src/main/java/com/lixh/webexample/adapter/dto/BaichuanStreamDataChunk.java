package com.lixh.webexample.adapter.dto;

import lombok.Data;

import java.util.List;

@Data
public class BaichuanStreamDataChunk {

    private String id; // "Chat Complete"的唯一标识符

    private int created; // 创建聊天完成时的 Unix 时间戳（秒）

    private List<Choice> choices; // 聊天完成选项的列表

    private String model; // 用于完成聊天的模型

    private String object; // 始终为"chat.completion.chunk"

    private KnowledgeBase knowledgeBase; // 知识库相关响应

    @Data
    public static class Choice {

        private int index; // choices 中对应的索引

        private Delta delta; // 增量返回，由流式模型生成的聊天完成消息

        private String finishReason; // 模型停止生成 token 的原因

        @Data
        public static class Delta {

            private String role; // 消息作者的角色

            private String content; // 消息内容

            private List<ToolCall> toolCalls; // 工具调用列表

            @Data
            public static class ToolCall {

                private String id; // 命中函数的唯一标识符

                private String type; // 模型使用工具的类型，目前仅有function

                private Function function;

                @Data
                public static class Function {

                    private String name; // 模型命中函数的名称

                    private String arguments; // 模型生成 JSON 格式的函数调用具体参数
                }
            }
        }
    }

    @Data
    public static class KnowledgeBase {

        private List<Cite> cites; // 引用知识

        @Data
        public static class Cite {

            private String title; // 文件名称

            private String fileId; // 文件 id

            private String content; // 分片内容

            private CiteAnnotation citeAnnotation;

            @Data
            public static class CiteAnnotation {

                private String value; // 大模型输出完整带角标内容

                private List<Annotation> annotations; // 引用批注

                @Data
                public static class Annotation {

                    private String type; // 批注类型

                    private String text; // 在回答内容中需要替换的字符串

                    private int startIndex; // 需要替换字符串的起始位置

                    private int endIndex; // 需要替换字符串的截止位置

                    private FileCitation fileCitation;

                    @Data
                    public static class FileCitation {

                        private String fileId; // 文件 id

                        private String quote; // 引用文档的具体内容
                    }
                }
            }
        }
    }
}