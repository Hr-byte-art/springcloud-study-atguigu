package com.wjh.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 18:07:02
 * @Description
 */
@Data
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer count;
    private Integer version;
}
