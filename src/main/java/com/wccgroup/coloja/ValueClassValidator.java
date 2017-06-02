package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.beans.ConstructorProperties;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class ValueClassValidator
{
	public static void validate(Class<?> clazz)
	{
		try
		{
			// Just poke the thing
			invokeAllArgsConstructor(clazz);

			Object instance = ObjectBuilder.createInstance(clazz);
			validateFromMethods(clazz, instance);
			validateFromFields(clazz);
		}
		catch (Exception e)
		{
			ExceptionToFailure.handle(e, clazz);
		}
	}

	private static void validateFromMethods(Class<?> clazz, Object instance)
		throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException
	{
		// Direct method based checks. Only allow recognized methods.
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
				fail(String.format("Immutable objects may not have setters, found %s", method.getName()));
			}
			else if (SpecialMembers.HASH_CODE.equals(method.getName()))
			{
				// We're lucky here. hashCode is not normally poked by jacoco, so we can get away with just covering it.
				method.invoke(instance);
			}
			else if (SpecialMembers.CAN_EQUAL.equals(method.getName()))
			{
				// The thing is protected by default.
				method.setAccessible(true);
				method.invoke(instance, new Object[] { null });
			}
			else if (SpecialMembers.EQUALS_METHOD.equals(method.getName()))
			{
				validateEquals(clazz);
			}
			else if (SpecialMembers.JACOCO_HELPER_METHOD.equals(method.getName()))
			{
				// Expected method. Not invoking..
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
				fail("Unexpected method " + method.getName());
			}
		}
	}

	private static void validateFromFields(Class<?> clazz)
		throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
	{
		// We're testing a lombok object, so make use of a high level commons-beanutils to intepret the fields as properties and
		// validate they behave as we expect them to.
		// It is a bit hairy, as the property names use a different casing from the constructor properties.

		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(clazz);
		Constructor theConstructor = ConstructorInspector.getBestConstructor(clazz);
		ConstructorProperties constructorProperties =
			(ConstructorProperties)theConstructor.getAnnotation(ConstructorProperties.class);

		Map<String, Object> constructorParameters1 = new HashMap<>();
		Map<String, Object> constructorParameters2 = new HashMap<>();
		Object[] arguments1 = new Object[constructorProperties.value().length];
		Object[] arguments2 = new Object[constructorProperties.value().length];

		for (int i = 0; i < constructorProperties.value().length; i++)
		{
			Object value1 = ObjectBuilder.createValue(theConstructor.getParameterTypes()[i], ObjectBuilder.ValueSet.SET1);
			arguments1[i] = value1;
			constructorParameters1.put(constructorProperties.value()[i].toLowerCase(), value1);

			Object value2 = ObjectBuilder.createValue(theConstructor.getParameterTypes()[i], ObjectBuilder.ValueSet.SET2);
			arguments2[i] = value2;
			constructorParameters2.put(constructorProperties.value()[i].toLowerCase(), value2);
		}

		Object theInstance1 = theConstructor.newInstance(arguments1);
		Object theInstance2 = theConstructor.newInstance(arguments2);

		for (PropertyDescriptor property : properties)
		{
			if (SpecialMembers.CLASS.equals(property.getName()))
			{
				continue;
			}

			assertThat(
				property.getReadMethod().invoke(theInstance1),
				is(constructorParameters1.get(property.getName().toLowerCase())));
			assertThat(
				property.getReadMethod().invoke(theInstance2),
				is(constructorParameters2.get(property.getName().toLowerCase())));
		}
	}

	private static void validateEquals(Class<?> clazz)
		throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException
	{
		Constructor theConstructor = ConstructorInspector.getBestConstructor(clazz);
		Class<?>[] parameterTypes = theConstructor.getParameterTypes();

		// Items in list 1 should evaluate to .equals()==false with items from list 2.
		Object[] argumentList1 = new Object[parameterTypes.length];
		Object[] argumentList2 = new Object[parameterTypes.length];

		for (int i = 0; i < parameterTypes.length; i++)
		{
			Class<?> parameterType = parameterTypes[i];
			argumentList1[i] = ObjectBuilder.createValue(parameterType, ObjectBuilder.ValueSet.SET1);
			argumentList2[i] = ObjectBuilder.createValue(parameterType, ObjectBuilder.ValueSet.SET2);
		}

		trivialEqualsChecks(theConstructor, argumentList1);

		singleFieldDifferenceChecks(theConstructor, parameterTypes, argumentList1, argumentList2);

		singleNullFieldChecks(theConstructor, parameterTypes, argumentList1);

		symmetricalNullChecks(theConstructor, parameterTypes, argumentList1);

		subclassCheck(clazz, theConstructor, argumentList1);
	}

	private static void subclassCheck(Class<?> clazz, Constructor theConstructor, Object[] argumentList1)
		throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		if (Modifier.isFinal(clazz.getModifiers()))
		{
			return;
		}

		// Non final class, so we the canEqual should have been generated. Test is.
		Object anInstance = theConstructor.newInstance(argumentList1);
		Object mockedSubclass = Mockito.mock(clazz);
		Method canEqual = mockedSubclass.getClass().getDeclaredMethod(SpecialMembers.CAN_EQUAL, Object.class);
		canEqual.setAccessible(true);
		Mockito.when(canEqual.invoke(mockedSubclass, new Object[] { ArgumentMatchers.any() })).thenReturn(false);
		assertThat(anInstance.equals(mockedSubclass), is(false));
	}

	private static void symmetricalNullChecks(Constructor theConstructor, Class<?>[] parameterTypes, Object[] argumentList1)
		throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		// Identical argument lists with a single shared entry set to null.
		for (int i = 0; i < parameterTypes.length; i++)
		{
			Object[] argumentList3 = new Object[parameterTypes.length];
			Object[] argumentList4 = new Object[parameterTypes.length];

			for (int j = 0; j < parameterTypes.length; j++)
			{
				argumentList3[j] = i == j ? ObjectBuilder.createNull(parameterTypes[i]) : argumentList1[j];
				argumentList4[j] = i == j ? ObjectBuilder.createNull(parameterTypes[i]) : argumentList1[j];
			}

			Object theInstance1 = theConstructor.newInstance(argumentList3);
			Object theInstance2 = theConstructor.newInstance(argumentList4);

			assertThat(
				theInstance1.equals(theInstance2),
				is(true));
		}
	}

	private static void singleNullFieldChecks(Constructor theConstructor, Class<?>[] parameterTypes, Object[] argumentList1)
		throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		// One of the fields null, equals should always be false (our own fields are non-null).
		for (int i = 0; i < parameterTypes.length; i++)
		{
			Object[] argumentList3 = new Object[parameterTypes.length];
			Object[] argumentList4 = new Object[parameterTypes.length];

			for (int j = 0; j < parameterTypes.length; j++)
			{
				argumentList3[j] = argumentList1[j];
				argumentList4[j] = i == j ? ObjectBuilder.createNull(parameterTypes[i]) : argumentList1[j];
			}

			Object theInstance1 = theConstructor.newInstance(argumentList3);
			Object theInstance2 = theConstructor.newInstance(argumentList4);

			assertThat(
				theInstance1.equals(theInstance2),
				is(false));
		}
	}

	private static void singleFieldDifferenceChecks(
		Constructor theConstructor,
		Class<?>[] parameterTypes,
		Object[] argumentList1,
		Object[] argumentList2) throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		// Single field difference: expect same outcome as equals just on that field.
		for (int i = 0; i < parameterTypes.length; i++)
		{
			Object[] argumentList3 = new Object[parameterTypes.length];
			Object[] argumentList4 = new Object[parameterTypes.length];

			for (int j = 0; j < parameterTypes.length; j++)
			{
				argumentList3[j] = argumentList1[j];
				argumentList4[j] = i == j ? argumentList2[j] : argumentList1[j];
			}

			Object theInstance1 = theConstructor.newInstance(argumentList3);
			Object theInstance2 = theConstructor.newInstance(argumentList4);

			assertThat(theInstance1.equals(theInstance2), is(argumentList1[i].equals(argumentList2[i])));
		}
	}

	private static void trivialEqualsChecks(Constructor theConstructor, Object[] argumentList1)
		throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		// Same set of fields
		assertThat(
			theConstructor.newInstance(argumentList1).equals(theConstructor.newInstance(argumentList1)),
			is(true));
		// Nulls
		assertThat(theConstructor.newInstance(argumentList1).equals(null), is(false)); // NOSONAR
		// Some other object
		assertThat(theConstructor.newInstance(argumentList1).equals(new LombokValidator()), is(false));

		Object itself = theConstructor.newInstance(argumentList1);
		assertThat(itself.equals(itself), is(true));//NOSONAR
	}

	private static void invokeAllArgsConstructor(Class<?> clazz)
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
