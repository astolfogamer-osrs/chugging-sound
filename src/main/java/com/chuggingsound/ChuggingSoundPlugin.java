package com.chuggingsound;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Chugging Sound",
	description = "Troca o som de beber do chugging barrel por um som personalizado",
	tags = {"sound", "potion", "barrel"}
)
public class ChuggingSoundPlugin extends Plugin
{
	// herblore_pre_pot_drink_01 (som de beber do chugging barrel)
	private static final int CHUGGING_DRINK_SOUND = 9491;

	@Inject
	private Client client;

	@Inject
	private ChuggingSoundConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	@Provides
	ChuggingSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChuggingSoundConfig.class);
	}

	// som tocado pelo proprio jogador
	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (event.getSoundId() == CHUGGING_DRINK_SOUND)
		{
			event.consume();
			play();
		}
	}

	// caso o jogo envie o som como som de area
	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		if (event.getSoundId() == CHUGGING_DRINK_SOUND && event.getSource() == client.getLocalPlayer())
		{
			event.consume();
			play();
		}
	}

	private void play()
	{
		int volume = config.volume();
		if (volume <= 0)
		{
			return;
		}

		// converte 0-100 para ganho em dB (100 = 0 dB, sem alteracao)
		float gain = 20f * (float) Math.log10(volume / 100f);

		try
		{
			audioPlayer.play(ChuggingSoundPlugin.class, "chug.wav", gain);
		}
		catch (Exception e)
		{
			log.warn("Nao foi possivel tocar o som", e);
		}
	}
}
