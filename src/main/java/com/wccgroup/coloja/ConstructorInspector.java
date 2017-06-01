package com.wccgroup.coloja;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstructorInspector
{
	// Heuristics. The one with the least amount of arguments is the best one.
	public static Constructor getBestConstructor(Class<?> clazz)
	{
		Constructor[] constructors = clazz.getDeclaredConstructors();
		Constructor theConstructor = null;

		if (constructors.length == 1)
		{
			theConstructor = constructors[0];
		}
		else if (constructors.length == 0)
		{
			// Oh crap..
			log.warn("Oops, {} has no usable constructors.", clazz);
			return null;
		}
		else
		{
			theConstructor = Arrays.stream(constructors)
				.sorted(Comparator.comparing(c -> c.getParameterCount()))
				.collect(Collectors.toList())
				.get(0);
		}

		if (!Modifier.isPublic(theConstructor.getModifiers()))
		{
			log.debug("Making constructor of {} accessible.", clazz.getName());
			theConstructor.setAccessible(true);
		}

		return theConstructor;
	}
}
