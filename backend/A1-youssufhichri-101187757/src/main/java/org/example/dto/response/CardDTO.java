package org.example.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CardDTO {
    private String id;
    private String type;
    private int value;
}
