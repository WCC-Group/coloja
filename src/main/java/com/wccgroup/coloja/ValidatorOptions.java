package com.wccgroup.coloja;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatorOptions
{
	private List<IgnorableMethod> ignorableMethods = new ArrayList<>();
	private List<IgnorableProperty> ignorableProperties = new ArrayList<>();
}
