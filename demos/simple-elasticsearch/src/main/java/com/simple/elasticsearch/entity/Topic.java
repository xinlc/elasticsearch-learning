package com.simple.elasticsearch.entity;

import com.simple.elasticsearch.annotation.EsId;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "topic")
public class Topic {

  @EsId
  private Long id;
  private Long subject;
  private Long type;
  private String img;
  private String content;
  private String contentStr;
  private Long score;
  private String audio;
  private String video;
  private Boolean disabled;
  private Boolean deleted;
  private String createdAt;
  private String updatedAt;
  private String answer;
  private String three;
  private String checkvalue;


}
