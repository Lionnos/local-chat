package com.onner.client.components;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {
    private String soundChat;

    private static Clip clip;
    private static AudioInputStream audioStream;
    private static long clipTimePosition = 0;

    public SoundPlayer() {
        this.soundChat = "src/main/java/com/onner/client/resources/sound.wav";
    }

    public static void playSound(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            if (clip != null) {
                clip.close();
            }
            audioStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clipTimePosition = 0;
                        clip.close();
                    }
                }
            });
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | javax.sound.sampled.LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public String getSoundEat() {
        return soundChat;
    }
}
