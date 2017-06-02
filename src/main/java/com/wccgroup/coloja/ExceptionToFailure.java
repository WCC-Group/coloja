package com.wccgroup.coloja;

import static org.junit.Assert.fail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionToFailure
{
	public static void handle(Exception e)
	{
		log.warn("An unexpected exception was encountered: {}", e);
		fail("An unexpected exception was encountered: " + e.getMessage());
	}

	public static void handle(Exception e, Class clazz)
	{
		log.warn("An unexpected exception was encountered while processing {}: {}", clazz.getName(), e);
		fail("An unexpected exception was encountered while processing " + clazz.getName() + ": " + e.getMessage());
	}

	public static void handle(Exception e, String className)
	{
		log.warn("An unexpected exception was encountered while processing {}: {}", className, e);
		fail("An unexpected exception was encountered while processing " + className + ": " + e.getMessage());
	}
}
