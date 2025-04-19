package com.wjh.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 18:06:51
 * @Description
 */
@Data
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String productName;
    private Integer productId;
    private Integer productNum;
    private BigDecimal totalAccount;
    private List<Product> products;

}
