package guru.qa.niffler.service.impl.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendAndCategoryRepository;
import guru.qa.niffler.data.repository.impl.SpendAndCategoryRepositoryHibernate;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendClient;
import lombok.NonNull;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    private final SpendAndCategoryRepository spendAndCategoryRepository = new SpendAndCategoryRepositoryHibernate();

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
    @Nullable
    public SpendJson createSpend(@NonNull SpendJson spend) {
        return xaTxTemplate.execute(() -> {
            SpendEntity spendEntity = SpendEntity.fromJson(spend);
            if (spendEntity.getCategory().getId() == null) {
                spendAndCategoryRepository.createCategory(spendEntity.getCategory());
            } else {
                CategoryEntity managedCategory = spendAndCategoryRepository
                        .findCategoryById(spendEntity.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
                spendEntity.setCategory(managedCategory);
            }
            return SpendJson.fromEntity(
                    spendAndCategoryRepository.createSpend(spendEntity)
            );
        });
    }

    @Nullable
    public CategoryJson createCategory(@NonNull CategoryJson category) {
        return xaTxTemplate.execute(() -> {
            CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
            CategoryEntity createdCategory = spendAndCategoryRepository.createCategory(categoryEntity);
            return CategoryJson.fromEntity(createdCategory);
        });
    }

    @Nullable
    public CategoryJson updateCategory(@NonNull CategoryJson category) {
        return xaTxTemplate.execute(() -> {
            CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
            CategoryEntity updatedCategory = spendAndCategoryRepository.updateCategory(categoryEntity);
            return CategoryJson.fromEntity(updatedCategory);
        });
    }

    @Override
    @NonNull
    public Optional<CategoryJson> findCategoryByNameAndUsername(@NonNull String categoryName, @NonNull String username) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            Optional<CategoryEntity> category = spendAndCategoryRepository.findCategoryByUsernameAndName(username, categoryName);
            return category.map(CategoryJson::fromEntity);
        }));
    }
}