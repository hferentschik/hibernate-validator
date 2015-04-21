/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Calendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.spi.time.TimeProvider;

/**
 * Check that the {@code java.time.chrono.ChronoLocalDateTime} passed is in the future.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class FutureValidatorForChronoLocalDateTime implements ConstraintValidator<Future, ChronoLocalDateTime<?>> {

	@Override
	public void initialize(Future constraintAnnotation) {

	}

	@Override
	public boolean isValid(ChronoLocalDateTime<?> value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		TimeProvider timeProvider = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider();

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis( timeProvider.getCurrentTime() );

		return value.isAfter( LocalDateTime.ofInstant( now.toInstant(), now.getTimeZone().toZoneId() ) );
	}
}
