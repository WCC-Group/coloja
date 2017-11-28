package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.wccgroup.coloja.almostlombokobjects.*;

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
					new IgnorableProperty(NonPrimitiveValue.class, "ADerivedProperty"),
					new IgnorableProperty(FieldExclusionsData.class, "ignoredInEqualsAndHashCode"),
					new IgnorableProperty(FieldExclusionsValue.class, "ignoredInEqualsAndHashCode")
				))
				.build();

		int classesValidated = LombokValidator.autoValidate(NonPrimitiveData.class.getPackage().getName(), options).size();

		assertThat(
			"Expect all classes to be validated",
			classesValidated,
			is(4));
	}

	@Test
	public void shouldFailWithAlmostLombokObjectsOnDefaultConfiguration()
		throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		expect(AssertionError.class, "Unexpected method");
		LombokValidator.validate(NonPrimitiveData.class);
	}

	@Test
	public void shouldFailWithAlmostLombokObjectsOnDefaultConfiguration1()
		throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		expect(AssertionError.class, "Unexpected method");
		LombokValidator.validate(NonPrimitiveValue.class);
	}

	@Test
	public void shouldFailWithAlmostLombokObjectsOnDefaultConfiguration2()
		throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		expect(AssertionError.class, "and expecting them to be not equal");
		LombokValidator.validate(FieldExclusionsData.class);
	}
}
