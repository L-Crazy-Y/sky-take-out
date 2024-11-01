package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalDateTime2TurpleDTO {
    private LocalDateTime begin;
    private LocalDateTime end;
}
