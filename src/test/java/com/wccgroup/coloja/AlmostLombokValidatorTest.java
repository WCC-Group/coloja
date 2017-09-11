package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.wccgroup.coloja.almostlombokobjects.NonPrimitiveData;
import com.wccgroup.coloja.almostlombokobjects.NonPrimitiveValue;

public class AlmostLombokValidatorTest
{
	@Rule
	public ExpectedException _thrown = ExpectedException.none();

	protected void expect(Class<? extends Throwable> type)
	{
		_thrown.expect(type);
	}

	protected void expect(Class<? extends Throwable> type, String expectedMessage)
	{
		_thrown.expect(type);
		_thrown.expectMessage(expectedMessage);
	}

	@Test
	public void shouldCoverLombokObjects()
	{
		ValidatorOptions options =
			ValidatorOptions.builder()
				.ignorableMethods(Arrays.asList(
					new IgnorableMethod(NonPrimitiveData.class, "helperMethod1"),
					new IgnorableMethod(NonPrimitiveValue.class, "helperMethod1")))
				.ignorableProperties(Arrays.asList(
					new IgnorableProperty(NonPrimitiveData.class, "ADerivedProperty"),
					new IgnorableProperty(NonPrimitiveValue.class, "ADerivedProperty")
				))
				.build();

		int classesValidated = LombokValidator.autoValidate(NonPrimitiveData.class.getPackage().getName(), options).size();

		assertThat(
			"Expect all classes to be validated",
			classesValidated,
			is(2));
	}

	@Test
	public void shouldFailWithAlmostLombokObjectsOnDefaultConfiguration()
	{
		expect(AssertionError.class, "Unexpected method");
		LombokValidator.autoValidate(NonPrimitiveData.class.getPackage().getName()).size();
	}
}
