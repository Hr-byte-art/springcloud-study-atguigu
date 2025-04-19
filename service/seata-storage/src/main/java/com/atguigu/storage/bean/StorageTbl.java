package com.atguigu.storage.bean;

import java.io.Serializable;
import lombok.Data;

/**
 * @author 王哈哈
 * @TableName storage_tbl
 */
@Data
public class StorageTbl implements Serializable {
    private Integer id;

    private String commodityCode;

    private Integer count;

    private static final long serialVersionUID = 1L;
}