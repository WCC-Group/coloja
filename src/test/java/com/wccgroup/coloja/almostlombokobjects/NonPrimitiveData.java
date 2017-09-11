package com.wccgroup.coloja.almostlombokobjects;

import java.util.*;
import com.wccgroup.coloja.lombokobjects.AnEnum;
import lombok.Data;

@Data
public class NonPrimitiveData
{
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
	private UUID aUUID;
	private Set<String> aSet;

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
