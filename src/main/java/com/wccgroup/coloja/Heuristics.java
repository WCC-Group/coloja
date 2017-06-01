package com.wccgroup.coloja;

import static org.junit.Assert.fail;
import java.beans.ConstructorProperties;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Heuristics
{
	/**
	 * Heuristics for lombok classes:
	 * <p>
	 * 1. Only non-static, non-public fields
	 * 2. Not a test class
	 * 3. No fields start with _ (notice that here sonar really helps as it only allows this for lombok pojos.
	 * 4. Has a canEqual method OR ConstructorProperties annotation on the constructor.
	 * <p>
	 * canEqual is generated for mutable pojos, not for @Value classes, that is why we also check for ConstructorProperties.
	 *
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> isLombokPojo(String className) throws ClassNotFoundException
	{
		Class<?> classDefinition = Class.forName(className); // NOSONAR

		// Test class
		if (classDefinition.getName().endsWith("Test") || classDefinition.getName().endsWith("IT"))
		{
			log.debug("Not a lombok class because it appears to be a test {}", className);
			return null;
		}

		// Some non-static field that isn't private/protected or starts with an underscore.
		if (Arrays.stream(classDefinition.getDeclaredFields())
			.filter(field -> !SpecialMembers.JACOCO_HELPER_FIELD.equals(field.getName()))
			.anyMatch(field -> Modifier.isStatic(field.getModifiers())
				|| Modifier.isPublic(field.getModifiers())
				|| field.getName().startsWith("_")))
		{
			log.debug("Not a lombok class because has a static or public field, or a field that starts with '_': {}", className);
			return null;
		}

		/**
		 * At this point we should only find @lombok.Data classes and
		 * cxf-codegen generated stuff. Only the first one consistently
		 * generates this (protected) method. This is of course heuristics and
		 * it will probably fail somewhere halfway 2017 when I can't remember
		 * ever writing this.
		 */
		boolean hasACanEqualMethod = Arrays.stream(classDefinition.getDeclaredMethods())
			.anyMatch(method -> method.getName().equals(SpecialMembers.CAN_EQUAL));
		boolean hasConstructorPropertiesAnnotation = Arrays.stream(classDefinition.getDeclaredConstructors())
			.anyMatch(constructor -> constructor.getAnnotation(ConstructorProperties.class) != null);

		if (hasACanEqualMethod || hasConstructorPropertiesAnnotation)
		{
			return classDefinition;
		}

		if (!hasACanEqualMethod)
		{
			log.debug("Not a lombok class because it has no canEqual method: {}", className);
		}

		if (!hasConstructorPropertiesAnnotation)
		{
			log.debug("Not a lombok class because it has no ConstructorProperties annotation: {}", className);
		}

		return null;
	}

	/**
	 * Differentiates beween mutable and immutable lombok pojos. Assumes that this class is already detected as lombok pojo. We
	 * assume the class is an immutable pojo iff
	 * <p>
	 * 1. It has 1 constructor
	 * 2. It's only constructor has 1 or more parameters
	 * 3. The number of final fields equals the number of constructor parameters.
	 *
	 * @param clazz
	 * @return
	 */
	public static boolean isImmutable(Class<?> clazz)
	{
		if (clazz.getConstructors().length == 1)
		{
			Constructor constructor = clazz.getConstructors()[0];

			long requiredParameters = constructor.getParameterCount();

			Field[] fields = clazz.getDeclaredFields();

			long numberOfFinalFields =
				Arrays.stream(fields).filter(field -> Modifier.isFinal(field.getModifiers())).count();

			if (requiredParameters > 0 && requiredParameters != numberOfFinalFields)
			{
				// Some fields are not marked as final.
				String fieldsNotMarkedAsFinal = String.join(", ", Arrays.stream(fields)
					.filter(field -> !Modifier.isFinal(field.getModifiers()))
					.map(field -> field.getName())
					.collect(Collectors.toList()));

				fail(String.format(
					"The following fields should be final: %s",
					fieldsNotMarkedAsFinal));
			}

			return requiredParameters == numberOfFinalFields && requiredParameters != 0;
		}

		return false;
	}
}
