package com.wccgroup.coloja.almostlombokobjects;

import java.util.*;
import com.wccgroup.coloja.lombokobjects.AnEnum;
import lombok.Value;

@Value
public class NonPrimitiveValue
{
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
	private Set<String> aSet;
	private UUID aUUID;

	public void helperMethod1()
	{
	}

	public int getADerivedProperty()
	{
		if (aSet == null)
		{
			return 0;
		}

		return aSet.size();
	}
}
