package pojo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class Stock {

    private Integer Id;

    private String Name;

    private Integer Count;

    private Integer Sale;

    private Integer Version;

}
