package pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class StockOrder {

    private Integer id;

    private Integer sid;

    private String name;

    private Date createTime;
}
