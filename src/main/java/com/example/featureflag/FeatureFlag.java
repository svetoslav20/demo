package com.example.featureflag;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FeatureFlag {

    private final String name;
    private final int category;
    private final String domain;

    private FeatureFlag(String name, int category, String domain) {
        this.name = Objects.requireNonNull(name);
        this.category = Objects.requireNonNull(category);
        this.domain = Objects.requireNonNull(domain);
    }

    public static FeatureFlag of(String name, Integer category, String domain) {
        return new FeatureFlag(name, category, domain);
    }

    public boolean isEnabled(SessionContext sessionContext) {
        return FeatureFlagUtil.isFlagEnabled(sessionContext, category, domain, name);
    }

    public <T> FeatureFlag ifEnabled(T a1, T a2, BiConsumer<T,T> consumer) {
        consumer.accept(a1, a2);
        return this;
    }

    public <T> FeatureFlag ifEnabled(T a, Consumer<T> consumer) {
        consumer.accept(a);
        return this;
    }

    public <T> void orElse(T a, Consumer<T> consumer) {
        consumer.accept(a);
    }

}
