package DistributedSystem.miaosha.pojo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class Stock {
    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Integer getCount() {
        return Count;
    }

    public void setCount(Integer count) {
        Count = count;
    }

    public Integer getSale() {
        return Sale;
    }

    public void setSale(Integer sale) {
        Sale = sale;
    }

    public Integer getVersion() {
        return Version;
    }

    public void setVersion(Integer version) {
        Version = version;
    }

    private Integer Id;

    private String Name;

    private Integer Count;

    private Integer Sale;

    private Integer Version;

}
