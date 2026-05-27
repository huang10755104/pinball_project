package com.pinball.core;

import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundManager {
    private AudioClip bumperSound;
    private AudioClip flipperSound;
    private long lastBounceTime = 0;

    public SoundManager() {
        // 預載音效
        bumperSound = loadSound("/com/pinball/sounds/new_bumper.wav");
        flipperSound = loadSound("/com/pinball/sounds/flipper.mp3");

        bumperSound.play(0);
        flipperSound.play(0);
    }

    private AudioClip loadSound(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                return new AudioClip(resource.toString());
            } else {
                System.err.println("找不到音效檔案: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 提供對外呼叫的播放方法
    public void playBumper(double volume) {
        if (bumperSound != null) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastBounceTime > 50) {
                bumperSound.play(volume);
                lastBounceTime = currentTime;
            }
        }
    }

    public void playFlipper() {
        if (flipperSound != null) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastBounceTime > 50) {
                flipperSound.play(0.35);
                lastBounceTime = currentTime;
            }
            
        }
    }
}