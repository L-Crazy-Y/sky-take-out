package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api("C-端套餐浏览接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类id查询套餐 2024/10/19
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId") //key:setmealCache::100
    public Result<List<Setmeal>> list(Long categoryId){
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        log.info("根据分类id查询套餐");
        //注意：必须起售下的套餐才能显示
        List<Setmeal> list = setmealService.list(setmeal);

        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品 2024/10/19
     * @param id
     * @return
     * 结合页面展示需求，考察多表查询。
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishVO>> dishList(@PathVariable Long id){
        log.info("根据套餐id查询包含的菜品:{}",id);
        List<DishVO>dishVOList = setmealService.getDishById(id);
        return Result.success(dishVOList);
    }

}
