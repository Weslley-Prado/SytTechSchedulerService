package com.syttech.syttech.scheduler.scheduler.config;

import org.springframework.context.annotation.Configuration;

/**
 * Módulo Scheduler — configuração central.
 *
 * <p>Domain services devem ser registrados aqui como {@code @Bean} assim que forem criados, para
 * manter o domínio livre de anotações Spring (mesmo padrão do módulo {@code crm} do {@code
 * syttechportal}).
 */
@Configuration
public class SchedulerModuleConfig {}
