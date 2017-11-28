package com.wccgroup.coloja.almostlombokobjects;

import java.util.*;
import com.wccgroup.coloja.lombokobjects.AnEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "ignoredInEqualsAndHashCode")
public class FieldExclusionsData
{
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
	private UUID aUUID;
	private Set<String> aSet;
	private String ignoredInEqualsAndHashCode;
}
