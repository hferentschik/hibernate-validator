/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util;

import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
public final class Version {
	static {
		LoggerFactory.make().version( getVersionString() );
	}

	public static String getVersionString() {
		return "[WORKING]";
	}

	public static void touch() {
	}

	/**
	 * Returns the Java release for the current runtime
	 *
	 * @return the Java release as an integer (e.g. 8 for Java 8)
	 */
	public static int getJavaRelease() {
		// Will return something like 1.8
		String[] specificationVersion = System.getProperty("java.specification.version").split("\\.");

		return Integer.parseInt(specificationVersion[1]);
	}

	// helper class should not have a public constructor
	private Version() {
	}
}
