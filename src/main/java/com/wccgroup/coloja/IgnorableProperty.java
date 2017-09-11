package com.wccgroup.coloja;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgnorableProperty
{
	private Class<?> clazz;
	private String property;
}
