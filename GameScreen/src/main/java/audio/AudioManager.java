package audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {

    private static MediaPlayer musicPlayer;

    public static void playMusic(String path) {
        try {
            Media media = new Media(AudioManager.class.getResource(path).toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.5);
            musicPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
    public static void playSound(String path) {
        try {
            Media media = new Media(AudioManager.class.getResource(path).toExternalForm());
            MediaPlayer sound = new MediaPlayer(media);
            sound.setVolume(0.7);
            sound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
