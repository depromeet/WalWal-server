package com.depromeet.stonebed.fixtureMonkeyTest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Order {
    @NotNull private Long id;

    @NotBlank private String orderNo;

    private OrderType orderType;

    @Size(min = 2, max = 10)
    private String productName;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    private long totalPrice;

    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent private Instant orderedAt;

    public enum OrderType {
        SMARTSTORE,
        BLOG,
        CAFE,
    }
}
