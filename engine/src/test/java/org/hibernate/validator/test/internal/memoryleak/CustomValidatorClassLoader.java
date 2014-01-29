/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * @author Hardy Ferentschik
 */
public class CustomValidatorClassLoader extends ClassLoader {
	/**
	 * Classes from this name space will be loaded by this class loader, all
	 * others will be loaded by the default loader.
	 */
	private final String prefix;
	private ClassLoader classLoaderParent;

	private final Map<String, Class<?>> classes = newHashMap();

	public CustomValidatorClassLoader(String prefix) {
		super( CustomValidatorClassLoader.class.getClassLoader() );
		this.prefix = prefix;
	}

	public CustomValidatorClassLoader(String prefix, ClassLoader parent) {
		this( prefix );
		this.classLoaderParent = parent;
	}

	@Override
	public Class<?> loadClass(String fqcn) throws ClassNotFoundException {
		if ( fqcn.startsWith( prefix ) ) {
			if ( classes.containsKey( fqcn ) ) {
				return classes.get( fqcn );
			}
			return myLoadClass( fqcn, true );
		}
		else if ( classLoaderParent != null ) {
			return classLoaderParent.loadClass( fqcn );
		}
		else {
			return super.loadClass( fqcn );
		}
	}

	private Class<?> myLoadClass(String fqcn, boolean resolve) throws ClassNotFoundException {
		// make sure there is no parent delegation, instead call custom findClass
		Class<?> clazz = myFindClass( fqcn );

		if ( resolve ) {
			resolveClass( clazz );
		}
		classes.put( fqcn, clazz );
		return clazz;
	}

	private Class<?> myFindClass(String className) throws ClassNotFoundException {
		byte[] classByte;
		Class<?> result;

		try {
			String classPath = ClassLoader.getSystemResource(
					className.replace( '.', '/' ) + ".class"
			).getFile();
			classByte = loadClassData( classPath );
			result = defineClass( className, classByte, 0, classByte.length, null );
			return result;
		}
		catch (Exception e) {
			throw new ClassNotFoundException();
		}
	}

	private byte[] loadClassData(String className) throws IOException {
		File f;
		f = new File( className );
		int size = (int) f.length();
		byte[] buff = new byte[size];
		FileInputStream fis = new FileInputStream( f );
		DataInputStream dis = new DataInputStream( fis );
		dis.readFully( buff );
		dis.close();
		return buff;
	}
}


