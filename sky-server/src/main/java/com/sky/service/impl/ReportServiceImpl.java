package com.sky.service.impl;

import com.sky.dto.LocalDateTime2TurpleDTO;
import com.sky.dto.OrderAmount;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    /**
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)){
            //日期计算，计算begin到end范围内每天的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //视频中的方法，网络io资源消耗大,以下是优化
        List<LocalDateTime2TurpleDTO> localDateTimes = localDate2LocalDateTime(dateList);

        List<OrderAmount> orderAmountList = orderMapper.countSumByDay(localDateTimes);
        List<Double> turnoverList = new ArrayList<>();
        for (OrderAmount orderAmount : orderAmountList) {
            if (orderAmount == null){
                turnoverList.add(0.0);
            }else {
                turnoverList.add(orderAmount.getSum());
            }
        }

        /*//存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额指的是：状态为已完成的订单金融合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);

            //select sum(amount) from orders where order_time > beginTime and
            //order_time < endTime and status = 5
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover =  orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }*/

        //将集合中的元素取出来用逗号分割，最终拼接成一个字符串。
    return TurnoverReportVO.builder()
            .dateList( StringUtils.join(dateList,","))
            .turnoverList(StringUtils.join(turnoverList,","))
            .build();
    }



    /**
     * 将LocalDate转化为LocalDateTime方便数据库查询
     *
     * @param dateList
     * @return
     */
    public List<LocalDateTime2TurpleDTO> localDate2LocalDateTime(List<LocalDate> dateList) {
        List<LocalDateTime2TurpleDTO> localDateTimes = new ArrayList<>();
        for (LocalDate date : dateList) {
            localDateTimes.add(LocalDateTime2TurpleDTO.builder()
                    .begin(LocalDateTime.of(date, LocalTime.MIN))
                    .end(LocalDateTime.of(date, LocalTime.MAX))
                    .build());
        }
        return localDateTimes;
    }

}
