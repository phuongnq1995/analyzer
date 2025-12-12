package org.phuongnq.analyzer.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class APIResponse {
  private Boolean success;
  private String message;
}