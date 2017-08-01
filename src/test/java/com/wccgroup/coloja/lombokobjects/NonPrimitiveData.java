package com.wccgroup.coloja.lombokobjects;

import java.util.*;
import lombok.Data;

@Data
public class NonPrimitiveData
{
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
	private UUID aUUID;
}
