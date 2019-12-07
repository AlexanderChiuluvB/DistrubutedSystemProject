package DistributedSystem.miaosha.service.impl;

import DistributedSystem.miaosha.service.api.StockService;
import DistributedSystem.miaosha.dao.StockMapper;
import DistributedSystem.miaosha.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service(value = "StockService")
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public int getStockCount(int id) {
        Stock stock = stockMapper.selectByPrimaryKey(id);
        return stock.getCount();
    }

    @Override
    public Stock getStockById(int id) {
        return stockMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateStockById(Stock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockInMysql(Stock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int initDBBefore(int id, int count) {
        return stockMapper.initDBBefore(id, count);
    }

    @Override
    public int createStock(int id, int count, String name) {
        return stockMapper.createStock(id, count, name);
    }

}
