package com.wccgroup.coloja;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgnorableMethod
{
	private Class<?> clazz;
	private String method;
}
