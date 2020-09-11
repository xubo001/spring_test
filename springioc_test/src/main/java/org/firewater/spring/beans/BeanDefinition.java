package org.firewater.spring.beans;

public class BeanDefinition {
    private String beanName;
    private boolean isLazy;
    private String scope;
    private Class clazz;


    public BeanDefinition(String beanName, boolean isLazy, String scope,Class clazz) {
        this.beanName = beanName;
        this.isLazy = isLazy;
        this.scope = scope;
        this.clazz = clazz;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
