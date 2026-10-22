package com.chuggingsound;

import com.google.inject.Provides;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
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

	private Clip clip;

	@Provides
	ChuggingSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChuggingSoundConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		try (InputStream in = ChuggingSoundPlugin.class.getResourceAsStream("chug.wav"))
		{
			if (in == null)
			{
				log.warn("chug.wav nao encontrado em resources/com/chuggingsound/");
				return;
			}

			try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(in)))
			{
				clip = AudioSystem.getClip();
				clip.open(audio);
			}
		}
	}

	@Override
	protected void shutDown()
	{
		if (clip != null)
		{
			clip.close();
			clip = null;
		}
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
		if (clip == null)
		{
			return;
		}
		applyVolume();
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	private void applyVolume()
	{
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float volume = config.volume() / 100f;
		float db = volume <= 0f
			? gain.getMinimum()
			: Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), 20f * (float) Math.log10(volume)));
		gain.setValue(db);
	}
}
