package com.syttech.syttech.scheduler;

import com.syttech.syttech.scheduler.support.IntegrationTestBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Full Spring context smoke test backed by a Postgres Testcontainer.
 *
 * <p>Skipped by default because Testcontainers 1.21.x bundles {@code docker-java} 3.4.x which
 * negotiates Docker API {@code v1.32}, while Docker Engine 29+ requires {@code v1.40+}. Enable with
 * {@code -Dintegration=true} once Testcontainers ships a docker-java version that defaults to a
 * newer API (or when running against an older Docker Engine / Colima).
 */
@EnabledIfSystemProperty(named = "integration", matches = "true")
class SytTechSchedulerServiceApplicationTests extends IntegrationTestBase {

    @Test
    void contextLoads() {}
}
