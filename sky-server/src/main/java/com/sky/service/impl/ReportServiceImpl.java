package com.sky.service.impl;

import com.sky.dto.LocalDateTime2TurpleDTO;
import com.sky.dto.OrderAmount;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
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
    @Autowired
    private UserMapper userMapper;
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
     * 统计指定时区区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getDateList(begin,end);
        //拿到处理后的日期列表数据
        List<LocalDateTime2TurpleDTO>localDateTimes = localDate2LocalDateTime(dateList);


        //放每天的新增用户数量
        List<Integer>newUserList = userMapper.countUserByDay(localDateTimes);
        //放总的用户量
        List<Integer>totalUserList = userMapper.sumByDay(localDateTimes);


        /*for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endTime);

            //总用户量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin",beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }*/

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    public List<LocalDate>getDateList(LocalDate begin,LocalDate end){
        List<LocalDate>dateList = new ArrayList<>();
        dateList.add(begin);
        //生成begin到end之间的日期
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
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
