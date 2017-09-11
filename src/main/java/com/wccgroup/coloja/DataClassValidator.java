package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataClassValidator
{
	public static void validate(Class<?> clazz, ValidatorOptions options)
	{
		try
		{
			invokeAllArgsConstructors(clazz);

			Constructor<?> constructor = clazz.getConstructor();
			Object instance = constructor.newInstance();

			validateFromMethods(clazz, instance, options);
			validateFromProperties(clazz, instance);
		}
		catch (Exception e)
		{
			ExceptionToFailure.handle(e, clazz);
		}
	}

	/**
	 * Method based validation. Look at the methods and figure out if we recognize all of the. Do further validation
	 * on a few special ones.
	 */
	private static void validateFromMethods(Class<?> clazz, Object instance, ValidatorOptions options)
		throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
	{
		for (Method method : clazz.getDeclaredMethods())
		{
			if (method.getName().startsWith("get") ||
				method.getName().startsWith("is") ||
				SpecialMembers.TO_STRING.equals(method.getName()))
			{
				method.invoke(instance);
			}
			else if (method.getName().startsWith("set"))
			{
				Object argumentValue = ObjectBuilder.createValue(method.getParameterTypes()[0], ObjectBuilder.ValueSet.SET1);
				method.invoke(instance, argumentValue);
			}
			else if (SpecialMembers.HASH_CODE.equals(method.getName()))
			{
				method.invoke(instance);

				validateHashCode(clazz);
			}
			else if (method.getName().equals(SpecialMembers.CAN_EQUAL))
			{
				method.setAccessible(true);
				method.invoke(instance, new Object[] { null });
			}
			else if (SpecialMembers.EQUALS_METHOD.equals(method.getName()))
			{
				validateEquals(clazz);
			}
			else if (SpecialMembers.JACOCO_HELPER_METHOD.equals(method.getName()))
			{
				// This method is only visible when running code coverage, we should ignore it.
			}
			else if (SpecialMembers.BUILDER.equals(method.getName()))
			{
				if (!Modifier.isStatic(method.getModifiers()))
				{
					fail("builder method should have been static");
				}
				else
				{
					Object builder = method.invoke(null);
					assertThat(builder, is(notNullValue()));
				}
			}
			else
			{
				boolean shouldBeIgnored = false;

				for (IgnorableMethod ignorableMethod : options.getIgnorableMethods())
				{
					if (clazz.equals(ignorableMethod.getClazz()) && method.getName().equals(ignorableMethod.getMethod()))
					{
						shouldBeIgnored = true;
					}
				}

				if (!shouldBeIgnored)
				{
					fail("Unexpected method " + method.getName());
				}
			}
		}
	}

	/**
	 * Use commons-beanutils to do the basic setFoo(bar) -> getFoo() == bar validation.
	 */
	private static void validateFromProperties(Class<?> clazz, Object instance)
		throws IllegalAccessException, InstantiationException, InvocationTargetException
	{
		// We're testing a lombok object, so make use of a high level commons-beanutils to intepret the fields as properties and
		// validate they behave as we expect them to.
		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(clazz);

		for (PropertyDescriptor property : properties)
		{
			if (SpecialMembers.CLASS.equals(property.getName()))
			{
				continue;
			}

			Class<?> propertyType = property.getPropertyType();

			// Not every property has a setter (e.g. getSomeList()).
			if (property.getWriteMethod() != null)
			{
				Object value = ObjectBuilder.createValue(property.getPropertyType(), ObjectBuilder.ValueSet.SET1);
				property.getWriteMethod().invoke(instance, value);
				Object retrievedValue = property.getReadMethod().invoke(instance);

				assertThat(retrievedValue, is(value));
			}
		}
	}

	/**
	 * Test all properties, making sure that the .equals behavior is correct.
	 */
	private static void validateEquals(final Class<?> clazz)
		throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
	{
		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(clazz);

		for (PropertyDescriptor property : properties)
		{
			if (SpecialMembers.CLASS.equals(property.getName()))
			{
				continue;
			}

			Class<?> propertyType = property.getPropertyType();

			validateEquals(
				clazz,
				ObjectBuilder.createValue(propertyType, ObjectBuilder.ValueSet.SET1),
				ObjectBuilder.createValue(propertyType, ObjectBuilder.ValueSet.SET2),
				property);
		}
	}

	/**
	 * Test a single property, evaluating all sane use-cases for .equals. Starts with an 'empty' lombok object (everything
	 * null/default
	 * value so that .equals() returns true). Then mutates the values for the single property and validates the .equals behavior.
	 */
	private static void validateEquals(
		Class<?> clazz,
		Object value1,
		Object value2,
		PropertyDescriptor property)
		throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException
	{
		Object instance1 = ObjectBuilder.createInstance(clazz);
		Object instance2 = ObjectBuilder.createInstance(clazz);

		// empty instances
		assertThat(instance1.equals(instance2), is(true));
		assertThat(instance1.equals(null), is(false)); //NOSONAR
		assertThat(instance1.equals(instance1), is(true)); // NOSONAR
		assertThat(instance1.equals(new LombokValidator()), is(false));

		Object nullValue = ObjectBuilder.createNull(property.getPropertyType());

		if (property.getWriteMethod() != null)
		{
			// null <--> null
			property.getWriteMethod().invoke(instance1, nullValue);
			property.getWriteMethod().invoke(instance2, nullValue);
			assertThat(instance1.equals(instance2), is(true));
			assertThat(instance2.equals(instance1), is(true));

			// something <--> null
			boolean expected = value2.equals(nullValue);
			property.getWriteMethod().invoke(instance1, nullValue);
			property.getWriteMethod().invoke(instance2, value2);
			assertThat(instance1.equals(instance2), is(expected));
			assertThat(instance2.equals(instance1), is(expected));

			// null <--> something
			expected = value1.equals(nullValue);
			property.getWriteMethod().invoke(instance1, value1);
			property.getWriteMethod().invoke(instance2, nullValue);
			assertThat(instance1.equals(instance2), is(expected));
			assertThat(instance2.equals(instance1), is(expected));

			// something <--> something
			expected = value2.equals(value2); // NOSONAR
			property.getWriteMethod().invoke(instance1, value2);
			property.getWriteMethod().invoke(instance2, value2);
			assertThat(instance1.equals(instance2), is(expected));
			assertThat(instance2.equals(instance1), is(expected));

			// something <--> something else
			expected = value1.equals(value2);
			property.getWriteMethod().invoke(instance1, value1);
			property.getWriteMethod().invoke(instance2, value2);
			assertThat(instance1.equals(instance2), is(expected));
			assertThat(instance2.equals(instance1), is(expected));
		}

		// We really have to mock here, this tests if the other one is a subclass.
		Object x = org.mockito.Mockito.mock(clazz);
		Method canEqual = x.getClass().getDeclaredMethod(SpecialMembers.CAN_EQUAL, Object.class);
		canEqual.setAccessible(true);
		Mockito.when(canEqual.invoke(x, new Object[] { ArgumentMatchers.any() })).thenReturn(false);
		assertThat(instance1.equals(x), is(false));
	}

	/**
	 * We mostly have to generate coverage. Mutation testing isn't done by default on hashCode(). We do however still have to
	 * cover the various branches in hashCode(). All branches are ternary expression for a null check, so we just feed it
	 * something or nulls.
	 */
	private static void validateHashCode(final Class<?> clazz)
		throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
	{
		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(clazz);

		for (PropertyDescriptor property : properties)
		{
			if ("class".equals(property.getName()))
			{
				continue;
			}

			Class<?> propertyType = property.getPropertyType();

			boolean isValueType = ObjectBuilder.createNull(propertyType) != null;
			Object aNonNullValue = ObjectBuilder.createValue(propertyType, ObjectBuilder.ValueSet.SET1);

			validateHashCode(clazz, aNonNullValue, property, !isValueType);
		}
	}

	/**
	 * On a fresh instance, calculate hashCode for a null and a non-null value. Should trigger all branches related to this
	 * property.
	 */
	private static void validateHashCode(Class<?> clazz, Object value, PropertyDescriptor property, boolean tryNull)
		throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException
	{
		Constructor<?> constructor = clazz.getConstructor();
		Object instance = constructor.newInstance();

		if (property.getWriteMethod() != null)
		{
			property.getWriteMethod().invoke(instance, value);
		}

		instance.hashCode();

		if (tryNull && property.getWriteMethod() != null)
		{
			property.getWriteMethod().invoke(instance, new Object[] { null });
			instance.hashCode();
		}
	}

	// We're cheating a little here. Pitest won't mutate this, so we just have to generate coverage.
	private static void invokeAllArgsConstructors(Class<?> clazz)
		throws IllegalAccessException, InvocationTargetException, InstantiationException
	{
		for (Constructor<?> c : clazz.getConstructors())
		{
			if (c.getParameterCount() == 0)
			{
				continue;
			}

			List<Object> parameterValues = new ArrayList<>();

			for (Class<?> parameterType : c.getParameterTypes())
			{
				parameterValues.add(ObjectBuilder.createInstance(parameterType));
			}

			c.newInstance(parameterValues.toArray(new Object[1]));
		}
	}
}
