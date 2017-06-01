package com.wccgroup.coloja;

import static org.junit.Assert.assertThat;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LombokValidator
{
	public static List<Class> autoValidate(String namespacePrefix)
	{
		List<Class> validatedClasses = new ArrayList<>();

		for (String className : new Reflections(namespacePrefix, new SubTypesScanner(false)).getAllTypes())
		{
			Class<?> classDefinition;

			try
			{
				classDefinition = Heuristics.isLombokPojo(className);
			}
			catch (ClassNotFoundException | NoClassDefFoundError e)
			{
				log.debug("Not validating '" + className + "' because the class could not be loaded: " + e.getMessage(), e);
				continue;
			}

			if (classDefinition != null)
			{
				try
				{
					log.info("Validating '{}' because it appears to be a lombok object.", className);
					validate(classDefinition);
					validatedClasses.add(classDefinition);
				}
				catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| InvocationTargetException e)
				{
					log.warn("Failure", e);
					org.junit.Assert.fail("Could not validate type '" + className + "' as being a valid Lombok object");
				}
			}
			else
			{
				log.debug("Not validating because it doesn't appear to be a lombok object: {}.", className);
			}
		}

		assertThat("Expect at least 1 lombok object", validatedClasses.size(), org.hamcrest.Matchers.greaterThan(0));

		return validatedClasses;
	}

	public static void validate(Class<?> clazz)
		throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
	{
		if (Heuristics.isImmutable(clazz))
		{
			log.info("{} should be an immutable pojo", clazz);
			ValueClassValidator.validate(clazz);
		}
		else
		{
			log.info("{} should be a mutable pojo", clazz);
			DataClassValidator.validate(clazz);
		}
	}
}
