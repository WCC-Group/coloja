package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.Test;

public class ObjectBuilderTest
{
	@Test
	public void shouldCreatePrimitiveInstances()
	{
		for (Class clazz : Arrays.asList(
			boolean.class, Boolean.class,
			byte.class, Byte.class,
			char.class, Character.class,
			short.class, Short.class,
			int.class, Integer.class,
			long.class, Long.class,
			float.class, Float.class,
			double.class, Double.class,
			String.class))
		{
			assertThat(
				"Expect non null value for " + clazz.getName(),
				ObjectBuilder.createValue(clazz, ObjectBuilder.ValueSet.SET1),
				is(notNullValue()));
			assertThat(
				"Expect non null value for " + clazz.getName(),
				ObjectBuilder.createValue(clazz, ObjectBuilder.ValueSet.SET2),
				is(notNullValue()));
		}
	}

	@Test
	public void shouldCreateDifferentPrimitiveInstances()
	{
		for (Class clazz : Arrays.asList(
			boolean.class, Boolean.class,
			byte.class, Byte.class,
			char.class, Character.class,
			short.class, Short.class,
			int.class, Integer.class,
			long.class, Long.class,
			float.class, Float.class,
			double.class, Double.class,
			String.class))
		{
			Object value1 = ObjectBuilder.createValue(clazz, ObjectBuilder.ValueSet.SET1);
			Object value2 = ObjectBuilder.createValue(clazz, ObjectBuilder.ValueSet.SET2);
			assertThat(
				"Expect different values for " + clazz.getName(),
				value1.equals(value2),
				is(false));
		}
	}
}