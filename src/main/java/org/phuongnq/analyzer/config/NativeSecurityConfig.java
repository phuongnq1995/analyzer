package org.phuongnq.analyzer.config;

import org.phuongnq.analyzer.config.registor.JjwtRuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportRuntimeHints(JjwtRuntimeHints.class)
public class NativeSecurityConfig {
}