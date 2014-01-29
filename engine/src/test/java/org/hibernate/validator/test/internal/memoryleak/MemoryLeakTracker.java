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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * https://www.ibm.com/developerworks/community/blogs/javavisualization/entry/unittestingmemoryleaks
 *
 * @author Hardy Ferentschik
 */
public class MemoryLeakTracker {
	private HashMap<String, Object> map = new HashMap<String, Object>();

	public void register(Object obj, String id) {
		if ( map.get( id ) != null ) {
			throw new IllegalArgumentException(
					"Object already stored under id " + id
			);
		}
		map.put( id, new WeakReference<Object>( obj, new ReferenceQueue<Object>() ) );
	}

	public boolean isGarbageCollectable(String id) {
		gc(); // ask for garbage collection
		WeakReference ref = (WeakReference) map.get( id );
		if ( ref == null ) {
			throw new RuntimeException(
					"No object stored under id " + id
			);
		}
		return ref.get() == null;
	}


	private void gc() {
		Runtime rt = Runtime.getRuntime();
		for ( int i = 0; i < 3; i++ ) {
			try {
				allocateMemory( (int) ( 2e6 ) );
			}
			catch (Throwable th) {
				th.printStackTrace();
			}
			for ( int j = 0; j < 3; j++ ) {
				rt.gc();
			}
		}
		rt.runFinalization();
		try {
			Thread.currentThread().sleep( 50 );
		}
		catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private void allocateMemory(int memAmount) {
		byte[] big = new byte[memAmount];
		// Fight against clever compilers/JVMs that may not allocate
		// unless we actually use the elements of the array
		int total = 0;
		for ( int i = 0; i < 10; i++ ) {
			// we don't touch all the elements, would take too long.
			if ( i % 2 == 0 ) {
				total += big[i];
			}
			else {
				total -= big[i];
			}
		}
	}
}

