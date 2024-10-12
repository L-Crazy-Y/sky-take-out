package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //分类id 2024/10/8 原先int->Long 解决了菜品分类 根据菜品分类id查询菜品信息前端页面不显示的情况
    private Long categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
