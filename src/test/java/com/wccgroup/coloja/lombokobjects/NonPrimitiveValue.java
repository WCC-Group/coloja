package com.wccgroup.coloja.lombokobjects;

import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class NonPrimitiveValue
{
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
}
