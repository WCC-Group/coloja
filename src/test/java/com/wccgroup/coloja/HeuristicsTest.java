package com.wccgroup.coloja;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import com.wccgroup.coloja.lombokobjects.BuildableValue;

public class HeuristicsTest
{
	@Test
	public void shouldBeValueClass()
	{
		assertThat(Heuristics.isImmutable(BuildableValue.class), is(true));
	}
}
