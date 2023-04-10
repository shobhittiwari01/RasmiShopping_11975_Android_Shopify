package com.shopify.buy3;

public abstract class Directive {
    private final String name;

    protected Directive(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
