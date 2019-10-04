package controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.StockWithRedis;
import service.dao.OrderService;
import service.dao.StockService;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = "/")
@Api(value="接口", tags = "程序启动接口")
public class controller {

    private static final String success = "SUCCESS";
    private static final String error = "ERROR";

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockService stockService;

    @ApiOperation(value = "初始化数据库", notes = "初始化数据库")
    @RequestMapping(value = "initDBAndRedis", method = RequestMethod.POST)
    @ResponseBody
    public String initDBAndRedisBefore(HttpServletRequest request) {

        int res = 0;
        try {
            // 初始化库存信息
            res = stockService.initDBBefore();
            // 清空订单表
            res &= (orderService.delOrderDB() == 0 ? 1 : 0);
            StockWithRedis.initRedisBefore();
        } catch (Exception e) {
            log.error("Exception: ", e);
        }
        if (res == 1) {
            log.info("重置数据库和缓存成功");
        }
        return res == 1 ? success : error;
    }

    @ApiOperation(value = "秒杀", notes = "秒杀")
    @RequestMapping(value = "createOrderWithLimitAndRedisAndKafka", method = RequestMethod.POST)
    @ResponseBody
    public String createOrderWithLimitsAndRedisAndKafka(HttpServletRequest request ,int sid) {

        try{
            orderService.checkRedisAndSendToKafka(sid);
        } catch (Exception e) {
            log.error("Exception:" + e);
            e.printStackTrace();
        }
        return "秒杀请求正在处理,排队中";
    }
}