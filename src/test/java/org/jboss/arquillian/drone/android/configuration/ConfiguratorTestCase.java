/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.android.configuration;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.android.AndroidConfigurationException;
import org.jboss.arquillian.android.configuration.AndroidSdk;
import org.jboss.arquillian.android.configuration.AndroidSdkConfiguration;
import org.jboss.arquillian.android.event.AndroidSdkConfigured;
import org.jboss.arquillian.android.impl.AndroidSdkConfigurator;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests Destroyer activation when no context was created (no @Drone) won't fail
 * 
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfiguratorTestCase extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidSdkConfigurator.class);
    }

    @Test
    public void validConfiguration() throws Exception {

        // set mocks
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
                .extension(AndroidSdkConfigurator.ANDROID_SDK_EXTENSION_NAME).property("skip", "true");

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);

        getManager().getContext(ClassContext.class).activate(DummyClass.class);

        Object instance = new DummyClass();
        Method testMethod = DummyClass.class.getMethod("testDummyMethod");

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        AndroidSdkConfiguration configuration = getManager().getContext(SuiteContext.class).getObjectStore()
                .get(AndroidSdkConfiguration.class);
        Assert.assertNotNull("Android SDK configuration was created in the context", configuration);

        fire(new BeforeClass(DummyClass.class));
        fire(new Before(instance, testMethod));

        fire(new After(instance, testMethod));
        fire(new AfterClass(DummyClass.class));

        // assert Android SDK event was passed
        assertEventFired(AndroidSdkConfigured.class, 1);
    }

    @Test(expected = AndroidConfigurationException.class)
    public void invalidPlatformConfiguration() throws Exception {

        // set mocks
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
                .extension(AndroidSdkConfigurator.ANDROID_SDK_EXTENSION_NAME).property("skip", "true")
                .property("apiLevel", "2.3.3");

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);

        getManager().getContext(ClassContext.class).activate(DummyClass.class);

        Object instance = new DummyClass();
        Method testMethod = DummyClass.class.getMethod("testDummyMethod");

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        AndroidSdkConfiguration configuration = getManager().getContext(SuiteContext.class).getObjectStore()
                .get(AndroidSdkConfiguration.class);
        Assert.assertNotNull("Android SDK configuration was created in the context", configuration);

        AndroidSdk sdk = getManager().getContext(SuiteContext.class).getObjectStore().get(AndroidSdk.class);
        Assert.assertNotNull("Android SDK was created in the context", sdk);

        fire(new BeforeClass(DummyClass.class));
        fire(new Before(instance, testMethod));

        fire(new After(instance, testMethod));
        fire(new AfterClass(DummyClass.class));

        // assert Android SDK event was passed
        assertEventFired(AndroidSdkConfigured.class, 1);
    }

    static class DummyClass {
        public void testDummyMethod() {
        }
    }
}
