package com.pinball.core;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;

public class SoundManager {
    // 短音效與連擊限制變數
    private AudioClip bumperSound;
    private AudioClip flipperSound;
    private long lastBumperTime = 0;
    private long lastFlipperTime = 0;

    // 背景音樂播放器與各音訊 URL 路徑
    private MediaPlayer bgmPlayer;
    private URL soldierDreamURL;
    private URL moonHaloURL;
    private URL rickrollURL;

    public SoundManager() {
        // 預載基礎短音效
        bumperSound = loadSound("/com/pinball/sounds/new_bumper.wav");
        flipperSound = loadSound("/com/pinball/sounds/flipper.mp3");

        if (bumperSound != null) bumperSound.play(0);
        if (flipperSound != null) flipperSound.play(0);

        // 預載所有背景音樂與迷因路徑
        soldierDreamURL = getClass().getResource("/com/pinball/sounds/soldier_dream.mp3");
        moonHaloURL = getClass().getResource("/com/pinball/sounds/moon_halo.mp3");
        rickrollURL = getClass().getResource("/com/pinball/sounds/rickroll.mp3");
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

    // 🌟 遊戲開始或重新開始：循環播放 Soldier Dream
    public void playSoldierDream() {
        stopBGM();
        if (soldierDreamURL != null) {
            try {
                Media media = new Media(soldierDreamURL.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.4);
                bgmPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🌟 剩餘最後一球：切換至 Moon Halo 並精準自 2:13 處高潮爆發
    public void switchToMoonHalo() {
        stopBGM();
        if (moonHaloURL != null) {
            try {
                Media media = new Media(moonHaloURL.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.55); // 稍微拉高音量增加史詩感
                
                // 等待 Media 載入就緒後，直接 Seek 到 133 秒 (2分13秒)
                bgmPlayer.setOnReady(() -> {
                    bgmPlayer.seek(Duration.seconds(133));
                });
                
                bgmPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🌟 遊戲結束 (Game Over)：啟動 Rickroll 精神污染機制
    public void playRickroll() {
        stopBGM();
        if (rickrollURL != null) {
            try {
                Media media = new Media(rickrollURL.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setVolume(0.6); // 迷因爆擊，音量調大
                bgmPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🌟 安全關閉並釋放當前 MediaPlayer 資源
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    // 提供對外呼叫的彈珠撞擊 Bumper 播放方法
    public void playBumper(double volume) {
        if (bumperSound != null) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastBumperTime > 50) {
                bumperSound.play(volume);
                lastBumperTime = currentTime;
            }
        }
    }

    // 提供對外呼叫的擺動撥片 Flipper 播放方法
    public void playFlipper() {
        if (flipperSound != null) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastFlipperTime > 50) {
                flipperSound.play(0.35);
                lastFlipperTime = currentTime;
            }
        }
    }
}