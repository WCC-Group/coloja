package com.wccgroup.coloja;

import static org.junit.Assert.fail;
import java.beans.ConstructorProperties;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import org.mockito.Mockito;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectBuilder
{
	private static final List<Class> SIMPLE_TYPES = Arrays.asList(
		boolean.class, Boolean.class,
		byte.class, Byte.class,
		char.class, Character.class,
		short.class, Short.class,
		int.class, Integer.class,
		long.class, Long.class,
		float.class, Float.class,
		double.class, Double.class,
		String.class);

	public enum ValueSet
	{
		SET1,
		SET2
	}

	public static Object createValue(Class<?> propertyType, ValueSet valueSet)
	{
		if (propertyType.isAssignableFrom(Boolean.class) ||
			propertyType.isAssignableFrom(boolean.class))
		{
			return valueSet.equals(ValueSet.SET1);
		}
		else if (propertyType.isAssignableFrom(Byte.class) ||
			propertyType.isAssignableFrom(byte.class))
		{
			return valueSet.equals(ValueSet.SET1) ? (byte)1 : (byte)2;
		}
		else if (propertyType.isAssignableFrom(Character.class) ||
			propertyType.isAssignableFrom(char.class))
		{
			return valueSet.equals(ValueSet.SET1) ? 'A' : 'B';
		}
		else if (propertyType.isAssignableFrom(Short.class) ||
			propertyType.isAssignableFrom(short.class))
		{
			return valueSet.equals(ValueSet.SET1) ? (short)1 : (short)2;
		}
		else if (propertyType.isAssignableFrom(Integer.class) ||
			propertyType.isAssignableFrom(int.class))
		{
			return valueSet.equals(ValueSet.SET1) ? 1 : 2;
		}
		else if (propertyType.isAssignableFrom(Long.class) ||
			propertyType.isAssignableFrom(long.class))
		{
			return valueSet.equals(ValueSet.SET1) ? 1L : 2L;
		}
		else if (propertyType.isAssignableFrom(Float.class) ||
			propertyType.isAssignableFrom(float.class))
		{
			return valueSet.equals(ValueSet.SET1) ? 1.0f : 2.0f;
		}
		else if (propertyType.isAssignableFrom(Double.class) ||
			propertyType.isAssignableFrom(double.class))
		{
			return valueSet.equals(ValueSet.SET1) ? 1.0 : 2.0;
		}
		else if (propertyType.isAssignableFrom(String.class))
		{
			return valueSet.equals(ValueSet.SET1) ? "A" : "B";
		}

		else if (propertyType.isEnum())
		{
			return valueSet.equals(ValueSet.SET1) ? propertyType.getEnumConstants()[0] : propertyType.getEnumConstants()[1];
		}
		else if (propertyType.getName().startsWith("java.util.Map"))
		{
			return new HashMap();
		}
		else if (propertyType.getName().startsWith("java.util.List"))
		{
			return new ArrayList();
		}
		else if (propertyType.isAssignableFrom(ZonedDateTime.class))
		{
			return valueSet.equals(ValueSet.SET1) ?
				ZonedDateTime.parse("2016-01-02T12:34:56Z") :
				ZonedDateTime.parse("1999-12-31T12:34:56Z");
		}
		else
		{
			return ObjectBuilder.createInstance(propertyType, true);
		}
	}

	public static Object createNull(Class<?> propertyType)
	{
		if (propertyType.isAssignableFrom(Boolean.class) ||
			propertyType.isAssignableFrom(boolean.class))
		{
			return false;
		}
		else if (propertyType.isAssignableFrom(Byte.class) ||
			propertyType.isAssignableFrom(byte.class))
		{
			return (byte)0;
		}
		else if (propertyType.isAssignableFrom(Character.class) ||
			propertyType.isAssignableFrom(char.class))
		{
			return (char)0;
		}
		else if (propertyType.isAssignableFrom(Short.class) ||
			propertyType.isAssignableFrom(short.class))
		{
			return (short)0;
		}
		else if (propertyType.isAssignableFrom(Integer.class) ||
			propertyType.isAssignableFrom(int.class))
		{
			return 0;
		}
		else if (propertyType.isAssignableFrom(Long.class) ||
			propertyType.isAssignableFrom(long.class))
		{
			return 0L;
		}
		else if (propertyType.isAssignableFrom(Float.class) ||
			propertyType.isAssignableFrom(float.class))
		{
			return 0.0f;
		}
		else if (propertyType.isAssignableFrom(Double.class) ||
			propertyType.isAssignableFrom(double.class))
		{
			return 0.0;
		}
		else
		{
			return null;
		}
	}

	public static Object createInstance(Class<?> objectType)
	{
		return createInstance(objectType, false);
	}

	@SuppressWarnings("rawtypes")
	public static Object createInstance(Class<?> objectType, boolean allowProxy)
	{
		if (SIMPLE_TYPES.stream().anyMatch(simpleType -> objectType.isAssignableFrom(simpleType)))
		{
			return createValue(objectType, ValueSet.SET1);
		}

		if (objectType.isEnum())
		{
			return objectType.getEnumConstants()[0];
		}
		else if (objectType.getName().startsWith("java.util.Map"))
		{
			return new HashMap();
		}
		else if (objectType.getName().startsWith("java.util.List"))
		{
			return new ArrayList();
		}
		else if (objectType.getName().equals("java.util.UUID"))
		{
			return UUID.randomUUID();
		}
		else if (objectType.isAssignableFrom(ZonedDateTime.class))
		{
			return ZonedDateTime.parse("2016-01-02T12:34:56Z");
		}
		else if (objectType.isAssignableFrom(LocalDate.class))
		{
			return LocalDate.parse("2016-01-02");
		}
		else if (objectType.equals(java.lang.reflect.Method.class))
		{
			return ObjectBuilder.class.getMethods()[0];
		}
		else if (objectType.equals(java.lang.Class.class))
		{
			return ObjectBuilder.class;
		}
		else
		{
			if (allowProxy && !Modifier.isFinal(objectType.getModifiers()))
			{
				return Mockito.mock(objectType);
			}

			final Constructor theConstructor = ConstructorInspector.getBestConstructor(objectType);

			if (theConstructor == null)
			{
				// If you get here and feel like something is missing just above, feel free to add it.
				fail(String.format(
					"Tried to create an instance of %s, but it contains no usable constructors.",
					objectType.getName()));
			}

			if (theConstructor.getParameterCount() == 0)
			{
				// Trivial constructor, yay.
				try
				{
					return theConstructor.newInstance();
				}
				catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
				{
					ExceptionToFailure.handle(e, objectType);
				}
			}
			else
			{
				// Bah
				ConstructorProperties constructorProperties =
					(ConstructorProperties)theConstructor.getAnnotation(ConstructorProperties.class);

				if (constructorProperties == null)
				{
					fail("Expected a ConstructorProperties annotation on " + objectType.getName());
				}

				List<Object> theParameters = new ArrayList<>();

				for (int i = 0; i < theConstructor.getParameterTypes().length; i++)
				{
					Class<?> parameterType = theConstructor.getParameterTypes()[i];
					String parameterName = constructorProperties.value()[i];

					theParameters.add(createInstance(parameterType, true));
				}

				try
				{
					return theConstructor.newInstance(theParameters.toArray());
				}
				catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
				{
					ExceptionToFailure.handle(e, objectType);
				}
			}

			return null; // Never hit
		}
	}
}
