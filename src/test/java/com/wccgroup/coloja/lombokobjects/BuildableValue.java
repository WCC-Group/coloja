package com.wccgroup.coloja.lombokobjects;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BuildableValue
{
	private boolean primitiveBoolean;
	private Boolean objectBoolean;
	private byte primitiveByte;
	private Byte objectByte;
	private char primitiveChar;
	private Character objectCharacter;
	private short primitiveShort;
	private Short objectShort;
	private int primitiveInt;
	private Integer objectInteger;
	private long primitiveLong;
	private Long objectLong;
	private float primitiveFloat;
	private Float objectFloat;
	private double primitiveDouble;
	private Double objectDouble;
	private String objectString;
	private AnEnum anEnum;
	private Object anObject;
	private List<Object> someObjects;
	private Map<String, Object> aMap;
}
