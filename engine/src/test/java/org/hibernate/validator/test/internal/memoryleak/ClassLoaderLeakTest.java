/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.memoryleak;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.FileAssert.fail;

/**
 * A test to verify that {@code Validator} instances do nto hold on to their @{code ClassLoader} and can
 * be garbage collected when the classloader is garbage collected.
 *
 * @author Hardy Ferentschik
 */
public class ClassLoaderLeakTest {

	private ClassLoader hibernateValidatorClassLoader;

	@BeforeClass
	public void createHibernateValidatorClassLoader() {
		hibernateValidatorClassLoader = new CustomValidatorClassLoader( "org.hibernate.validator" );
	}

	@Test
	public void testValidatorCanBeGarbageCollected() throws Exception {
		CustomValidatorClassLoader appClassLoader = new CustomValidatorClassLoader(
				"org.hibernate.validator.test",
				hibernateValidatorClassLoader
		);

		MemoryLeakTracker memoryLeakTracker = new MemoryLeakTracker();
		memoryLeakTracker.register( appClassLoader, "classLoader" );

		Object executableValidator = createExecutableValidatorViaReflection( appClassLoader );
		Object foo = getFooInstance( appClassLoader );
		invokeMethod(
				executableValidator,
				"validateParameters",
				new Class<?>[] { Object.class, Method.class, Object[].class, Class[].class },
				new Object[] {
						foo,
						foo.getClass().getDeclaredMethods()[0],
						new Object[] { "foo" },
						new Class[] { }
				}
		);

//		Thread.currentThread().sleep( 30000 );

		// nullify all created instances
		appClassLoader = null;
		executableValidator = null;
		foo = null;

		if ( !memoryLeakTracker.isGarbageCollectable( "classLoader" ) ) {
//			Thread.currentThread().sleep( 30000 );
			fail( "classloader instance not gc-able!" );
		}
	}

	private Object getFooInstance(ClassLoader classLoader) throws Exception {
		Class<?> fooClazz = loadClass(
				"org.hibernate.validator.test.internal.memoryleak.FooImpl",
				classLoader
		);
		return createInstance( fooClazz );
	}

	private Object createValidatorInstanceViaReflection(ClassLoader classLoader) throws Exception {
		Object hibernateValidatorInstance = createInstance(
				loadClass(
						"org.hibernate.validator.HibernateValidator",
						classLoader
				)
		);

		Object hibernateValidatorConfiguration = createInstance(
				getConstructor(
						"org.hibernate.validator.internal.engine.ConfigurationImpl",
						classLoader,
						new Class[] { ValidationProvider.class }
				),
				hibernateValidatorInstance
		);

		invokeMethod(
				hibernateValidatorConfiguration, "messageInterpolator", new Class[] { MessageInterpolator.class },
				new Object[] {
						invokeMethod(
								hibernateValidatorConfiguration,
								"getDefaultMessageInterpolator",
								new Object[] { }
						)
				}
		);

		invokeMethod(
				hibernateValidatorConfiguration, "traversableResolver", new Class[] { TraversableResolver.class },
				new Object[] {
						invokeMethod(
								hibernateValidatorConfiguration,
								"getDefaultTraversableResolver",
								new Object[] { }
						)
				}
		);

		invokeMethod(
				hibernateValidatorConfiguration,
				"constraintValidatorFactory",
				new Class[] { ConstraintValidatorFactory.class },
				new Object[] {
						invokeMethod(
								hibernateValidatorConfiguration,
								"getDefaultConstraintValidatorFactory",
								new Object[] { }
						)
				}
		);

		invokeMethod(
				hibernateValidatorConfiguration, "parameterNameProvider", new Class[] { ParameterNameProvider.class },
				new Object[] {
						invokeMethod(
								hibernateValidatorConfiguration,
								"getDefaultParameterNameProvider",
								new Object[] { }
						)
				}
		);

		Object validatorFactory = createInstance(
				getConstructor(
						"org.hibernate.validator.internal.engine.ValidatorFactoryImpl",
						classLoader,
						new Class[] { ConfigurationState.class }
				),
				hibernateValidatorConfiguration
		);

		return invokeMethod( validatorFactory, "getValidator", new Object[] { } );
	}

	private Object createExecutableValidatorViaReflection(ClassLoader classLoader) throws Exception {
		Object validator = createValidatorInstanceViaReflection( classLoader );
		return invokeMethod( validator, "forExecutables", new Object[] { } );
	}

	private Class<?> loadClass(String fqcn, ClassLoader classLoader) throws Exception {
		return classLoader.loadClass( fqcn );
	}

	private Constructor<?> getConstructor(String fqcn, ClassLoader classLoader, Class<?>[] paramTypes)
			throws Exception {
		Class<?> clazz = loadClass( fqcn, classLoader );
		Constructor<?> constructor = clazz.getConstructor( paramTypes );
		constructor.setAccessible( true );
		return constructor;
	}

	private Object createInstance(Class<?> clazz, Object[]... params) throws Exception {
		Constructor<?> constructor = clazz.getConstructor();
		return createInstance( constructor, params );
	}

	private Object createInstance(Constructor<?> constructor, Object... params) throws Exception {
		return constructor.newInstance( params );
	}

	private Object invokeMethod(Object instance, String methodName, Object[] params) throws Exception {
		Method method = instance.getClass().getMethod( methodName );
		return method.invoke( instance, params );
	}


	private Object invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object[] params)
			throws Exception {
		Method method = instance.getClass().getMethod( methodName, paramTypes );
		return method.invoke( instance, params );
	}
}
