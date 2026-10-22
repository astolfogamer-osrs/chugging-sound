package com.chuggingsound;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("chuggingsound")
public interface ChuggingSoundConfig extends Config
{
	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume do som personalizado (0 a 100)"
	)
	default int volume()
	{
		return 100;
	}
}
