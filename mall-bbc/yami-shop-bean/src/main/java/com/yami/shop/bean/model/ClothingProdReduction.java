package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@TableName("tz_clothing_prod_reduction")
public class ClothingProdReduction {
    @TableId
    @ApiModelProperty("id")
    private Long id;

    private Long shopId;

    private Long prodId;

    private String pic;

    private String prodName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(exist = false)
    private List<Product> productList;

    @TableField(exist = false)
    private Long prodNum;

    @TableField(exist = false)
    private String shopName;
}
