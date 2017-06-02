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
	/**
	 * Picks up all classes in the provided namespace and sub-namespaces. Each class is checked with some heuristics. If the
	 * library thinks it is a class generated through an @Value or @Data annotation, it hands it off to the DataClassValidator or
	 * ValueClassValidator. These in turn generate as much coverage as possible, as well as doing some validation.
	 * <p>
	 * An assertion failure will trigger when no objects are found.
	 *
	 * @param namespacePrefix The root namespace from where to start looking for lombok annotated objects.
	 * @return A list of all Classes that have been processed.
	 */
	public static List<Class> autoValidate(String namespacePrefix)
	{
		List<Class> validatedClasses = new ArrayList<>();

		for (String className : new Reflections(namespacePrefix, new SubTypesScanner(false)).getAllTypes())
		{
			Class<?> classDefinition;

			try
			{
				classDefinition = Heuristics.isLombokGeneratedObject(className);
			}
			catch (NoClassDefFoundError e)
			{
				log.debug("Not validating '" + className + "' because the class could not be loaded: " + e.getMessage(), e);
				continue;
			}

			if (classDefinition != null)
			{
				try
				{
					log.debug("Validating '{}' because it appears to be a lombok object.", className);
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

		log.info("Automatically validated {} classes", validatedClasses.size());

		return validatedClasses;
	}

	/**
	 * @param clazz The class to validate. It will both trigger the coverage generator as well as doing some validation on the
	 *              object itself.
	 * @throws InvocationTargetException thrown when the validation fails
	 * @throws NoSuchMethodException     thrown when the validation fails
	 * @throws InstantiationException    thrown when the validation fails
	 * @throws IllegalAccessException    thrown when the validation fails
	 */
	public static void validate(Class<?> clazz)
		throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
	{
		if (Heuristics.isImmutable(clazz))
		{
			log.debug("Lombok Value Class: {}", clazz);
			ValueClassValidator.validate(clazz);
		}
		else
		{
			log.debug("Lombok Data Class: {}", clazz);
			DataClassValidator.validate(clazz);
		}
	}
}
