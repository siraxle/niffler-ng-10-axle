package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendAndCategoryRepository;
import guru.qa.niffler.data.repository.impl.SpendAndCategoryRepositoryJdbc;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    private final SpendAndCategoryRepository spendAndCategoryRepository = new SpendAndCategoryRepositoryJdbc();

    private final TransactionTemplate transactionTemplate = new TransactionTemplate(
            new JdbcTransactionManager(
                    DataSources.dataSource(CFG.spendJdbcUrl())
            )
    );

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(
            CFG.spendJdbcUrl()
    );

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.spendJdbcUrl()
    );


    @Override
    public SpendJson createSpend(SpendJson spend) {
        return xaTxTemplate.execute(() -> {
            SpendEntity spendEntity = SpendEntity.fromJson(spend);
            if (spendEntity.getCategory().getId() == null) {
                CategoryEntity categoryEntity = spendAndCategoryRepository.createCategory(spendEntity.getCategory());
                spendEntity.setCategory(categoryEntity);
            }
            return SpendJson.fromEntity(
                    spendAndCategoryRepository.createSpend(spendEntity)
            );
        });
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return xaTxTemplate.execute(() -> {
            CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
            CategoryEntity createdCategory = spendAndCategoryRepository.createCategory(categoryEntity);
            return CategoryJson.fromEntity(createdCategory);
        });
    }

    @Override
    public CategoryJson updateCategory(CategoryJson category) {
        return xaTxTemplate.execute(() -> {
            CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
            CategoryEntity updatedCategory = spendAndCategoryRepository.updateCategory(categoryEntity);
            return CategoryJson.fromEntity(updatedCategory);
        });
    }


    @Override
    public Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        return xaTxTemplate.execute(() -> {
            Optional<CategoryEntity> category = spendAndCategoryRepository.findCategoryByUsernameAndName(username, categoryName);
            return category.map(CategoryJson::fromEntity);
        });
    }
}
