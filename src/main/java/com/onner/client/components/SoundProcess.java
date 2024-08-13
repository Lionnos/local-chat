package com.onner.client.components;

public class SoundProcess implements Runnable {
    private final SoundPlayer soundPlayer;
    private volatile boolean running = false;
    private volatile String soundToPlay = null;

    public SoundProcess(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    @Override
    public void run() {
        while (running) {
            if (soundToPlay != null) {
                SoundPlayer.playSound(soundToPlay);
                soundToPlay = null;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void startSoundChat() {
        soundToPlay = soundPlayer.getSoundEat();
        if (!running) {
            running = true;
            new Thread(this).start();
        }
    }

    public void stopSoundChat() {
        running = false;
    }
}
