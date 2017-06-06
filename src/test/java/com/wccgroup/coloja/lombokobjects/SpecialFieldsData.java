package com.wccgroup.coloja.lombokobjects;

import java.lang.reflect.Method;
import lombok.Data;

@Data
public class SpecialFieldsData
{
	private String aString;
	private String anotherString;
	private Object someObject;
	private Method someMethod;
	private Class<?> aClass;
}
