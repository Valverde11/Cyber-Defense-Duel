package audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {

    private static MediaPlayer musicPlayer;     // Guardamos el reproductor de música para poder pararlo

    public static void playMusic(String path) {
        try {
            Media media = new Media(AudioManager.class.getResource(path).toExternalForm()); // Carga el archivo desde recursos
            musicPlayer = new MediaPlayer(media); // Reproductor de musica de fondo
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Bucle infinito
            musicPlayer.setVolume(0.5); // Volumen al 50%
            musicPlayer.play(); // Inicia reproduccion
        } catch (Exception e) {
            e.printStackTrace(); // Si no encuentra el archivo, muestra el error
        }
    }

    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop(); // Detiene la música de fondo
        }
    }

    public static void playSound(String path) {
        try {
            Media media = new Media(AudioManager.class.getResource(path).toExternalForm());
            MediaPlayer sound = new MediaPlayer(media); // Reproductor nuevo para efectos
            sound.setVolume(0.7); // Volumen de efectos
            sound.play(); // Reproduce una sola vez y se olvida
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}