package guru.qa.niffler.data.entity.spend;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
public class SpendEntity implements Serializable {
    private UUID id;

    private String username;

    private CurrencyValues currency;

    private Date spendDate;

    private Double amount;

    private String description;

    private CategoryEntity category;

    public static SpendEntity fromJson(SpendJson spendJson) {
        SpendEntity entity = new SpendEntity();
        entity.setId(spendJson.id());
        entity.setUsername(spendJson.username());
        entity.setCategory(CategoryEntity.fromJson(spendJson.category()));
        entity.setDescription(spendJson.description());
        entity.setAmount(spendJson.amount());
        entity.setSpendDate(new java.sql.Date(spendJson.spendDate().getTime()));
        return entity;
    }

}
