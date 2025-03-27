package com.lixh.webexample.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.lixh.webexample.config.UserContext;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;


    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;
}