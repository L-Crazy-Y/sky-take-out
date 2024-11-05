package com.sky.service.impl;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.LocalDateTime2TurpleDTO;
import com.sky.dto.OrderAmount;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkSpaceService workSpaceService;
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

    /**
     * 统计指定时间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin,end);
        //拿到处理后的日期列表数据
        List<LocalDateTime2TurpleDTO>localDateTimes = localDate2LocalDateTime(dateList);

        List<Integer> orderCountList = orderMapper.countOrderNumByDay(localDateTimes);
        List<Integer> validOrderCountList = orderMapper.sumValidNumByDay(localDateTimes);

       /* for (LocalDateTime2TurpleDTO localDateTime : localDateTimes) {
            //查询每天的订单总数 select count(id) from orders where order_time > ? and order_time < ?
            Integer orderCount = getOrderCount(localDateTime.getBegin(),localDateTime.getEnd(),null);

            //查询每天的有效订单数select count(id) from orders where order_time > ? and order_time < ?
            //and status = 5
            Integer validOrderCount = getOrderCount(localDateTime.getBegin(),localDateTime.getEnd(), Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }*/

        //计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单总数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();


        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            //计算订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;

        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销量排名前10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime,endTime);
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();

        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

   /* private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }*/

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

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workSpaceService.getBusibess(LocalDateTime.of(dateBegin,LocalTime.MIN),LocalDateTime.of(dateEnd,LocalTime.MAX));

        //2.通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");


        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间: " + dateBegin + "至" + dateEnd);

            //获取第四行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workSpaceService.getBusibess(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //3.通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭资源
            outputStream.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
