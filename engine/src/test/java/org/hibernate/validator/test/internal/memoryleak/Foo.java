package org.hibernate.validator.test.internal.memoryleak;

import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public interface Foo {
	void foo(@Size(min = 4) String s);
}


