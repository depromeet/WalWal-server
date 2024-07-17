package com.depromeet.stonebed.fixtureMonkeyTest;

import static org.assertj.core.api.BDDAssertions.*;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestFixtureMonkey {

    @Test
    void checkPerson() {
        // given
        FixtureMonkey sut =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();

        // when
        User person =
                sut.giveMeBuilder(User.class)
                        .set("personId", Arbitraries.strings().ofMinLength(1).ofMaxLength(16))
                        .set("personName", Arbitraries.strings().ofMinLength(1).ofMaxLength(16))
                        .set("age", Arbitraries.integers().between(1, 100))
                        .set("personNo", Arbitraries.strings().ofMinLength(1).ofMaxLength(16))
                        .set(
                                "addressList",
                                Arbitraries.strings()
                                        .ofMaxLength(16)
                                        .list()
                                        .ofMinSize(1)
                                        .ofMaxSize(2))
                        .sample();

        // then
        then(person.getPersonId()).isNotNull();
        then(person.getPersonName()).isNotBlank();
        then(person.getPersonNo().length()).isBetween(1, 16);
    }

    @Test
    void testOrder() {
        // given
        FixtureMonkey sut =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();

        // when
        Order actual =
                sut.giveMeBuilder(Order.class)
                        .set("orderNo", Arbitraries.strings().ofMinLength(1).ofMaxLength(16))
                        .set("productName", Arbitraries.strings().ofMinLength(2).ofMaxLength(10))
                        .set("price", Arbitraries.longs().between(0, 1000))
                        .set("quantity", Arbitraries.integers().between(1, 100))
                        .set(
                                "items",
                                Arbitraries.strings()
                                        .ofMaxLength(10)
                                        .list()
                                        .ofMinSize(1)
                                        .ofMaxSize(2))
                        .sample();

        // then
        then(actual.getId()).isNotNull(); // @NotNull
        then(actual.getOrderNo()).isNotBlank(); // @NotBlank
        then(actual.getProductName().length()).isBetween(2, 10); // @Size(min = 2, max = 10)
        then(actual.getQuantity()).isBetween(1, 100); // Min(1) @Max(100)
        then(actual.getPrice()).isGreaterThanOrEqualTo(0); // @Min(0)
        then(actual.getItems()).hasSizeLessThan(3); // @Size(max = 3)
        then(actual.getItems()).allMatch(it -> it.length() <= 10); // @NotBlank @Size(max = 10)
    }
}
