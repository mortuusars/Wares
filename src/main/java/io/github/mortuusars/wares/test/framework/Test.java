package io.github.mortuusars.wares.test.framework;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class Test {
    public String name;
    public Consumer<ServerPlayer> test;

    public Test(String name, Consumer<ServerPlayer> test) {
        this.name = name;
        this.test = test;
    }
}
