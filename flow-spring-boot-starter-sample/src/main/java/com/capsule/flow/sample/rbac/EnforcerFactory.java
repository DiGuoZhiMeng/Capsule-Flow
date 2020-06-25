package com.capsule.flow.sample.rbac;

import org.casbin.jcasbin.main.Enforcer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class EnforcerFactory implements InitializingBean {

    private static Enforcer enforcer;

    @Override
    public void afterPropertiesSet() {
        String modePath = this.getClass().getClassLoader().getResource("config/rbac_model.conf").getPath();
        String policyPath = this.getClass().getClassLoader().getResource("config/rbac_policy.csv").getPath();
        enforcer = new Enforcer(modePath, policyPath);
        enforcer.loadPolicy(); // Load the policy
    }

    public static Enforcer getEnforcer() {
        return enforcer;
    }
}