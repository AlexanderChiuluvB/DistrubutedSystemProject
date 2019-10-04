package pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class StockOrder {

    private Integer Id;

    private Integer Sid;

    private String Name;

    private Date CreateTime;
}
