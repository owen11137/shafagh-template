package com.misc.shafagh.base.api.dto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class FundDto {
    private Long id;
    private String code;
    private String name;
    private Long ownerId;
    private LocalDateTime toDate;
}
