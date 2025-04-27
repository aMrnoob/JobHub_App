package com.example.befindingjob.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkRequest {
    private int userId;
    private int jobId;
}
