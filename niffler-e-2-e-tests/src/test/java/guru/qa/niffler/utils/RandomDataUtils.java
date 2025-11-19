package guru.qa.niffler.utils;

import com.github.javafaker.Faker;

import java.util.UUID;

public class RandomDataUtils {
    private static final Faker faker = new Faker();

    public static String randomUsername() {
        return faker.name().username();
    }

    public static String randomeName() {
        return faker.name().fullName();
    }

    public static String randomeSurname() {
        return faker.name().lastName();
    }

    public static String randomeCategoryName() {
        return faker.rockBand().name();
    }

    public static String randomeSentence(int wordsCount) {
        return faker.lorem().sentence(wordsCount);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

}
