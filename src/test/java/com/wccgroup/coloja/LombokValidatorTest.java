package com.wccgroup.coloja;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import org.junit.Test;
import com.wccgroup.coloja.lombokobjects.BuildableValue;
import com.wccgroup.coloja.lombokobjects.SimpleData;

public class LombokValidatorTest
{
	@Test
	public void shouldCoverLombokObjects()
	{
		int classesValidated = LombokValidator.autoValidate(SimpleData.class.getPackage().getName()).size();

		assertThat(
			"Expect all classes to be validated",
			classesValidated,
			is(7));
	}

	@Test
	// This test will only succeed with lombok 1.16.16 or later.
	public void shouldBeDetected()
	{
		Constructor[] constructors = BuildableValue.class.getDeclaredConstructors();
		ConstructorProperties props = (ConstructorProperties)constructors[0].getAnnotation(ConstructorProperties.class);
		assertThat(Heuristics.isLombokGeneratedObject(BuildableValue.class.getName()), is(notNullValue()));
	}
}
