package org.phuongnq.analyzer.config.registor;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class JjwtRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(
            io.jsonwebtoken.impl.security.KeysBridge.class
        );
    }
}