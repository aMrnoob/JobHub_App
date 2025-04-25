package com.example.befindingjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class BookmarkRequest {
    private int userId;
    private int jobId;
}
