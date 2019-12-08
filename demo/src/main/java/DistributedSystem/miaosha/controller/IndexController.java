package DistributedSystem.miaosha.controller;

import DistributedSystem.miaosha.redis.StockWithRedis;
import DistributedSystem.miaosha.service.api.OrderService;
import DistributedSystem.miaosha.service.api.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = "/")
@Api(value = "接口", tags = "程序启动接口")
public class IndexController {

    private static final String success = "SUCCESS";
    private static final String error = "ERROR";

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockService stockService;

    @ApiOperation(value = "初始化数据库", notes = "初始化数据库")
    @RequestMapping(value = "initDBAndRedis", method = RequestMethod.POST)
    @ResponseBody
    public String initDBAndRedisBefore(HttpServletRequest request, Integer sid, Integer count) {

        int res = 0;
        try {
            // 初始化Mysql库存信息
            res = stockService.initDBBefore(sid, count);
            if (res != 1) {
                return "Mysql更新失败";
            }

            // 清空订单表
            res = (orderService.delOrderDB() == 0 ? 1 : 0);
            if (res != 1) {
                return "清空订单表失败";
            }

            // 初始化Redis库存信息
            res = StockWithRedis.initRedisBefore(sid, count);
            if (res != 1) {
                return "初始化Redis失败";
            }

            // 初始化服务器本地拥有的库存和buffer库存的数量
            StockWithRedis.initServerBefore(sid,count);

        } catch (Exception e) {
            System.out.printf("Exception: %s ", e);
        }
        if (res == 1) {
            System.out.println("重置数据库和缓存成功");
        }
        return res == 1 ? success : error;
    }

    @ApiOperation(value = "秒杀", notes = "秒杀")
    @RequestMapping(value = "createOrderWithLimitAndRedisAndKafka", method = RequestMethod.POST)
    @ResponseBody
    public String createOrderWithLimitsAndRedisAndKafka(HttpServletRequest request, Integer sid) {

        try {
            if (!orderService.acquireTokenFromRedisBucket(sid))
                return "令牌获取失败";
            orderService.checkRedisAndSendToKafka(sid);
        } catch (Exception e) {
            System.out.printf("Exception: %s ", e);
            e.printStackTrace();
        }
        //System.out.println("秒杀商品下单成功！");
        return "秒杀下单成功";
    }

    @ApiOperation(value = "查询库存", notes = "查询库存")
    @RequestMapping(value = "checkStock", method = RequestMethod.POST)
    @ResponseBody
    public int checkStock(HttpServletRequest request, Integer sid) {

        try {
            return stockService.getStockCount(sid);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @ApiOperation(value = "入库", notes = "入库")
    @RequestMapping(value = "insertStock", method = RequestMethod.POST)
    @ResponseBody
    public int insertStock(HttpServletRequest request, Integer id, Integer count, String name) {

        try {
            return stockService.createStock(id, count, name);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


}